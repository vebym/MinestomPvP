package io.github.togar2.pvp.potion.effect;

import net.kyori.adventure.key.Key;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.potion.PotionEffect;

public class HealthBoostPotionEffect extends CombatPotionEffect {

	public HealthBoostPotionEffect() {
		super(
			PotionEffect.HEALTH_BOOST,
			Attribute.MAX_HEALTH,
			Key.key("minecraft:effect.health_boost"),
			4.0d, AttributeOperation.ADD_VALUE
		);
	}
	
	public void remove(LivingEntity entity, int amplifier) {
		super.remove(entity, amplifier);
		
		if (entity.getHealth() > entity.getAttributeValue(Attribute.MAX_HEALTH)) {
			entity.setHealth((float) entity.getAttributeValue(Attribute.MAX_HEALTH));
		}
	}
}
