package io.github.togar2.pvp.feature.projectile;

import io.github.togar2.pvp.entity.projectile.FishingBobber;
import io.github.togar2.pvp.events.FishingBobberRetrieveEvent;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Vanilla implementation of {@link FishingRodFeature}
 */
public class VanillaFishingRodFeature implements FishingRodFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaFishingRodFeature> DEFINED = new DefinedFeature<>(
		FeatureType.FISHING_ROD, VanillaFishingRodFeature::new,
		FeatureType.ITEM_DAMAGE, FeatureType.VERSION
	);

	public static final Tag<FishingBobber> FISHING_BOBBER = Tag.Transient("minestompvp:fishing_bobber");

	private final FeatureConfiguration configuration;

	private ItemDamageFeature itemDamageFeature;
	private CombatVersion version;

	public VanillaFishingRodFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void initDependencies() {
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.version = configuration.get(FeatureType.VERSION);
	}

	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerUseItemEvent.class, event -> {
			if (event.getItemStack().material() != Material.FISHING_ROD) return;
			throwEvent(event);
		});
	}

	@SuppressWarnings("MagicNumber")
	protected void throwEvent(PlayerUseItemEvent event) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		Player player = event.getPlayer();

		FishingBobber bobber = player.getTag(FISHING_BOBBER);
		if (bobber != null) {
			final var bobber2 = bobber;
			var retrieveEvent = new FishingBobberRetrieveEvent(player, bobber);
			EventDispatcher.callCancellable(retrieveEvent, () ->
				retrieveEvent(player, bobber2, event.getHand())
			);
			return;
		}

		ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
			SoundEvent.ENTITY_FISHING_BOBBER_THROW, Sound.Source.NEUTRAL,
			0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f)
		), player);

		bobber = new FishingBobber(player, version.legacy());
		player.setTag(FISHING_BOBBER, bobber);

		EntityShootEvent shootEvent = new EntityShootEvent(player, bobber,
			player.getPosition(), 0, 1.0);
		EventDispatcher.call(shootEvent);
		if (shootEvent.isCancelled()) {
			bobber.remove();
			return;
		}
		double spread = shootEvent.getSpread() * (version.legacy() ? 0.0075d : 0.0045d);

		Pos playerPos = player.getPosition();
		float playerPitch = playerPos.pitch();
		float playerYaw = playerPos.yaw();

		float zDir = (float) Math.cos(Math.toRadians(-playerYaw) - Math.PI);
		float xDir = (float) Math.sin(Math.toRadians(-playerYaw) - Math.PI);
		double x = playerPos.x() - xDir * 0.3d;
		double y = playerPos.y() + player.getEyeHeight();
		double z = playerPos.z() - zDir * 0.3d;
		bobber.setInstance(Objects.requireNonNull(player.getInstance()), new Pos(x, y, z));

		Vec velocity;

		if (version.modern()) {
			double yDir = -(Math.sin(Math.toRadians(-playerPitch)) / -Math.cos(Math.toRadians(-playerPitch)));
			velocity = new Vec(-xDir, Math.clamp(yDir, -5.0f, 5.0f), -zDir);

			double length = velocity.length();
			velocity = velocity.mul(
				0.6d / length + 0.5d + random.nextGaussian() * spread,
				0.6d / length + 0.5d + random.nextGaussian() * spread,
				0.6d / length + 0.5d + random.nextGaussian() * spread
			);
		} else {
			double maxVelocity = 0.4f;
			double yaw = playerYaw / 180.0f * Math.PI;
			double pitch = playerPitch / 180.0f * Math.PI;

			velocity = new Vec(
				-Math.sin(yaw) * Math.cos(pitch) * maxVelocity,
				-Math.sin(pitch) * maxVelocity,
				Math.cos(yaw) * Math.cos(pitch) * maxVelocity
			);

			double length = velocity.length();
			velocity = velocity
				.div(length)
				.add(
					random.nextGaussian() * spread,
					random.nextGaussian() * spread,
					random.nextGaussian() * spread
				)
				.mul(1.5d);
		}

		bobber.setVelocity(velocity.mul(ServerFlag.SERVER_TICKS_PER_SECOND));
	}

	protected void retrieveEvent(Player player, FishingBobber bobber, PlayerHand hand) {
		int durability = bobber.retrieve();
		if (player.getGameMode() != GameMode.CREATIVE) {
			itemDamageFeature.damageEquipment(
				player,
				hand == PlayerHand.MAIN ?
					EquipmentSlot.MAIN_HAND :
					EquipmentSlot.OFF_HAND, durability
			);
		}

		ViewUtil.viewersAndSelf(player).playSound(
			Sound.sound(
				SoundEvent.ENTITY_FISHING_BOBBER_RETRIEVE,
				Sound.Source.NEUTRAL,
				1.0f,
				0.4f / (
					ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f
				)
			), player
		);
	}
}
