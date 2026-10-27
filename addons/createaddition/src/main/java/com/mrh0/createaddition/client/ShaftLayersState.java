package com.mrh0.createaddition.client;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/** Render state of a machine with several independently turning/translated partial models. */
public class ShaftLayersState extends BlockEntityRenderState {
    public record Layer(SuperByteBufferRenderState model, @Nullable Quaternionf yaw, float ty, @Nullable Quaternionf spin) {}

    public final List<Layer> layers = new ArrayList<>();

    public void submit(PoseStack matrices, SubmitNodeCollector queue) {
        for (Layer l : layers) {
            matrices.pushPose();
            if (l.yaw != null) matrices.rotateAround(l.yaw, 0.5f, 0.5f, 0.5f);
            if (l.ty != 0) matrices.translate(0, l.ty, 0);
            if (l.spin != null) matrices.rotateAround(l.spin, 0.5f, 0.5f, 0.5f);
            l.model.submit(matrices, queue);
            matrices.popPose();
        }
    }
}
