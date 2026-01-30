package io.github.togar2.pvp.potion.effect;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.PotionEffect;

public class AbsorptionPotionEffect extends CombatPotionEffect {

	public AbsorptionPotionEffect() {
		super(PotionEffect.ABSORPTION);
	}
	
	@Override
	public void apply(LivingEntity entity, int amplifier) {
		if (entity instanceof Player player) {
			player.setAdditionalHearts(player.getAdditionalHearts() + (4 * (amplifier + 1)));
		}
		
		super.apply(entity, amplifier);
	}
	
	@Override
	public void remove(LivingEntity entity, int amplifier) {
		if (entity instanceof Player player) {
			player.setAdditionalHearts(
				Math.max(0, player.getAdditionalHearts() - (4 * (amplifier + 1)))
			);
		}
		
		super.remove(entity, amplifier);
	}
}
