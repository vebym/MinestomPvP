package io.github.togar2.pvp.feature.effect;

import io.github.togar2.pvp.entity.projectile.Arrow;
import io.github.togar2.pvp.events.PotionVisibilityEvent;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.food.ExhaustionFeature;
import io.github.togar2.pvp.feature.food.FoodFeature;
import io.github.togar2.pvp.potion.effect.CombatPotionEffect;
import io.github.togar2.pvp.potion.item.CombatPotionType;
import io.github.togar2.pvp.potion.registry.CombatPotionRegistry;
import io.github.togar2.pvp.potion.registry.VanillaPotionRegistry;
import io.github.togar2.pvp.utils.PotionFlagsUtil;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityPotionAddEvent;
import net.minestom.server.event.entity.EntityPotionRemoveEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vanilla implementation of {@link EffectFeature}
 */
public class VanillaEffectFeature implements EffectFeature, RegistrableFeature {

	public static final DefinedFeature<VanillaEffectFeature> MODERN = new DefinedFeature<>(
		FeatureType.EFFECT,
		config -> new VanillaEffectFeature(config),
		FeatureType.EXHAUSTION, FeatureType.FOOD
	);

	public static final int DEFAULT_POTION_COLOR = 0xff385dc6;
	public static final Tag<Map<PotionEffect, Integer>> TAG_DURATION =
		Tag.Transient("minestompvp:effect_duration_remaining");

	private final FeatureConfiguration configuration;
	private final CombatPotionRegistry potionRegistry;

	private ExhaustionFeature exhaustionFeature;
	private FoodFeature foodFeature;

	public VanillaEffectFeature(FeatureConfiguration configuration) {
		this(configuration, VanillaPotionRegistry.get());
	}

	public VanillaEffectFeature(
		FeatureConfiguration configuration,
		CombatPotionRegistry potionRegistry
	) {
		this.configuration = configuration;
		this.potionRegistry = Objects.requireNonNull(potionRegistry);
	}
	
	@Override
	public void initDependencies() {
		this.exhaustionFeature = configuration.get(FeatureType.EXHAUSTION);
		this.foodFeature = configuration.get(FeatureType.FOOD);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(EntityDeathEvent.class, event ->
				event.getEntity().clearEffects());
		
		node.addListener(EntityTickEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity entity)) return;
			Map<PotionEffect, Integer> potionMap = getDurationLeftMap(entity);
			
			for (TimedPotion potion : entity.getActiveEffects()) {
				potionMap.putIfAbsent(potion.potion().effect(), potion.potion().duration() - 1);
				int durationLeft = potionMap.get(potion.potion().effect());
				
				if (durationLeft > 0) {
					CombatPotionEffect combatPotionEffect = potionRegistry.getEffect(potion.potion().effect());
					if (combatPotionEffect == null) continue;
					int amplifier = potion.potion().amplifier();

					if (combatPotionEffect.canApplyUpdateEffect(durationLeft, amplifier)) {
						combatPotionEffect.applyUpdateEffect(entity, amplifier, exhaustionFeature, foodFeature);
					}
					
					potionMap.put(potion.potion().effect(), durationLeft - 1);
				}
			}
			
			if (entity instanceof Player player && player.hasEffect(PotionEffect.ABSORPTION) && player.getAdditionalHearts() <= 0) {
				player.removeEffect(PotionEffect.ABSORPTION);
			}
			
