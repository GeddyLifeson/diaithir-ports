package com.gregtechceu.gtceu.common.item.behavior;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IAddInformation;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.*;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.common.data.item.GTDataComponents;
import com.gregtechceu.gtceu.common.machine.owner.MachineOwner;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import joptsimple.internal.Strings;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MachineConfigCopyBehaviour implements IInteractionItem, IAddInformation {

    private static final String NONE_DIRECTION = "null";
    private static final String CONFIG_DATA = "config_data";
    private static final String COPY_SOURCE = "copy_source";
    private static final String ITEMS_TO_PASTE = "items_to_paste";

    private static final Component ENABLED = Component.translatable("behaviour.memory_card.enabled");
    private static final Component DISABLED = Component.translatable("behaviour.memory_card.disabled");
    private static final String[] DIRECTION_STRINGS = { "§eDown§r", "§eUp§r", "§eNorth§r", "§eSouth§r", "§eWest§r",
            "§eEast§r" };

    private static String directionToString(@Nullable Direction direction) {
        if (direction == null) return NONE_DIRECTION;
        return direction.getName();
    }

    private static @Nullable Direction stringToDirection(@Nullable String str) {
        if (Strings.isNullOrEmpty(str) || NONE_DIRECTION.equalsIgnoreCase(str)) return null;
        return Direction.byName(str);
    }

    private static Component directionListComponent(int directions) {
        List<String> dirStrings = new ArrayList<>();
        if ((directions & (1)) > 0) dirStrings.add(DIRECTION_STRINGS[0]);
        if ((directions & (1 << 1)) > 0) dirStrings.add(DIRECTION_STRINGS[1]);
        if ((directions & (1 << 2)) > 0) dirStrings.add(DIRECTION_STRINGS[2]);
        if ((directions & (1 << 3)) > 0) dirStrings.add(DIRECTION_STRINGS[3]);
        if ((directions & (1 << 4)) > 0) dirStrings.add(DIRECTION_STRINGS[4]);
        if ((directions & (1 << 5)) > 0) dirStrings.add(DIRECTION_STRINGS[5]);
        return Component.literal(String.join(", ", dirStrings));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) { return null; /* ported stub */ }

    //// Logic for actual config options

    // NBT keys for machine config values
    private static final String PIPE_CONNECTIONS = "pipe_connections";
    private static final String PIPE_BLOCKED_CONNECTIONS = "pipe_blocked_connections";

    private static final String COVER = "cover";
    private static final String FACING_DIR = "front_facing";

    private static final String ITEM_OUTPUT_SIDE = "output_direction_item";
    private static final String ITEM_AUTO_OUTPUT = "item_auto_output";
    private static final String ALLOW_ITEM_IN_FROM_OUT = "allow_input_from_output_item";

    private static final String FLUID_OUTPUT_SIDE = "output_direction_fluid";
    private static final String FLUID_AUTO_OUTPUT = "fluid_auto_output";
    private static final String ALLOW_FLUID_IN_FROM_OUT = "allow_input_from_output_fluid";

    private static final String MUFFLED = "muffled";
    private static final String CIRCUIT = "circuit_config";

    private static CompoundTag gatherPipeConfig(PipeBlockEntity<?, ?> pipe) {
        var tag = new CompoundTag();

        tag.putInt(PIPE_CONNECTIONS, pipe.getConnections());
        tag.putInt(PIPE_BLOCKED_CONNECTIONS, pipe.getBlockedConnections());

        return tag;
    }

    private static void pastePipeConfig(ServerPlayer player, PipeBlockEntity<?, ?> pipe, CompoundTag tag) {  /* ported stub */ }

    private static CompoundTag gatherMachineConfig(MetaMachine machine) {
        var tag = new CompoundTag();

        tag.putString(FACING_DIR, directionToString(machine.getFrontFacing()));

        var outputTrait = machine.getTraitHolder().getTrait(AutoOutputTrait.TYPE);
        if (outputTrait != null && outputTrait.supportsAutoOutputItems() &&
                outputTrait.getItemOutputDirection() != null) {
            tag.putString(ITEM_OUTPUT_SIDE, directionToString(outputTrait.getItemOutputDirection()));
            tag.putBoolean(ITEM_AUTO_OUTPUT, outputTrait.isAutoOutputItems());
            tag.putBoolean(ALLOW_ITEM_IN_FROM_OUT, outputTrait.allowsItemInputFromOutputSide());
        }

        if (outputTrait != null && outputTrait.supportsAutoOutputFluids() &&
                outputTrait.getFluidOutputDirection() != null) {
            tag.putString(FLUID_OUTPUT_SIDE, directionToString(outputTrait.getFluidOutputDirection()));
            tag.putBoolean(FLUID_AUTO_OUTPUT, outputTrait.isAutoOutputFluids());
            tag.putBoolean(ALLOW_FLUID_IN_FROM_OUT, outputTrait.allowsFluidInputFromOutputSide());
        }

        if (machine instanceof IMufflableMachine mufflableMachine) {
            tag.putBoolean(MUFFLED, mufflableMachine.isMuffled());
        }

        if (machine instanceof IHasCircuitSlot circuitMachine) {
            var circuit = IntCircuitBehaviour
                    .getCircuitConfiguration(circuitMachine.getCircuitInventory().getStackInSlot(0));
            if (circuitMachine.isCircuitSlotEnabled() && circuit != 0) {
                tag.putInt(CIRCUIT, circuit);
            }
        }

        tag.put(COVER, machine.getCoverContainer().copyConfig(new CompoundTag()));

        tag = machine.copyConfig(tag);

        return tag;
    }

    private static void pasteMachineConfig(ServerPlayer player, MetaMachine machine, CompoundTag tag) {  /* ported stub */ }

    private static void addConfigTooltips(List<Component> tooltip, CompoundTag tag, Item.TooltipContext context) {  /* ported stub */ }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {  /* ported stub */ }
}
