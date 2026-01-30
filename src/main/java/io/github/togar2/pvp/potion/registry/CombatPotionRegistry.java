package io.github.togar2.pvp.potion.registry;

import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.potion.effect.CombatPotionEffect;
import io.github.togar2.pvp.potion.item.CombatPotionType;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface CombatPotionRegistry {

	static CombatPotionRegistry of(
		Map<PotionType, CombatPotionType> types,
		Map<PotionEffect, CombatPotionEffect> effects
	) {
		return new CombatPotionRegistryImpl(types, effects);
	}

	static CombatPotionRegistry of(
		List<CombatPotionType> types,
		List<CombatPotionEffect> effects
	) {
		return of(
			types.stream().collect(Collectors.toMap(
				CombatPotionType::getPotionType,
				Function.identity()
			)),
			effects.stream()
				.collect(Collectors.toMap(
					CombatPotionEffect::getPotionEffect,
					Function.identity()
				))
		);
	}

	/**
	 *
	 * @return the implementation of this type of potion
	 * @see net.minestom.server.component.DataComponents#POTION_CONTENTS
	 */
	@Nullable CombatPotionType getType(PotionType potionType);

	/**
	 * Get the {@link CombatPotionEffect} from its {@link PotionEffect}
	 * @see net.minestom.server.entity.Entity#getActiveEffects()
	 * @see net.minestom.server.potion.Potion#effect()
	 */
	@Nullable CombatPotionEffect getEffect(PotionEffect potionEffect);

}
