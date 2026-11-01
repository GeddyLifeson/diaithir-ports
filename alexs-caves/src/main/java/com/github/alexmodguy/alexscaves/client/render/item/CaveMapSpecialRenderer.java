package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexmodguy.alexscaves.client.render.misc.CaveMapRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/** Special item model "alexscaves:cave_map": draws the filled cave map texture. */
public class CaveMapSpecialRenderer implements SpecialModelRenderer<ItemStack> {
    public static final MapCodec<Unbaked> CODEC = MapCodec.unit(new Unbaked());

    public static class Unbaked implements SpecialModelRenderer.Unbaked<ItemStack> {
        @Override
        public @Nullable SpecialModelRenderer<ItemStack> bake(BakingContext context) {
            return new CaveMapSpecialRenderer();
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<ItemStack>> type() {
            return CODEC;
        }
    }

    @Override
    public void submit(@Nullable ItemStack stack, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean foil, int outline) {
        if (stack == null || net.minecraft.client.Minecraft.getInstance().level == null) return;
        CaveMapRenderer.getMapFor(stack).submit(poseStack, collector.order(0), light);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new Vector3f(0, 0, 0.5F));
        output.accept(new Vector3f(1, 1, 0.5F));
    }

    @Override
    public @Nullable ItemStack extractArgument(ItemStack stack) {
        return stack;
    }
}
