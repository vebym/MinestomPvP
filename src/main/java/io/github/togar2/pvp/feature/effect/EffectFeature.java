package io.github.togar2.pvp.feature.effect;

import io.github.togar2.pvp.entity.projectile.Arrow;
import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Combat feature which manages potion effects and their effects on entities.
 */
public interface EffectFeature extends CombatFeature {
	EffectFeature NO_OP = new EffectFeature() {
		@Override
		public int getPotionColor(PotionContents contents) {
			return 0;
		}
		
		@Override
		public List<Potion> getAllPotions(PotionType potionType, Collection<CustomPotionEffect> customEffects) {
			return List.of();
		}
		
		@Override public void updatePotionVisibility(LivingEntity entity) {}
		@Override public void addArrowEffects(LivingEntity entity, Arrow arrow) {}
		@Override public void addSplashPotionEffects(LivingEntity target, PotionContents potionContents,
									   @Nullable Entity source, @Nullable Entity attacker, boolean directHit) {}
	};
	
	int getPotionColor(PotionContents contents);
	
	default List<Potion> getAllPotions(@Nullable PotionContents potionContents) {
		if (potionContents == null) return List.of();
		return getAllPotions(potionContents.potion(), potionContents.customEffects());
	}
	
	List<Potion> getAllPotions(PotionType potionType, Collection<CustomPotionEffect> customEffects);
	
	/**
	 * Updates the potion visibility of an entity. This includes particles and invisibility status.
	 *
	 * @param entity the entity to update the potion visibility of
	 */
	void updatePotionVisibility(LivingEntity entity);
	
	/**
	 * Applies the effects of a (tipped) arrow to an entity.
	 *
	 * @param entity the entity which was hit
	 * @param arrow the arrow
	 */
	void addArrowEffects(LivingEntity entity, Arrow arrow);
	
	/**
	 * Applies the effects of a splash potion to an entity.
	 *
	 * @param target         the entity which was hit
	 * @param potionContents the potion contents of the splash potion
	 * @param source         the direct source of the splash (usually the splash potion)
	 * @param attacker       the attacker of the splash (usually the thrower)
	 * @param directHit      if the potion landed on the target
	 */
	void addSplashPotionEffects(
		LivingEntity target,
		PotionContents potionContents,
		Entity source,
		@Nullable Entity attacker,
		boolean directHit
	);
}
