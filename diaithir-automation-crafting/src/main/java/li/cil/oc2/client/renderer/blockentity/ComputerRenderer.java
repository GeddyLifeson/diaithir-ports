/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer.blockentity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import li.cil.oc2.api.API;
import li.cil.oc2.common.blockentity.ComputerBlockEntity;
import li.cil.oc2.common.vm.terminal.Terminal;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;

import java.time.Duration;

@SuppressWarnings("unused")
@EventBusSubscriber(value = Dist.CLIENT, modid = API.MOD_ID)
public final class ComputerRenderer implements BlockEntityRenderer<ComputerBlockEntity, BlockEntityRenderState> {
    public static final Identifier OVERLAY_POWER_LOCATION = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/computer/computer_overlay_power");
    public static final Identifier OVERLAY_STATUS_LOCATION = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/computer/computer_overlay_status");
    public static final Identifier OVERLAY_TERMINAL_LOCATION = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/computer/computer_overlay_terminal");

    private static final Cache<Terminal, Terminal.RendererView> rendererViews = CacheBuilder.newBuilder()
        .expireAfterAccess(Duration.ofSeconds(5))
        .removalListener(ComputerRenderer::handleNoLongerRendering)
        .build();

    ///////////////////////////////////////////////////////////////////

    public ComputerRenderer(final BlockEntityRendererProvider.Context context) {
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void submit(final BlockEntityRenderState state, final PoseStack stack, final SubmitNodeCollector collector, final CameraRenderState camera) {
        // TODO(oc2-restore): Port terminal/status/power overlay rendering to the 26.1 render state + submit pipeline.
        //  The previous implementation rendered the terminal contents (via Terminal.RendererView), the boot error
        //  text and the status/power/terminal overlay quads using ModRenderType::getUnlitBlock.
    }

    @SubscribeEvent
    public static void updateCache(final ClientTickEvent.Pre event) {
        rendererViews.cleanUp();
    }

    private static void handleNoLongerRendering(final RemovalNotification<Terminal, Terminal.RendererView> notification) {
        final Terminal key = notification.getKey();
        final Terminal.RendererView value = notification.getValue();
        if (key != null && value != null) {
            key.releaseRenderer(value);
        }
    }
}
