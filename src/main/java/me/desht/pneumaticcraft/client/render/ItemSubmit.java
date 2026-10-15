package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.render.legacy.RecordingBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Stand-in for the removed ItemRenderer.renderStatic: resolves the item model and submits it to the collector. */
public class ItemSubmit {
    public static void render(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay) {
        if (stack.isEmpty()) return;
        ItemStackRenderState state = new ItemStackRenderState();
        Minecraft mc = Minecraft.getInstance();
        mc.getItemModelResolver().updateForTopItem(state, stack, ctx, mc.level, null, 0);
        state.submit(poseStack, collector, light, overlay, 0);
    }

    /** Only a RecordingBufferSource (from LegacyBlockEntityRenderer) reaches the submit pipeline; other buffers draw nothing. */
    public static void render(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (buffer instanceof RecordingBufferSource recording) {
            render(stack, ctx, poseStack, recording.collector(), light, overlay);
        }
    }
}
