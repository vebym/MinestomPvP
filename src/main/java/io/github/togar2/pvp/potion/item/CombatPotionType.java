package io.github.togar2.pvp.potion.item;

import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

@NotNullByDefault
public record CombatPotionType(
	PotionType potionType,
	@Unmodifiable
	List<Potion> effects
) {

	public static final CombatPotionType NO_OP = new CombatPotionType(PotionType.MUNDANE);

	public CombatPotionType(PotionType potionType, Potion... effects) {
		this(Objects.requireNonNull(potionType), List.of(effects));
	}

	public PotionType getPotionType() {
		return potionType;
	}

	@Unmodifiable
	public List<Potion> getEffects() {
		//noinspection AssignmentOrReturnOfFieldWithMutableType
		return effects;
	}
}
