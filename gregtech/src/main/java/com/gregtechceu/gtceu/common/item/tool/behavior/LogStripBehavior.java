package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LogStripBehavior implements IToolBehavior<LogStripBehavior> {

    public static final LogStripBehavior INSTANCE = new LogStripBehavior();
    public static final Codec<LogStripBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, LogStripBehavior> STREAM_CODEC = StreamCodec
            .unit(INSTANCE);

    protected LogStripBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return action == ItemAbilities.AXE_STRIP;
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) { return null; /* ported stub */ }

    public static List<BlockPos> getStrippableBlocks(AoESymmetrical aoeDefinition, UseOnContext context) {
        return ToolHelper.iterateAoE(aoeDefinition, LogStripBehavior::isBlockStrippable, context);
    }

    protected static boolean isBlockStrippable(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockState newState = state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, true);
        return newState != null && newState != state;
    }

    protected BlockState getStripped(BlockState state, UseOnContext context) {
        return state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext Level, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.strip_log"));
    }

    @Override
    public ToolBehaviorType<LogStripBehavior> getType() {
        return GTToolBehaviors.STRIP_LOG;
    }
}
