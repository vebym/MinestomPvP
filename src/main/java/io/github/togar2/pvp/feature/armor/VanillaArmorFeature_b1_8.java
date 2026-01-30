package io.github.togar2.pvp.feature.armor;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.utils.ModifierId;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.inventory.EquipmentHandler;
import net.minestom.server.item.ItemStack;

/**
 * How armor worked up to Minecraft 1.0.0
 *
 * @see ArmorFeature
 */
// TODO update armor points depending on durability
public class VanillaArmorFeature_b1_8 implements ArmorFeature {

	public static final DefinedFeature<ArmorFeature> DEFINED = new DefinedFeature<>(
		FeatureType.ARMOR,
		VanillaArmorFeature_b1_8::new,
		FeatureType.ITEM_DAMAGE
	);

	/**
	 * in beta, armor points are the same for every armor material,
	 * but are depleted depending on the armor's durability
	 */
	private static final int[] ARMOR_POINTS = {3, 8, 6, 3};
	private static final EquipmentSlot[] ARMOR_SLOTS = {
		EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE,
		EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS
	};

	private static final Key[] MODIFIERS = {
		ModifierId.ARMOR_MODIFIERS[3],
		ModifierId.ARMOR_MODIFIERS[2],
		ModifierId.ARMOR_MODIFIERS[1],
		ModifierId.ARMOR_MODIFIERS[0],
	};


	private final ItemDamageFeature damageFeature;

	public VanillaArmorFeature_b1_8(FeatureConfiguration configuration) {
		this.damageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
	}

	@Override
	public float getDamageWithProtection(LivingEntity entity, DamageType type, float amount) {
		DamageTypeInfo info = DamageTypeInfo.of(
			MinecraftServer.getDamageTypeRegistry().getKey(type)
		);

		if (info.bypassesArmor()) {
			return amount;
		}

		applyEquipmentDamage(entity, amount);

		int maxDurability = 0;
		int totalDamage = 0;

		var armorAttribute = entity.getAttribute(Attribute.ARMOR);

		for (int i = 0; i < 4; i++) {
			var item = entity.getEquipment(ARMOR_SLOTS[i]);

			if (item.isAir()) {
				armorAttribute.removeModifier(MODIFIERS[i]);
				continue;
			}

			armorAttribute.addModifier(new AttributeModifier(
				MODIFIERS[i],
				ARMOR_POINTS[i],
				AttributeOperation.ADD_VALUE
			));

			maxDurability += item.get(DataComponents.MAX_DAMAGE, 0);
			totalDamage += item.get(DataComponents.DAMAGE, 0);
		}

		final double armorPoints = armorAttribute.getValue();

		// Pre-1.0.0 formula. Basically armorPoints * remaining durability percentage
		return (float) (
			((armorPoints - 1) * (maxDurability - totalDamage)) / (maxDurability + 1)
		);
	}

	protected void applyEquipmentDamage(LivingEntity entity, float damage) {
		// - 1 because each armor piece has already taken 1 damage by being hit,
		// applied by VanillaItemDamageFeature
		int durability = Math.max(0, (int) Math.floor(damage / 4) - 1);
		if (durability <= 0) return;

		ItemStack item;
		for (var slot : ARMOR_SLOTS) {
			item = entity.getEquipment(slot);
			if (!item.isAir()) {
				damageFeature.damageEquipment(entity, slot, durability);
			}
		}
	}
}
