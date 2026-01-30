package io.github.togar2.pvp.potion.registry;

import io.github.togar2.pvp.potion.effect.CombatPotionEffect;
import io.github.togar2.pvp.potion.item.CombatPotionType;
import net.kyori.adventure.identity.Identity;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

public class CombatPotionRegistryImpl implements CombatPotionRegistry {

	@Unmodifiable
	private final Map<PotionType, CombatPotionType> types;
	@Unmodifiable
	private final Map<PotionEffect, CombatPotionEffect> effects;

	public CombatPotionRegistryImpl(
		Map<PotionType, CombatPotionType> types,
		Map<PotionEffect, CombatPotionEffect> effects
	) {
		this.types = new IdentityHashMap<>(Objects.requireNonNull(types));
		this.effects = new IdentityHashMap<>(Objects.requireNonNull(effects));
	}

	@Override
	public @Nullable CombatPotionType getType(PotionType potionType) {
		return types.get(potionType);
	}

	@Override
	public @Nullable CombatPotionEffect getEffect(PotionEffect potionEffect) {
		return effects.get(potionEffect);
	}

	public Map<PotionType, CombatPotionType> getTypes() {
		return types;
	}

	public Map<PotionEffect, CombatPotionEffect> getEffects() {
		return effects;
	}
}
