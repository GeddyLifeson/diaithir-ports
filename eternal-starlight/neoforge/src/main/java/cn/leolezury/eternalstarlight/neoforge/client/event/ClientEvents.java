package cn.leolezury.eternalstarlight.neoforge.client.event;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.handler.ESClientHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = EternalStarlight.ID, value = Dist.CLIENT)
public class ClientEvents {
	@SubscribeEvent
	private static void onClientTick(ClientTickEvent.Post event) {
		ESClientHandler.onClientTick();
	}

	@SubscribeEvent
	private static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
		Vec3 angle = ESClientHandler.onComputeCameraAngles(new Vec3(event.getPitch(), event.getYaw(), event.getRoll()));
		event.setPitch((float) angle.x);
		event.setYaw((float) angle.y);
	}

	@SubscribeEvent
	private static void onComputeFovModifier(ComputeFovModifierEvent event) {
		ESClientHandler.onComputeFovModifier(event.getFovModifier()).ifPresent(d -> event.setNewFovModifier((float) d));
	}

	@SubscribeEvent
	private static void onRenderFog(ViewportEvent.RenderFog event) {
		float[] distances = ESClientHandler.onRenderFog(event.getCamera(), event.getType(), event.getNearPlaneDistance(), event.getFarPlaneDistance());
		event.setNearPlaneDistance(distances[0]);
		event.setFarPlaneDistance(distances[1]);
	}

	@SubscribeEvent
	private static void onRenderBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
		if (ESClientHandler.renderBossBar(event.getGuiGraphics(), event.getBossEvent(), event.getX(), event.getY())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	private static void onSubmitCustomGeometry(SubmitCustomGeometryEvent event) {
		ESClientHandler.onSubmitCustomGeometry(event.getSubmitNodeCollector(), event.getPoseStack(), Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
	}

	@SubscribeEvent
	private static void onRenderBlockScreenEffect(RenderBlockScreenEffectEvent event) {
		if (!event.isCanceled()) {
			boolean allow = ESClientHandler.onRenderBlockOverlay(event.getPlayer(), event.getBlockState());
			if (!allow) {
				event.setCanceled(true);
			}
		}
	}
}
