package io.github.togar2.pvp.feature.armor;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.enchantment.EnchantmentFeature;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;

/**
 * Vanilla implementation of {@link ArmorFeature}
 *
 * Subject to change, as it follows the latest vanilla version.
 */
@SuppressWarnings("MagicNumber")
public class VanillaArmorFeature implements ArmorFeature {

	public static final DefinedFeature<ArmorFeature> DEFINED = new DefinedFeature<>(
		FeatureType.ARMOR,
		VanillaArmorFeature::new,
		FeatureType.ENCHANTMENT
	);

	private final FeatureConfiguration configuration;

	private EnchantmentFeature enchantmentFeature;

	public VanillaArmorFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void initDependencies() {
		this.enchantmentFeature = configuration.get(FeatureType.ENCHANTMENT);
	}

	@Override
	public float getDamageWithProtection(LivingEntity entity, DamageType type, float amount) {
		DamageTypeInfo info = DamageTypeInfo.of(
			MinecraftServer.getDamageTypeRegistry().getKey(type)
		);

		amount = getDamageWithArmor(entity, info, amount);
		return getDamageWithEnchantments(entity, type, amount);
	}

	protected float getDamageWithArmor(
		LivingEntity entity,
		DamageTypeInfo typeInfo,
		float damage
	) {
		if (typeInfo.bypassesArmor()) return damage;

		float armor = (float) Math.floor(entity.getAttributeValue(Attribute.ARMOR));
		float toughness = (float) entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS);

		float f = 2.0f + toughness / 4.0f;
		float g = Math.clamp(armor - damage / f, armor * 0.2f, 20.0f);
		return damage * (1.0F - g / 25.0F);
	}

	protected float getDamageWithEnchantments(
		LivingEntity entity,
		DamageType damageType,
		float amount
	) {
		DamageTypeInfo damageTypeInfo = DamageTypeInfo.of(
			MinecraftServer.getDamageTypeRegistry().getKey(damageType)
		);
		if (damageTypeInfo.unblockable()) return amount;

		int k; // TODO move potion damage reduction to its potion feature
		TimedPotion effect = entity.getEffect(PotionEffect.RESISTANCE);
		if (effect != null) {
			k = (effect.potion().amplifier() + 1) * 5;
			int j = 25 - k;
			float f = amount * j;
			amount = Math.max(f / 25, 0);
		}

		if (amount <= 0) {
			return 0;
		}

		k = enchantmentFeature.getProtectionAmount(entity, damageType);
		return getDamageAfterProtectionEnchantment(amount, k);
	}

	protected float getDamageAfterProtectionEnchantment(float damageDealt, int protection) {
		float f = Math.clamp(protection, 0.0f, 20.0f);
		return damageDealt * (1.0f - f / 25.0f);
	}
}