			//TODO keep track of underlying potions with longer duration
			if (potionMap.size() != entity.getActiveEffects().size()) {
				potionMap.keySet().removeIf(effect -> !entity.hasEffect(effect));
			}
		});
		
		node.addListener(EntityPotionAddEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity entity)) return;
			Map<PotionEffect, Integer> potionMap = getDurationLeftMap(entity);
			boolean infinite = event.getPotion().duration() == Potion.INFINITE_DURATION;
			potionMap.put(event.getPotion().effect(), infinite ? Integer.MAX_VALUE : event.getPotion().duration());
			
			CombatPotionEffect combatPotionEffect = potionRegistry.getEffect(event.getPotion().effect());
			if (combatPotionEffect == null) return;
			combatPotionEffect.apply(entity, event.getPotion().amplifier());
			
			updatePotionVisibility(entity);
		});
		
		node.addListener(EntityPotionRemoveEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity entity)) return;
			
			CombatPotionEffect combatPotionEffect = potionRegistry.getEffect(event.getPotion().effect());
			if (combatPotionEffect == null) return;
			combatPotionEffect.remove(entity, event.getPotion().amplifier());
			
			//Delay update 1 tick because we need to have the removing effect removed
			MinecraftServer.getSchedulerManager()
					.buildTask(() -> updatePotionVisibility(entity))
					.delay(1, TimeUnit.SERVER_TICK)
					.schedule();
		});
	}
	
	private Map<PotionEffect, Integer> getDurationLeftMap(Entity entity) {
		Map<PotionEffect, Integer> potionMap = entity.getTag(TAG_DURATION);
		if (potionMap == null) {
			potionMap = new ConcurrentHashMap<>();
			entity.setTag(TAG_DURATION, potionMap);
		}
		return potionMap;
	}
	
	@Override
	public int getPotionColor(PotionContents contents) {
		if (contents.customColor() != null) {
			RGBLike rgbLike = contents.customColor();
			return PotionColorUtils.rgba(255, rgbLike.red(), rgbLike.green(), rgbLike.blue());
		} else if (contents.equals(PotionContents.EMPTY)) {
			return DEFAULT_POTION_COLOR;
		} else {
			Collection<Potion> effects = getAllPotions(contents);
			int color = PotionColorUtils.getPotionColor(effects);
			return color == -1 ? DEFAULT_POTION_COLOR : color;
		}
	}
	
	@Override
	public List<Potion> getAllPotions(
		PotionType potionType,
	      Collection<CustomPotionEffect> customEffects
	) {
		// PotionType effects plus custom effects
		List<Potion> potions = new ArrayList<>();
		
		CombatPotionType combatPotionType = potionRegistry.getType(potionType);
		if (combatPotionType != null) potions.addAll(combatPotionType.effects());

		potions.addAll(customEffects.stream().map(customPotion ->
			new Potion(Objects.requireNonNull(customPotion.id()),
				(byte)customPotion.amplifier(), customPotion.duration(),
				PotionFlagsUtil.create(
					customPotion.isAmbient(),
					customPotion.showParticles(),
					customPotion.showIcon()
				))).toList());

		return potions;
	}
	
	@Override
	public void updatePotionVisibility(LivingEntity entity) {
		boolean ambient;
		List<Particle> particles;
		boolean invisible;
		
		if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) {
			ambient = false;
			particles = List.of();
			invisible = true;
		} else {
			Collection<TimedPotion> effects = entity.getActiveEffects();
			if (effects.isEmpty()) {
				ambient = false;
				particles = List.of();
				invisible = false;
			} else {
				ambient = true;
				particles = new ArrayList<>();
				
				for (TimedPotion potion : effects) {
					if (!potion.potion().isAmbient()) {
						ambient = false;
					}
					
					if (potion.potion().hasParticles()) {
						CombatPotionEffect effect = potionRegistry.getEffect(potion.potion().effect());
						if (effect == null) continue;
						particles.add(effect.getParticle(potion.potion()));
					}
				}
				
				invisible = entity.hasEffect(PotionEffect.INVISIBILITY);
			}
		}
		
		PotionVisibilityEvent potionVisibilityEvent = new PotionVisibilityEvent(entity, ambient, particles, invisible);
		EventDispatcher.callCancellable(potionVisibilityEvent, () -> {
			LivingEntityMeta meta = (LivingEntityMeta) entity.getEntityMeta();
			
			meta.setPotionEffectAmbient(potionVisibilityEvent.isAmbient());
			meta.setEffectParticles(potionVisibilityEvent.getParticles());
			meta.setInvisible(potionVisibilityEvent.isInvisible());
		});
	}
	
	@Override
	public void addArrowEffects(LivingEntity entity, Arrow arrow) {
		PotionContents potionContents = arrow.getPotion();
		
		CombatPotionType combatPotionType = potionRegistry.getType(potionContents.potion());
		if (combatPotionType != null) {
			for (Potion potion : combatPotionType.effects()) {
				CombatPotionEffect combatPotionEffect = potionRegistry.getEffect(potion.effect());
				if (combatPotionEffect == null) continue;
				if (combatPotionEffect.isInstant()) {
					combatPotionEffect.applyInstantEffect(arrow, null,
							entity, potion.amplifier(), 1.0, exhaustionFeature, foodFeature);
				} else {
					int duration = Math.max(potion.duration() / 8, 1);
					entity.addEffect(new Potion(potion.effect(), potion.amplifier(), duration, potion.flags()));
				}
			}
		}
		
		if (potionContents.customEffects().isEmpty()) return;

		potionContents.customEffects()
			.stream()
			.map(customPotion ->
				new Potion(Objects.requireNonNull(customPotion.id()),
					(byte)customPotion.amplifier(), customPotion.duration(),
					PotionFlagsUtil.create(
						customPotion.isAmbient(),
						customPotion.showParticles(),
						customPotion.showIcon()
					)))
			.forEach(potion -> {
				CombatPotionEffect effect = potionRegistry.getEffect(potion.effect());
				if (effect == null) return;
				if (effect.isInstant()) {
					effect.applyInstantEffect(arrow, null,
						entity, potion.amplifier(), 1.0, exhaustionFeature, foodFeature);
				} else {
					entity.addEffect(new Potion(potion.effect(), potion.amplifier(),
						potion.duration(), potion.flags()));
				}
			});
	}

	@Override
	public void addSplashPotionEffects(
		LivingEntity target,
		PotionContents potionContents,
		Entity source,
		@Nullable Entity attacker,
		boolean directHit
	) {
		double distanceSquared = Objects.requireNonNull(source)
			.getDistanceSquared(target.getPosition().withY(target.getEyeHeight())); // TODO distance from eyes?

		// TODO results are slightly different in 1.8. Find why.
		double proximity = directHit ? 1.0 : (1.0 - Math.sqrt(distanceSquared) / 4.0);

		for (Potion potion : getAllPotions(potionContents)) {
			CombatPotionEffect combatPotionEffect = potionRegistry.getEffect(potion.effect());
			if (combatPotionEffect == null) continue;
			if (combatPotionEffect.isInstant()) {
				combatPotionEffect.applyInstantEffect(source, attacker,
						target, potion.amplifier(), proximity, exhaustionFeature, foodFeature);
			} else {
				int duration = (int) (proximity * potion.duration() + 0.5);
				
				if (duration > 20) {
					target.addEffect(new Potion(potion.effect(), potion.amplifier(), duration, potion.flags()));
				}
			}
		}
	}
}
