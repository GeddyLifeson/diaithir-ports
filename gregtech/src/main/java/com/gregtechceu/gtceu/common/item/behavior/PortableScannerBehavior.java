package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.*;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidVeinSavedData;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.sync_system.ManagedSyncBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.capability.LocalizedHazardSavedData;
import com.gregtechceu.gtceu.common.data.GTSoundEntries;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockFluid;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PortableScannerBehavior implements IInteractionItem, IAddInformation {

    private int debugLevel = 0;

    @Getter
    public enum DisplayMode {

        SHOW_ALL("behavior.portable_scanner.mode.show_all_info"),
        SHOW_BLOCK_INFO("behavior.portable_scanner.mode.show_block_info"),
        SHOW_MACHINE_INFO("behavior.portable_scanner.mode.show_machine_info"),
        SHOW_ELECTRICAL_INFO("behavior.portable_scanner.mode.show_electrical_info"),
        SHOW_RECIPE_INFO("behavior.portable_scanner.mode.show_recipe_info"),
        SHOW_ENVIRONMENTAL_INFO("behavior.portable_scanner.mode.show_environmental_info"),
        SHOW_INTERNAL_JAVA_INFO("behavior.portable_scanner.mode.show_internal_info");

        private final String langKey;

        DisplayMode(String langKey) {
            this.langKey = langKey;
        }
    }

    public PortableScannerBehavior(int debugLevel) {
        this.debugLevel = debugLevel;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var stack = context.getItemInHand();

        if (!level.isClientSide() && !level.isEmptyBlock(pos) && player != null) {

            List<Component> list = new ArrayList<>();
            int energyCost = addScannerInfo(player, level, pos, getMode(stack), list);

            if (player.isCreative()) {
                energyCost = 0;
            }

            if (energyCost > 0 && !drainEnergy(stack, energyCost, true)) {
                player.sendSystemMessage(Component.translatable("behavior.prospector.not_enough_energy"));
                return InteractionResult.CONSUME;
            }

            drainEnergy(stack, energyCost, false);
            for (var line : list) {
                player.sendSystemMessage(line);
            }

            GTSoundEntries.PORTABLE_SCANNER.play(level, null, player.position(), 1.0f, 1.0f);

            return InteractionResult.CONSUME;
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                setNextMode(item);
                var mode = getMode(item);
                player.sendSystemMessage(Component.translatable("behavior.portable_scanner.mode.caption",
                        Component.translatable(mode.getLangKey())));
            }
            return InteractionResult.SUCCESS.heldItemTransformedTo(item);
        }
        return IInteractionItem.super.use(item, level, player, usedHand);
    }

    protected boolean drainEnergy(@NotNull ItemStack stack, int amount, boolean simulate) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;
        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    protected void setNextMode(ItemStack stack) {
        stack.update(GTDataComponents.SCANNER_MODE, (byte) 0,
                mode -> (byte) ((mode + 1) % DisplayMode.values().length));
    }

    @NotNull
    protected DisplayMode getMode(ItemStack stack) {
        if (stack == ItemStack.EMPTY) {
            return DisplayMode.SHOW_ALL;
        }
        return DisplayMode.values()[stack.getOrDefault(GTDataComponents.SCANNER_MODE, (byte) 0) %
                DisplayMode.values().length];
    }

    public int addScannerInfo(Player player, Level level, BlockPos pos, DisplayMode mode, List<Component> list) { return (int)0; /* ported stub */ }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("metaitem.behavior.mode_switch.tooltip"));
        tooltipComponents.add(Component.translatable("behavior.portable_scanner.mode.caption",
                Component.translatable(getMode(stack).getLangKey()).withStyle(ChatFormatting.AQUA)));
    }
}
