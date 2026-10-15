/*
 * This file ("RenderBatteryBox.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.ellpeck.actuallyadditions.mod.items.ItemBattery;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityBatteryBox;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import de.ellpeck.actuallyadditions.mod.util.EnergyCaps;
import de.ellpeck.actuallyadditions.mod.util.Lang;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jspecify.annotations.Nullable;

public class RenderBatteryBox implements BlockEntityRenderer<TileEntityBatteryBox, RenderBatteryBox.State> {
    private final ItemModelResolver itemModelResolver;

    public RenderBatteryBox(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileEntityBatteryBox tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        ItemStack stack = tile.inv.getStackInSlot(0);
        boolean battery = !stack.isEmpty() && stack.getItem() instanceof ItemBattery;
        state.extract(this.itemModelResolver, battery ? stack : ItemStack.EMPTY, tile);
        IEnergyStorage energy = battery ? EnergyCaps.item(stack) : null;
        state.showEnergy = energy != null;
        if (energy != null) {
            Font font = Minecraft.getInstance().font;
            state.energyTotal = Component.literal(Lang.cleanEnergyValues(energy, false)).getVisualOrderText();
            state.energyName = Component.translatable("misc.actuallyadditions.power_name_long").getVisualOrderText();
            state.totalX = -font.width(state.energyTotal) / 2F;
            state.nameX = -font.width(state.energyName) / 2F;
            state.background = (int) (Minecraft.getInstance().options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
        }
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.item.isEmpty()) return;
        if (state.showEnergy) {
            // the battery's charge, written on all four sides of the box
            matrices.pushPose();
            matrices.translate(.5f, .35f, .5f);
            matrices.mulPose(Axis.ZP.rotationDegrees(180));
            matrices.scale(0.0075F, 0.0075F, 0.0075F);
            matrices.translate(0F, 0F, -60F);
            for (int i = 0; i < 4; i++) {
                collector.submitText(matrices, state.totalX, 10F, state.energyTotal, false, Font.DisplayMode.NORMAL, state.lightCoords, 0xFFFFFFFF, state.background, 0);
                collector.submitText(matrices, state.nameX, 20F, state.energyName, false, Font.DisplayMode.NORMAL, state.lightCoords, 0xFFFFFFFF, state.background, 0);
                matrices.translate(-60F, 0F, 60F);
                matrices.mulPose(Axis.YP.rotationDegrees(90));
            }
            matrices.popPose();
        }
        state.submit(matrices, collector);
    }

    public static class State extends AssetUtil.FloatingItemState {
        boolean showEnergy;
        FormattedCharSequence energyTotal = FormattedCharSequence.EMPTY;
        FormattedCharSequence energyName = FormattedCharSequence.EMPTY;
        float totalX;
        float nameX;
        int background;
    }
}
