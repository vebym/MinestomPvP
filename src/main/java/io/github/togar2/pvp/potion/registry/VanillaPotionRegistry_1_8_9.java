package io.github.togar2.pvp.potion.registry;

import io.github.togar2.pvp.potion.effect.AbsorptionPotionEffect;
import io.github.togar2.pvp.potion.effect.CombatPotionEffect;
import io.github.togar2.pvp.potion.effect.GlowingPotionEffect;
import io.github.togar2.pvp.potion.effect.HealthBoostPotionEffect;
import io.github.togar2.pvp.potion.item.CombatPotionType;
import io.github.togar2.pvp.utils.PotionFlagsUtil;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum VanillaPotionRegistry_1_8_9 {
	;

	/**
	 * Only legacy potions. Potions that did not exist in 1.8 won't have any effect.
	 */
	public static final CombatPotionRegistry REGISTRY_STRICT;
	/**
	 * Legacy potions + modern fallbacks
	 */
	public static final CombatPotionRegistry REGISTRY_LENIENT;

	static {
		// avoid duplicates
		var types = getTypes();
		var effects = getEffects();

		REGISTRY_STRICT = CombatPotionRegistry.of(types, effects);

		var vanilla = ((CombatPotionRegistryImpl) VanillaPotionRegistry.get());
		var types2 = new ArrayList<>(vanilla.getTypes().values());
		types2.addAll(types);

		var effects2 = new ArrayList<>(vanilla.getEffects().values());
		effects2.addAll(effects);

		REGISTRY_LENIENT = CombatPotionRegistry.of(types2, effects2);
	}

	private static List<CombatPotionType> getTypes() {
		var flags = PotionFlagsUtil.defaultFlags();
		return List.of(
			new CombatPotionType(PotionType.WATER),
			new CombatPotionType(PotionType.MUNDANE),

			new CombatPotionType(PotionType.NIGHT_VISION, new Potion(PotionEffect.NIGHT_VISION, 0, 3600, flags)),
			new CombatPotionType(PotionType.LONG_NIGHT_VISION, new Potion(PotionEffect.NIGHT_VISION, 0, 9600, flags)),
			new CombatPotionType(PotionType.INVISIBILITY, new Potion(PotionEffect.INVISIBILITY, 0, 3600, flags)),
			new CombatPotionType(PotionType.LONG_INVISIBILITY, new Potion(PotionEffect.INVISIBILITY, 0, 9600, flags)),
			new CombatPotionType(PotionType.LEAPING, new Potion(PotionEffect.JUMP_BOOST, 0, 3600, flags)),
			new CombatPotionType(PotionType.LONG_LEAPING, new Potion(PotionEffect.JUMP_BOOST, 0, 9600, flags)),
			new CombatPotionType(PotionType.STRONG_LEAPING, new Potion(PotionEffect.JUMP_BOOST, 1, 1800, flags)),
			new CombatPotionType(PotionType.FIRE_RESISTANCE, new Potion(PotionEffect.FIRE_RESISTANCE, 0, 3600, flags)),
			new CombatPotionType(PotionType.LONG_FIRE_RESISTANCE, new Potion(PotionEffect.FIRE_RESISTANCE, 0, 9600, flags)),
			new CombatPotionType(PotionType.SWIFTNESS, new Potion(PotionEffect.SPEED, 0, 3600, flags)),
			new CombatPotionType(PotionType.LONG_SWIFTNESS, new Potion(PotionEffect.SPEED, 0, 9600, flags)),
			new CombatPotionType(PotionType.STRONG_SWIFTNESS, new Potion(PotionEffect.SPEED, 1, 1800, flags)),
			new CombatPotionType(PotionType.SLOWNESS, new Potion(PotionEffect.SLOWNESS, 0, 1800, flags)),
			new CombatPotionType(PotionType.LONG_SLOWNESS, new Potion(PotionEffect.SLOWNESS, 0, 4800, flags)),
			new CombatPotionType(PotionType.STRONG_SLOWNESS, new Potion(PotionEffect.SLOWNESS, 3, 400, flags)),
			new CombatPotionType(PotionType.WATER_BREATHING, new Potion(PotionEffect.WATER_BREATHING, 0, 3600, flags)),
			new CombatPotionType(PotionType.LONG_WATER_BREATHING, new Potion(PotionEffect.WATER_BREATHING, 0, 9600, flags)),
			new CombatPotionType(PotionType.HEALING, new Potion(PotionEffect.INSTANT_HEALTH, 0, 1, flags)),
			new CombatPotionType(PotionType.STRONG_HEALING, new Potion(PotionEffect.INSTANT_HEALTH, 1, 1, flags)),
			new CombatPotionType(PotionType.HARMING, new Potion(PotionEffect.INSTANT_DAMAGE, 0, 1, flags)),
			new CombatPotionType(PotionType.STRONG_HARMING, new Potion(PotionEffect.INSTANT_DAMAGE, 1, 1, flags)),
			new CombatPotionType(PotionType.POISON, new Potion(PotionEffect.POISON, 0, 900, flags)),
			new CombatPotionType(PotionType.REGENERATION, new Potion(PotionEffect.REGENERATION, 0, 900, flags)),
			new CombatPotionType(PotionType.STRONG_REGENERATION, new Potion(PotionEffect.REGENERATION, 1, 450, flags)),
			new CombatPotionType(PotionType.STRENGTH, new Potion(PotionEffect.STRENGTH, 0, 3600, flags)),
			new CombatPotionType(PotionType.LONG_STRENGTH, new Potion(PotionEffect.STRENGTH, 0, 9600, flags)),
			new CombatPotionType(PotionType.STRONG_STRENGTH, new Potion(PotionEffect.STRENGTH, 1, 1800, flags)),
			new CombatPotionType(PotionType.WEAKNESS, new Potion(PotionEffect.WEAKNESS, 0, 1800, flags)),
			new CombatPotionType(PotionType.LONG_WEAKNESS, new Potion(PotionEffect.WEAKNESS, 0, 4800, flags)),

			/*
				====== Different from modern ======
			 */
			new CombatPotionType(PotionType.LONG_POISON, new Potion(PotionEffect.POISON, 0, 2400, flags)),
			new CombatPotionType(PotionType.STRONG_POISON, new Potion(PotionEffect.POISON, 1, 450, flags)),
			new CombatPotionType(PotionType.LONG_REGENERATION, new Potion(PotionEffect.REGENERATION, 0, 2400, flags))
		);
	}

	private static List<CombatPotionEffect> getEffects() {
		return List.of(
			new CombatPotionEffect(
				PotionEffect.SPEED,
				Attribute.MOVEMENT_SPEED,
				Key.key("minecraft:effect.speed"),
				0.2d, AttributeOperation.ADD_MULTIPLIED_TOTAL
			),
			new CombatPotionEffect(
				PotionEffect.SLOWNESS,
				Attribute.MOVEMENT_SPEED,
				Key.key("minecraft:effect.slowness"),
				-0.15, AttributeOperation.ADD_MULTIPLIED_TOTAL
			),
			new CombatPotionEffect(
				PotionEffect.HASTE,
				Attribute.ATTACK_SPEED,
				Key.key("minecraft:effect.haste"),
				0.1, AttributeOperation.ADD_MULTIPLIED_TOTAL
			),
			new CombatPotionEffect(
				PotionEffect.MINING_FATIGUE,
				Attribute.ATTACK_SPEED,
				Key.key("minecraft:effect.mining_fatigue"),
				-0.1, AttributeOperation.ADD_MULTIPLIED_TOTAL
			),
			new CombatPotionEffect(PotionEffect.INSTANT_HEALTH),
			new CombatPotionEffect(PotionEffect.INSTANT_DAMAGE),
			new CombatPotionEffect(
				PotionEffect.JUMP_BOOST,
				Attribute.SAFE_FALL_DISTANCE,
				Key.key("minecraft:effect.jump_boost"),
				1.0, AttributeOperation.ADD_VALUE
			),
			new CombatPotionEffect(PotionEffect.NAUSEA),
			new CombatPotionEffect(PotionEffect.REGENERATION),
			new CombatPotionEffect(PotionEffect.RESISTANCE),
			new CombatPotionEffect(PotionEffect.FIRE_RESISTANCE),
			new CombatPotionEffect(PotionEffect.WATER_BREATHING),
			new CombatPotionEffect(PotionEffect.INVISIBILITY),
			new CombatPotionEffect(PotionEffect.BLINDNESS),
			new CombatPotionEffect(PotionEffect.NIGHT_VISION),
			new CombatPotionEffect(PotionEffect.HUNGER),
			new CombatPotionEffect(PotionEffect.POISON),
			new CombatPotionEffect(PotionEffect.WITHER),
			new HealthBoostPotionEffect(),
			new AbsorptionPotionEffect(),
			new CombatPotionEffect(PotionEffect.SATURATION),

			/*
				====== Different from modern ======
			 */
			new CombatPotionEffect(
				PotionEffect.STRENGTH,
				Attribute.ATTACK_DAMAGE,
				Key.key("minecraft:effect.strength"),
				1.3d, AttributeOperation.ADD_MULTIPLIED_TOTAL
			),
			new CombatPotionEffect(
				PotionEffect.WEAKNESS,
				Attribute.ATTACK_DAMAGE,
				Key.key("minecraft:effect.weakness"),
				-0.5d, AttributeOperation.ADD_VALUE
			)
		);
	}
}
