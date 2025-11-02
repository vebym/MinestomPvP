package io.github.togar2.pvp.feature.attack;

import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.state.PlayerStateFeature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.PotionEffect;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Vanilla implementation of {@link CriticalFeature}
 */
public class VanillaCriticalFeature implements CriticalFeature {
	public static final DefinedFeature<VanillaCriticalFeature> MODERN = new DefinedFeature<>(
		FeatureType.CRITICAL,
		config -> new VanillaCriticalFeature(config, false),
		FeatureType.PLAYER_STATE, FeatureType.VERSION
	);

	public static final DefinedFeature<VanillaCriticalFeature> LEGACY = new DefinedFeature<>(
		FeatureType.CRITICAL,
		config -> new VanillaCriticalFeature(config, true),
		FeatureType.PLAYER_STATE, FeatureType.VERSION
	);

	private final FeatureConfiguration configuration;
	private final boolean legacy;
	
	private PlayerStateFeature playerStateFeature;

	public VanillaCriticalFeature(FeatureConfiguration configuration, boolean legacy) {
		this.configuration = configuration;
		this.legacy = legacy;
	}
	
	@Override
	public void initDependencies() {
		this.playerStateFeature = configuration.get(FeatureType.PLAYER_STATE);
	}
	
	@Override
	public boolean shouldCrit(LivingEntity attacker, AttackValues.PreCritical values) {
		boolean critical = values.strong() && !playerStateFeature.isClimbing(attacker)
				&& attacker.getVelocity().y() < 0 && !attacker.isOnGround()
				&& !attacker.hasEffect(PotionEffect.BLINDNESS)
				&& attacker.getVehicle() == null;
		if (legacy) return critical;
		
		// Not sprinting required for critical in 1.9+
		return critical && !attacker.isSprinting();
	}
	
	@Override
	public float applyToDamage(float damage) {
		return legacy ?
			// TODO RandomizerFeature with custom thresholds
			damage + ThreadLocalRandom.current().nextInt((int) (damage / 2 + 2)) :
			damage * 1.5f;
	}
}
