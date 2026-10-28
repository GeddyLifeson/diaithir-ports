package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.item.datacomponents.LargeItemContent;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.PoseStackExtensions;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.machine.storage.CreativeChestMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumChestMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import static com.gregtechceu.gtceu.utils.GTMatrixUtils.*;

@ExtensionMethod(PoseStackExtensions.class)
public class QuantumChestItemRender extends DynamicRender<QuantumChestMachine, QuantumChestItemRender> {

    // spotless:off
    public static final MapCodec<QuantumChestItemRender> CODEC = MapCodec.unit(QuantumChestItemRender::new);
    public static final DynamicRenderType<QuantumChestMachine, QuantumChestItemRender> TYPE = new DynamicRenderType<>(QuantumChestItemRender.CODEC);
    // spotless:on

    private static @Nullable Item CREATIVE_CHEST_ITEM = null;

    public QuantumChestItemRender() {}

    @Override
    public DynamicRenderType<QuantumChestMachine, QuantumChestItemRender> getType() {
        return TYPE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {  /* ported stub */ }

    @Override
    public void render(QuantumChestMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        poseStack.pushPose();
        setupModelRotation(machine, poseStack);

        var totalTick = machine.getLevel().getGameTime() + partialTick;
        renderChestItem(poseStack, buffer, totalTick, machine.getFrontFacing(),
                machine.getStored(), machine.getStoredAmount(), machine.getLockedItem(),
                machine instanceof CreativeChestMachine);
        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderChestItem(PoseStack poseStack, MultiBufferSource buffer, float totalTick, Direction frontFacing,
                                ItemStack stored, long storedAmount, ItemStack locked, boolean isCreative) {  /* ported stub */ }

    public static void setupModelRotation(MetaMachine machine, PoseStack poseStack) {
        var frontFacing = machine.getFrontFacing();
        var upwardFacing = machine.getUpwardsFacing();

        poseStack.translate(0.5f, 0.5f, 0.5f);
        rotateMatrix(poseStack.last().pose(),
                upwardFacingAngle(upwardFacing) + (upwardFacing.getAxis() == Direction.Axis.X ? Mth.PI : 0),
                getDirectionAxis(frontFacing));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
    }

    public static void drawAmountText(PoseStack poseStack, MultiBufferSource buffer, Direction frontFacing,
                                      long storedAmount, boolean isCreative) {  /* ported stub */ }
}
