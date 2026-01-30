package io.github.togar2.pvp.potion.effect;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.PotionEffect;

public class GlowingPotionEffect extends CombatPotionEffect {
	public GlowingPotionEffect() {
		super(PotionEffect.GLOWING);
	}
	
	@Override
	public void apply(LivingEntity entity, int amplifier) {
		entity.setGlowing(true);
	}
	
	@Override
	public void remove(LivingEntity entity, int amplifier) {
		entity.setGlowing(false);
	}
}
