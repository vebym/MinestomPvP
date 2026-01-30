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
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum VanillaPotionRegistry {
	;

	private static final CombatPotionRegistry REGISTRY =
		CombatPotionRegistry.of(getTypes(), getEffects());

	public static CombatPotionRegistry get() {
		return REGISTRY;
	}

	private static List<CombatPotionType> getTypes() {
		var flags = PotionFlagsUtil.defaultFlags();
		return List.of(
			new CombatPotionType(PotionType.WATER),
			new CombatPotionType(PotionType.MUNDANE),
			new CombatPotionType(PotionType.THICK),
			new CombatPotionType(PotionType.AWKWARD),

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
			new CombatPotionType(PotionType.TURTLE_MASTER, new Potion(PotionEffect.SLOWNESS, 3, 400, flags), new Potion(PotionEffect.RESISTANCE, 2, 400, flags)),
			new CombatPotionType(PotionType.LONG_TURTLE_MASTER, new Potion(PotionEffect.SLOWNESS, 3, 800, flags), new Potion(PotionEffect.RESISTANCE, 2, 800, flags)),
			new CombatPotionType(PotionType.STRONG_TURTLE_MASTER, new Potion(PotionEffect.SLOWNESS, 5, 400, flags), new Potion(PotionEffect.RESISTANCE, 3, 400, flags)),
			new CombatPotionType(PotionType.WATER_BREATHING, new Potion(PotionEffect.WATER_BREATHING, 0, 3600, flags)),
			new CombatPotionType(PotionType.LONG_WATER_BREATHING, new Potion(PotionEffect.WATER_BREATHING, 0, 9600, flags)),
			new CombatPotionType(PotionType.HEALING, new Potion(PotionEffect.INSTANT_HEALTH, 0, 1, flags)),
			new CombatPotionType(PotionType.STRONG_HEALING, new Potion(PotionEffect.INSTANT_HEALTH, 1, 1, flags)),
			new CombatPotionType(PotionType.HARMING, new Potion(PotionEffect.INSTANT_DAMAGE, 0, 1, flags)),
			new CombatPotionType(PotionType.STRONG_HARMING, new Potion(PotionEffect.INSTANT_DAMAGE, 1, 1, flags)),
			new CombatPotionType(PotionType.POISON, new Potion(PotionEffect.POISON, 0, 900, flags)),
			new CombatPotionType(PotionType.LONG_POISON, new Potion(PotionEffect.POISON, 0, 1800, flags)),
			new CombatPotionType(PotionType.STRONG_POISON, new Potion(PotionEffect.POISON, 1, 432, flags)),
			new CombatPotionType(PotionType.REGENERATION, new Potion(PotionEffect.REGENERATION, 0, 900, flags)),
			new CombatPotionType(PotionType.LONG_REGENERATION, new Potion(PotionEffect.REGENERATION, 0, 1800, flags)),
			new CombatPotionType(PotionType.STRONG_REGENERATION, new Potion(PotionEffect.REGENERATION, 1, 450, flags)),
			new CombatPotionType(PotionType.STRENGTH, new Potion(PotionEffect.STRENGTH, 0, 3600, flags)),
			new CombatPotionType(PotionType.LONG_STRENGTH, new Potion(PotionEffect.STRENGTH, 0, 9600, flags)),
			new CombatPotionType(PotionType.STRONG_STRENGTH, new Potion(PotionEffect.STRENGTH, 1, 1800, flags)),
			new CombatPotionType(PotionType.WEAKNESS, new Potion(PotionEffect.WEAKNESS, 0, 1800, flags)),
			new CombatPotionType(PotionType.LONG_WEAKNESS, new Potion(PotionEffect.WEAKNESS, 0, 4800, flags)),
			new CombatPotionType(PotionType.LUCK, new Potion(PotionEffect.LUCK, 0, 6000, flags)),
			new CombatPotionType(PotionType.SLOW_FALLING, new Potion(PotionEffect.SLOW_FALLING, 0, 1800, flags)),
			new CombatPotionType(PotionType.LONG_SLOW_FALLING, new Potion(PotionEffect.SLOW_FALLING, 0, 4800, flags)),
			new CombatPotionType(PotionType.WIND_CHARGED, new Potion(PotionEffect.WIND_CHARGED, 0, 3600, flags)),
			new CombatPotionType(PotionType.WEAVING, new Potion(PotionEffect.WEAVING, 0, 3600, flags)),
			new CombatPotionType(PotionType.OOZING, new Potion(PotionEffect.OOZING, 0, 3600, flags)),
			new CombatPotionType(PotionType.INFESTED, new Potion(PotionEffect.INFESTED, 0, 3600, flags))
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
			new CombatPotionEffect(
				PotionEffect.STRENGTH,
				Attribute.ATTACK_DAMAGE,
				Key.key("minecraft:effect.strength"),
				3.0, AttributeOperation.ADD_VALUE
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
			new CombatPotionEffect(
				PotionEffect.WEAKNESS,
				Attribute.ATTACK_DAMAGE,
				Key.key("minecraft:effect.weakness"),
				-4.0, AttributeOperation.ADD_VALUE
			),
			new CombatPotionEffect(PotionEffect.POISON),
			new CombatPotionEffect(PotionEffect.WITHER),
			new HealthBoostPotionEffect(),
			new AbsorptionPotionEffect(),
			new CombatPotionEffect(PotionEffect.SATURATION),
			new GlowingPotionEffect(),
			new CombatPotionEffect(PotionEffect.LEVITATION),
			new CombatPotionEffect(
				PotionEffect.LUCK,
				Attribute.LUCK,
				Key.key("minecraft:effect.luck"),
				1.0, AttributeOperation.ADD_VALUE
			),
			new CombatPotionEffect(
				PotionEffect.UNLUCK,
				Attribute.LUCK,
				Key.key("minecraft:effect.unluck"),
				-1.0, AttributeOperation.ADD_VALUE
			),
			new CombatPotionEffect(PotionEffect.SLOW_FALLING),
			new CombatPotionEffect(PotionEffect.CONDUIT_POWER),
			new CombatPotionEffect(PotionEffect.DOLPHINS_GRACE),
			new CombatPotionEffect(PotionEffect.BAD_OMEN),
			new CombatPotionEffect(PotionEffect.HERO_OF_THE_VILLAGE),
			new CombatPotionEffect(PotionEffect.DARKNESS),
			new CombatPotionEffect(PotionEffect.TRIAL_OMEN) {
				@Override
				public Particle getParticle(Potion potion) {
					return Particle.TRIAL_OMEN;
				}
			},
			new CombatPotionEffect(PotionEffect.RAID_OMEN) {
				@Override
				public Particle getParticle(Potion potion) {
					return Particle.RAID_OMEN;
				}
			},
			new CombatPotionEffect(PotionEffect.WIND_CHARGED) {
				@Override
				public Particle getParticle(Potion potion) {
					return Particle.SMALL_GUST;
				}
			},
			new CombatPotionEffect(PotionEffect.WEAVING),
			new CombatPotionEffect(PotionEffect.OOZING),
			new CombatPotionEffect(PotionEffect.INFESTED)
		);
	}
}
