package com.gregtechceu.gtceu.client.renderer.machine.impl;

import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.item.datacomponents.LargeFluidContent;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRender;
import com.gregtechceu.gtceu.client.renderer.machine.DynamicRenderType;
import com.gregtechceu.gtceu.client.util.RenderBufferHelper;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.machine.storage.CreativeTankMachine;
import com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine;

import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.MapCodec;

import java.util.EnumSet;

import javax.annotation.Nullable;

import static com.gregtechceu.gtceu.client.renderer.machine.impl.QuantumChestItemRender.*;
import static com.gregtechceu.gtceu.common.machine.storage.QuantumTankMachine.TANK_CAPACITY;

public class QuantumTankFluidRender extends DynamicRender<QuantumTankMachine, QuantumTankFluidRender> {

    // spotless:off
    public static final MapCodec<QuantumTankFluidRender> CODEC = MapCodec.unit(QuantumTankFluidRender::new);
    public static final DynamicRenderType<QuantumTankMachine, QuantumTankFluidRender> TYPE = new DynamicRenderType<>(QuantumTankFluidRender.CODEC);
    // spotless:on

    private static final float MIN = 0.16f;
    private static final float MAX = 0.84f;

    private static @Nullable Item CREATIVE_FLUID_ITEM = null;

    public QuantumTankFluidRender() {}

    @Override
    public DynamicRenderType<QuantumTankMachine, QuantumTankFluidRender> getType() {
        return TYPE;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                             PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (CREATIVE_FLUID_ITEM == null) CREATIVE_FLUID_ITEM = GTMachines.CREATIVE_FLUID.getItem();

        LargeFluidContent content = stack.get(GTDataComponents.LARGE_FLUID_CONTENT);
        if (content != null) {
            poseStack.pushPose();

            FluidStack stored = content.stored();
            long storedAmount = content.amount();
            if (storedAmount == 0 && !stored.isEmpty()) storedAmount = stored.getAmount();
            long maxAmount = 0;
            if (stack.getItem() instanceof MetaMachineItem machineItem) {
                maxAmount = TANK_CAPACITY.getLong(machineItem.getDefinition());
            }
            // Don't need to handle locked fluids here since they don't get saved to the item
            renderTank(poseStack, buffer, Direction.NORTH,
                    stored, storedAmount, maxAmount, FluidStack.EMPTY,
                    stack.is(CREATIVE_FLUID_ITEM));

            poseStack.popPose();
        }
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void render(QuantumTankMachine machine, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        poseStack.pushPose();
        setupModelRotation(machine, poseStack);

        renderTank(poseStack, buffer, machine.getFrontFacing(),
                machine.getStored(), machine.getStoredAmount(), machine.getMaxAmount(), machine.getLockedFluid(),
                machine instanceof CreativeTankMachine);

        poseStack.popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderTank(PoseStack poseStack, MultiBufferSource buffer, Direction frontFacing,
                           FluidStack stored, long storedAmount, long maxAmount, FluidStack locked,
                           boolean isCreative) {  /* ported stub */ }
}
