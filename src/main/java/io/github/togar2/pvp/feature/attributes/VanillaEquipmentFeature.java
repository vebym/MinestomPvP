package io.github.togar2.pvp.feature.attributes;

import io.github.togar2.pvp.enums.ArmorMaterial;
import io.github.togar2.pvp.enums.Tool;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemStack;

/**
 * Vanilla implementation of {@link EquipmentFeature}
 */
public class VanillaEquipmentFeature implements EquipmentFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaEquipmentFeature> MODERN = new DefinedFeature<>(
		FeatureType.EQUIPMENT,
		config -> new VanillaEquipmentFeature(config, false)
	);

	public static final DefinedFeature<VanillaEquipmentFeature> LEGACY = new DefinedFeature<>(
		FeatureType.EQUIPMENT,
		config -> new VanillaEquipmentFeature(config, true)
	);

	//TODO this probably shouldn't work this way
	// We probably want to store all the tools & armor separately per DataFeature
	private final boolean legacy;
	private final FeatureConfiguration configuration;

	public VanillaEquipmentFeature(FeatureConfiguration configuration, boolean legacy) {
		this.configuration = configuration;
		this.legacy = legacy;
	}

	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(EntityEquipEvent.class, this::onEquip);
		node.addListener(PlayerChangeHeldSlotEvent.class, event -> {
			LivingEntity entity = event.getPlayer();
			ItemStack newItem = event.getPlayer().getInventory().getItemStack(event.getNewSlot());
			Tool.updateEquipmentAttributes(entity, entity.getEquipment(EquipmentSlot.MAIN_HAND), newItem, EquipmentSlot.MAIN_HAND, legacy);
		});
	}
	
	protected void onEquip(EntityEquipEvent event) {
		if (!(event.getEntity() instanceof LivingEntity entity)) return;
		
		EquipmentSlot slot = event.getSlot();
		if (slot.isArmor()) {
			ArmorMaterial.updateEquipmentAttributes(entity, entity.getEquipment(slot), event.getEquippedItem(), slot, legacy);
		} else if (slot.isHand()) {
			Tool.updateEquipmentAttributes(entity, entity.getEquipment(slot), event.getEquippedItem(), slot, legacy);
		}
	}
}
