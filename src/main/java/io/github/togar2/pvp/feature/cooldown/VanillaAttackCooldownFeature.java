package io.github.togar2.pvp.feature.cooldown;

import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;

/**
 * Vanilla implementation of {@link AttackCooldownFeature}
 */
public class VanillaAttackCooldownFeature implements AttackCooldownFeature, RegistrableFeature {

	private static final VanillaAttackCooldownFeature INSTANCE_MODERN = new VanillaAttackCooldownFeature(false);
	private static final VanillaAttackCooldownFeature INSTANCE_LEGACY = new VanillaAttackCooldownFeature(true);

	public static final DefinedFeature<VanillaAttackCooldownFeature> MODERN = new DefinedFeature<>(
		FeatureType.ATTACK_COOLDOWN,
		config -> INSTANCE_MODERN
	);

	public static final DefinedFeature<VanillaAttackCooldownFeature> LEGACY = new DefinedFeature<>(
		FeatureType.ATTACK_COOLDOWN,
		config -> INSTANCE_LEGACY
	);

	public static final Tag<Long> LAST_ATTACKED_TICKS = Tag.Transient("minestompvp:ticks_last_attacked");
	
	private final boolean legacy;

	public VanillaAttackCooldownFeature(boolean legacy) {
		this.legacy = legacy;
	}

	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(EventListener.builder(PlayerHandAnimationEvent.class).handler(event ->
				resetCooldownProgress(event.getPlayer())).build());
		
		node.addListener(EventListener.builder(PlayerChangeHeldSlotEvent.class).handler(event -> {
			if (!event.getPlayer().getItemInMainHand()
					.isSimilar(event.getPlayer().getInventory().getItemStack(event.getNewSlot()))) {
				resetCooldownProgress(event.getPlayer());
			}
		}).build());
	}
	
	@Override
	public void resetCooldownProgress(Player player) {
		player.setTag(LAST_ATTACKED_TICKS, player.getAliveTicks());
	}
	
	@Override
	public double getAttackCooldownProgress(Player player) {
		if (legacy) return 1.0;
		
		Long lastAttacked = player.getTag(LAST_ATTACKED_TICKS);
		if (lastAttacked == null) return 1.0;
		
		long timeSinceLastAttacked = player.getAliveTicks() - lastAttacked;
		return MathUtils.clamp(
				(timeSinceLastAttacked + 0.5) / getAttackCooldownProgressPerTick(player),
				0, 1
		);
	}
	
	protected double getAttackCooldownProgressPerTick(Player player) {
		return (1 / player.getAttributeValue(Attribute.ATTACK_SPEED)) * 20;
	}
}
