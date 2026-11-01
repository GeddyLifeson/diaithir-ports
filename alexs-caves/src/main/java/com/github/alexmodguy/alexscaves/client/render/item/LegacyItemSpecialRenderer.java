package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext;
import com.github.alexthe666.citadel.client.render.legacy.RecordingBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/** Special item model "alexscaves:legacy_item": draws the 1.21 ISTER body through the Citadel recording bridge. */
public class LegacyItemSpecialRenderer implements SpecialModelRenderer<ItemStack> {
    private static final ACLegacyItemRenderer RENDERER = new ACLegacyItemRenderer();

    public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

    public static class Unbaked implements SpecialModelRenderer.Unbaked<ItemStack> {
        @Override
        public @Nullable SpecialModelRenderer<ItemStack> bake(BakingContext context) {
            return new LegacyItemSpecialRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<ItemStack>> type() {
            return CODEC;
        }
    }

    @Override
    public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
        if (stack == null) return;
        RecordingBufferSource rec = new RecordingBufferSource();
        LegacyRenderContext.beginPublic(collector);
        poseStack.pushPose();
        try {
            RENDERER.renderByItem(stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, poseStack, rec, light, overlay);
        } finally {
            poseStack.popPose();
            LegacyRenderContext.endPublic();
        }
        rec.flush(collector);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0));
        output.accept(new Vector3f(1, 1, 1));
    }

    @Override
    public @Nullable ItemStack extractArgument(ItemStack stack) {
        return stack;
    }
}
