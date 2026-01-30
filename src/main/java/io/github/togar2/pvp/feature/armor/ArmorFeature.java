package io.github.togar2.pvp.feature.armor;

import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;

/**
 * Combat feature used for determining the resulting damage after armor usage.
 */
public interface ArmorFeature extends CombatFeature {
	ArmorFeature NO_OP = (entity, type, amount) -> amount;

	DefinedFeature<ArmorFeature> DEFINED_LATEST = VanillaArmorFeature.DEFINED;
	DefinedFeature<ArmorFeature> DEFINED_1_7_10 = VanillaArmorFeature_1_8.DEFINED;

	/**
	 * Computes the damage with the armor's protection, and potential enchantments, applied.
	 * @return the reduced damage to apply
	 */
	float getDamageWithProtection(LivingEntity entity, DamageType type, float amount);
}
