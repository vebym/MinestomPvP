package io.github.togar2.pvp.feature.effect;

import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.potion.effect.CombatPotionEffect;
import io.github.togar2.pvp.potion.effect.CombatPotionEffects;
import io.github.togar2.pvp.potion.registry.VanillaPotionRegistry_1_8_9;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class VanillaEffectFeature_1_8_9 extends VanillaEffectFeature {

	public VanillaEffectFeature_1_8_9(FeatureConfiguration configuration) {
		super(configuration, VanillaPotionRegistry_1_8_9.REGISTRY_LENIENT);
	}

	@Override
	public void addSplashPotionEffects(
		LivingEntity target,
		PotionContents potionContents,
		Entity source,
		@Nullable Entity attacker,
		boolean directHit
	) {
		// Distance from feet // TODO
		double distanceSquared = Objects.requireNonNull(source)
			.getDistanceSquared(target.getPosition());

		double proximity = directHit ? 1.0 : (1.0 - Math.sqrt(distanceSquared) / 4.0);

		for (Potion potion : getAllPotions(potionContents)) {
			CombatPotionEffect combatPotionEffect = CombatPotionEffects.get(potion.effect());
			if (combatPotionEffect.isInstant()) {
				combatPotionEffect.applyInstantEffect(
					source, attacker, target,
					potion.amplifier(), proximity,
					exhaustionFeature, foodFeature
				);
			} else {
				// legacy floors the duration
				int duration = (int) Math.floor(potion.duration() * 0.75d);
				duration = (int) (proximity * duration + 0.5);

				if (duration > 20) {
					target.addEffect(new Potion(potion.effect(), potion.amplifier(), duration, potion.flags()));
				}
			}
		}
	}

}
