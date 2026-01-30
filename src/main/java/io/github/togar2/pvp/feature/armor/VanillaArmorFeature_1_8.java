package io.github.togar2.pvp.feature.armor;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;

/**
 * How armor worked up to Minecraft 1.9
 *
 * @see ArmorFeature
 */
@SuppressWarnings("MagicNumber")
public class VanillaArmorFeature_1_8 extends VanillaArmorFeature {

	public static final DefinedFeature<ArmorFeature> DEFINED =
		new DefinedFeature<>(
			FeatureType.ARMOR,
			VanillaArmorFeature_1_8::new,
			FeatureType.ENCHANTMENT
		);

	public VanillaArmorFeature_1_8(FeatureConfiguration configuration) {
		super(configuration);
	}

	@Override
	protected float getDamageWithArmor(
		LivingEntity entity,
		DamageTypeInfo typeInfo,
		float damage
	) {
		if (typeInfo.bypassesArmor()) return damage;

		double armorValue = entity.getAttributeValue(Attribute.ARMOR);
		int armorMultiplier = 25 - (int) armorValue;

		return (damage * armorMultiplier) / 25;
	}

	@Override
	protected float getDamageAfterProtectionEnchantment(float damageDealt, int protection) {
		if (protection > 20) {
			protection = 20;
		}

		if (protection > 0) {
			int j = 25 - protection;
			float f = damageDealt * j;
			damageDealt = f / 25;
		}
		return damageDealt;
	}
}
