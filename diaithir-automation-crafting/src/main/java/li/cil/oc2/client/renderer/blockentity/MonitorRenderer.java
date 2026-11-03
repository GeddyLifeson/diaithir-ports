/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer.blockentity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.oc2.api.API;
import li.cil.oc2.client.renderer.MonitorGUIRenderer;
import li.cil.oc2.common.blockentity.MonitorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.time.Duration;

@SuppressWarnings("unused")
@EventBusSubscriber(value = Dist.CLIENT, modid = API.MOD_ID)
public final class MonitorRenderer implements BlockEntityRenderer<MonitorBlockEntity, BlockEntityRenderState> {
    public static final Identifier OVERLAY_POWER_LOCATION = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/monitor/monitor_overlay_power");
    public static final Identifier OVERLAY_TERMINAL_LOCATION = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/computer/computer_overlay_terminal");

    private static final Cache<MonitorGUIRenderer, MonitorGUIRenderer.RendererView> rendererViews = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(5))
        .removalListener(MonitorRenderer::handleNoLongerRendering)
        .build();

    ///////////////////////////////////////////////////////////////////

    public MonitorRenderer(final BlockEntityRendererProvider.Context context) {
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(final BlockEntityRenderState state, final PoseStack stack, final SubmitNodeCollector collector, final CameraRenderState camera) {
        // TODO(oc2-restore): The monitor rendering (terminal content, status text, power and terminal
        //  overlays) relied on the old immediate-mode render pipeline (RenderSystem blend/depth state,
        //  projection matrix, Material buffers and Font.drawInBatch) which no longer exists in 26.1.
        //  Port to render state extraction + SubmitNodeCollector.
    }

    @SubscribeEvent
    public static void updateCache(final ClientTickEvent.Pre event) {
        rendererViews.cleanUp();
    }

    private static void handleNoLongerRendering(final RemovalNotification<MonitorGUIRenderer, MonitorGUIRenderer.RendererView> notification) {
        final MonitorGUIRenderer key = notification.getKey();
        final MonitorGUIRenderer.RendererView value = notification.getValue();
        if (key != null && value != null) {
            key.releaseRenderer(value);
        }
    }
}
