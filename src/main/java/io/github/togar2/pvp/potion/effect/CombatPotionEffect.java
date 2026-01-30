package io.github.togar2.pvp.potion.effect;

import io.github.togar2.pvp.enchantment.EntityGroup;
import io.github.togar2.pvp.feature.food.ExhaustionFeature;
import io.github.togar2.pvp.feature.food.FoodFeature;
import io.github.togar2.pvp.utils.Pair;
import net.kyori.adventure.key.Key;
import net.minestom.server.color.AlphaColor;
import net.minestom.server.color.Color;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Defines what does a {@link PotionEffect}.
 * <br>Contains the effect, its particles, and {@link AttributeModifier attributes}.
 * <br>Can have custom apply/remove actions.
 */
public class CombatPotionEffect {

	private final PotionEffect effect;
	private final List<Pair<Attribute, AttributeModifier>> attributes;

	public CombatPotionEffect(PotionEffect potionEffect) {
		this.effect = Objects.requireNonNull(potionEffect);
		this.attributes = List.of();
	}

	public CombatPotionEffect(
		PotionEffect potionEffect,
		Attribute attribute,
		Key attributeKey,
		double attributeValue,
		AttributeOperation operation
	) {
		this(potionEffect, List.of(Pair.of(
			attribute,
			new AttributeModifier(
				attributeKey,
				attributeValue,
				operation
			)
		)));
	}

	public CombatPotionEffect(
		PotionEffect potionEffect,
		Map<Attribute, AttributeModifier> attributes
	) {
		this(potionEffect, attributes.entrySet().stream()
			.map(entry -> Pair.of(entry.getKey(), entry.getValue()))
			.toList()
		);
	}

	public CombatPotionEffect(
		PotionEffect potionEffect,
		List<Pair<Attribute, AttributeModifier>> attributes
	) {
		this.effect = Objects.requireNonNull(potionEffect);
		this.attributes = List.copyOf(Objects.requireNonNull(attributes));
	}

	public PotionEffect getPotionEffect() {
		return effect;
	}

	public Particle getParticle(Potion potion) {
		return Particle.ENTITY_EFFECT.withColor(
			new AlphaColor(
				potion.isAmbient() ? 38 : 255,
				new Color(potion.effect().registry().color())
			)
		);
	}

	public void applyUpdateEffect(LivingEntity entity, int amplifier,
						ExhaustionFeature exhaustionFeature, FoodFeature foodFeature) {
		if (effect == PotionEffect.REGENERATION) {
			if (entity.getHealth() < entity.getAttributeValue(Attribute.MAX_HEALTH)) {
				entity.setHealth(entity.getHealth() + 1);
			}
			return;
		} else if (effect == PotionEffect.POISON) {
			if (entity.getHealth() > 1.0F) {
				entity.damage(DamageType.MAGIC, 1.0F);
			}
			return;
		} else if (effect == PotionEffect.WITHER) {
			entity.damage(DamageType.WITHER, 1.0F);
			return;
		}

		if (entity instanceof Player player) {
			if (effect == PotionEffect.HUNGER) {
				exhaustionFeature.applyHungerEffect(player, amplifier);
				return;
			} else if (effect == PotionEffect.SATURATION) {
				foodFeature.applySaturationEffect(player, amplifier);
				return;
			}
		}

		if (effect == PotionEffect.INSTANT_DAMAGE || effect == PotionEffect.INSTANT_HEALTH) {
			EntityGroup entityGroup = EntityGroup.ofEntity(entity);

			if (shouldHeal(entityGroup)) {
				entity.setHealth(entity.getHealth() + Math.max(4 << amplifier, 0));
			} else {
				entity.damage(DamageType.MAGIC, (6 << amplifier));
			}
		}
	}

	public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target,
						 int amplifier, double proximity, ExhaustionFeature exhaustionFeature, FoodFeature foodFeature) {
		EntityGroup targetGroup = EntityGroup.ofEntity(target);

		if (effect != PotionEffect.INSTANT_DAMAGE && effect != PotionEffect.INSTANT_HEALTH) {
			applyUpdateEffect(target, amplifier, exhaustionFeature, foodFeature);
			return;
		}

		if (shouldHeal(targetGroup)) {
			int amount = (int) (proximity * (4 << amplifier) + 0.5D);
			target.setHealth(target.getHealth() + amount);
		} else {
			int amount = (int) (proximity * (6 << amplifier) + 0.5D);
			if (source == null) {
				target.damage(DamageType.MAGIC, amount);
			} else {
				target.damage(new Damage(DamageType.INDIRECT_MAGIC, source, attacker, null, amount));
			}
		}
	}

	private boolean shouldHeal(EntityGroup group) {
		return (group.isUndead() && effect == PotionEffect.INSTANT_DAMAGE)
			|| (!group.isUndead() && effect == PotionEffect.INSTANT_HEALTH);
	}

	public boolean canApplyUpdateEffect(int duration, int amplifier) {
		if (isInstant() || effect == PotionEffect.SATURATION) return duration >= 1;

		int applyInterval;
		if (effect == PotionEffect.REGENERATION) {
			applyInterval = 50 >> amplifier;
		} else if (effect == PotionEffect.POISON) {
			applyInterval = 25 >> amplifier;
		} else if (effect == PotionEffect.WITHER) {
			applyInterval = 40 >> amplifier;
		} else {
			return effect == PotionEffect.HUNGER;
		}

		if (applyInterval > 0) {
			return duration % applyInterval == 0;
		} else {
			return true;
		}
	}

	public boolean isInstant() {
		return effect.registry().isInstantaneous();
	}

	public void apply(LivingEntity entity, int amplifier) {
		attributes.forEach(pair -> {
			AttributeInstance instance = entity.getAttribute(pair.key());

			var modifier = pair.value();
			instance.removeModifier(modifier);

			instance.addModifier(new AttributeModifier(
				modifier.id(),
				adjustModifierAmount(
					amplifier,
					modifier
				),
				modifier.operation())
			);
		});
	}

	public void remove(LivingEntity entity, int amplifier) {
		attributes.forEach(pair ->
			entity.getAttribute(pair.key())
				.removeModifier(pair.value())
		);
	}

	private double adjustModifierAmount(int amplifier, AttributeModifier modifier) {
		return modifier.amount() * (amplifier + 1);
	}
}
