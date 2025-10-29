package io.github.togar2.pvp.feature.food;

import io.github.togar2.pvp.events.PlayerExhaustEvent;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.provider.DifficultyProvider;
import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.minestom.server.world.Difficulty;

import java.util.Objects;

/**
 * Vanilla implementation of {@link ExhaustionFeature}
 */
public class VanillaExhaustionFeature implements ExhaustionFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaExhaustionFeature> DEFINED = new DefinedFeature<>(
			FeatureType.EXHAUSTION, VanillaExhaustionFeature::new,
			VanillaExhaustionFeature::initPlayer,
			FeatureType.DIFFICULTY, FeatureType.VERSION
	);

	private static final float DEFAULT_EXHAUSTION = 40.0F;
	public static final Tag<Float> EXHAUSTION =
		Tag.<Float>Transient("minestompvp:food_exhaustion")
			.defaultValue(DEFAULT_EXHAUSTION);


	private final FeatureConfiguration configuration;
	
	private DifficultyProvider difficultyFeature;
	private CombatVersion version;
	
	public VanillaExhaustionFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void initDependencies() {
		this.difficultyFeature = configuration.get(FeatureType.DIFFICULTY);
		this.version = configuration.get(FeatureType.VERSION);
	}
	
	public static void initPlayer(Player player, boolean firstInit) {
		player.setTag(EXHAUSTION, 0.0f);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerTickEvent.class, event -> onTick(event.getPlayer()));
		
		node.addListener(PlayerBlockBreakEvent.class, event ->
				addExhaustion(event.getPlayer(), version.legacy() ? 0.025f : 0.005f));
		
		node.addListener(PlayerMoveEvent.class, this::onMove);
	}
	
	protected void onTick(Player player) {
		if (player.getGameMode().invulnerable()) return;
		
		float exhaustion = player.getTag(EXHAUSTION);
		if (exhaustion > 4) {
			player.setTag(EXHAUSTION, exhaustion - 4);
			if (player.getFoodSaturation() > 0) {
				player.setFoodSaturation(Math.max(player.getFoodSaturation() - 1, 0));
			} else if (difficultyFeature.getValue(player) != Difficulty.PEACEFUL) {
				player.setFood(Math.max(player.getFood() - 1, 0));
			}
		}
	}
	
	protected void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		
		double xDiff = event.getNewPosition().x() - player.getPosition().x();
		double yDiff = event.getNewPosition().y() - player.getPosition().y();
		double zDiff = event.getNewPosition().z() - player.getPosition().z();
		
		// Check if movement was a jump
		if (yDiff > 0.0D && player.isOnGround()) {
			if (player.isSprinting()) {
				addExhaustion(player, version.legacy() ? 0.8f : 0.2f);
			} else {
				addExhaustion(player, version.legacy() ? 0.2f : 0.05f);
			}
		}
		
		if (player.isOnGround()) {
			int l = (int) Math.round(Math.sqrt(xDiff * xDiff + zDiff * zDiff) * 100.0f);
			if (l > 0) addExhaustion(player, (player.isSprinting() ? 0.1f : 0.0f) * l * 0.01f);
		} else {
			if (Objects.requireNonNull(player.getInstance()).getBlock(player.getPosition()) == Block.WATER) {
				int l = (int) Math.round(Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff) * 100.0f);
				if (l > 0) addExhaustion(player, 0.01f * l * 0.01f);
			}
		}
	}
	
	@Override
	public void addExhaustion(Player player, float exhaustion) {
		if (player.getGameMode().invulnerable()) return;

		PlayerExhaustEvent event = new PlayerExhaustEvent(player, exhaustion);
		EventDispatcher.callCancellable(event,
			() -> player.updateTag(EXHAUSTION, v -> Math.min(DEFAULT_EXHAUSTION, v + event.getAmount()))
		);
	}
	
	@Override
	public void addAttackExhaustion(Player player) {
		addExhaustion(player, version.legacy() ? 0.3f: 0.1f);
	}
	
	@Override
	public void addDamageExhaustion(Player player, DamageType type) {
		addExhaustion(player, type.exhaustion() * (version.legacy() ? 3 : 1));
	}
	
	@Override
	public void applyHungerEffect(Player player, int amplifier) {
		addExhaustion(player, (version.legacy() ? 0.025f : 0.005f) * (float) (amplifier + 1));
	}
}
