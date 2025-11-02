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
import net.minestom.server.utils.MathUtils;

/**
 * Vanilla implementation of {@link ArmorFeature}
 */
public class VanillaArmorFeature implements ArmorFeature {
	public static final DefinedFeature<VanillaArmorFeature> MODERN = new DefinedFeature<>(
		FeatureType.ARMOR,
		config -> new VanillaArmorFeature(config, false),
		FeatureType.ENCHANTMENT
	);

	public static final DefinedFeature<VanillaArmorFeature> LEGACY = new DefinedFeature<>(
		FeatureType.ARMOR,
		config -> new VanillaArmorFeature(config, true),
		FeatureType.ENCHANTMENT
	);

	private final boolean legacy;
	private final FeatureConfiguration configuration;

	private EnchantmentFeature enchantmentFeature;

	public VanillaArmorFeature(FeatureConfiguration configuration, boolean legacy) {
		this.configuration = configuration;
		this.legacy = legacy;
	}
	
	@Override
	public void initDependencies() {
		this.enchantmentFeature = configuration.get(FeatureType.ENCHANTMENT);
	}
	
	@Override
	public float getDamageWithProtection(LivingEntity entity, DamageType type, float amount) {
		DamageTypeInfo info = DamageTypeInfo.of(MinecraftServer.getDamageTypeRegistry().getKey(type));
		amount = getDamageWithArmor(entity, info, amount);
		return getDamageWithEnchantments(entity, type, amount);
	}
	
	protected float getDamageWithArmor(LivingEntity entity, DamageTypeInfo typeInfo, float amount) {
		if (typeInfo.bypassesArmor()) return amount;
		
		double armorValue = entity.getAttributeValue(Attribute.ARMOR);
		if (legacy) {
			int armorMultiplier = 25 - (int) armorValue;
			return (amount * armorMultiplier) / 25;
		} else {
			return getDamageLeft(
				amount, (float) Math.floor(armorValue),
				(float) entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS)
			);
		}
	}
	
	protected float getDamageWithEnchantments(LivingEntity entity, DamageType damageType, float amount) {
		DamageTypeInfo damageTypeInfo = DamageTypeInfo.of(MinecraftServer.getDamageTypeRegistry().getKey(damageType));
		if (damageTypeInfo.unblockable()) return amount;
		
		int k;
		TimedPotion effect = entity.getEffect(PotionEffect.RESISTANCE);
		if (effect != null) {
			k = (effect.potion().amplifier() + 1) * 5;
			int j = 25 - k;
			float f = amount * j;
			amount = Math.max(f / 25, 0);
		}
		
		if (amount <= 0) {
			return 0;
		} else {
			k = enchantmentFeature.getProtectionAmount(entity, damageType);
			if (legacy) {
				if (k > 20) {
					k = 20;
				}

				if (k > 0) {
					int j = 25 - k;
					float f = amount * j;
					amount = f / 25;
				}
			} else {
				if (k > 0) {
					amount = getDamageAfterProtectionEnchantment(amount, k);
				}
			}
			
			return amount;
		}
	}
	
	protected float getDamageLeft(float damage, float armor, float armorToughness) {
		float f = 2.0f + armorToughness / 4.0f;
		float g = MathUtils.clamp(armor - damage / f, armor * 0.2f, 20.0f);
		return damage * (1.0F - g / 25.0F);
	}
	
	protected float getDamageAfterProtectionEnchantment(float damageDealt, float protection) {
		float f = MathUtils.clamp(protection, 0.0f, 20.0f);
		return damageDealt * (1.0f - f / 25.0f);
	}
}
