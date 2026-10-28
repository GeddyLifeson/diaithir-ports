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
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScrapeBehavior implements IToolBehavior<ScrapeBehavior> {

    public static final ScrapeBehavior INSTANCE = new ScrapeBehavior();
    public static final Codec<ScrapeBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, ScrapeBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected ScrapeBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return action == ItemAbilities.AXE_SCRAPE;
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) { return null; /* ported stub */ }

    public static List<BlockPos> getScrapableBlocks(AoESymmetrical aoeDefinition, UseOnContext context) {
        return ToolHelper.iterateAoE(aoeDefinition, ScrapeBehavior::isBlockScrapable, context);
    }

    protected static boolean isBlockScrapable(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockState newState = state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, true);
        return newState != null && newState != state;
    }

    protected BlockState getScraped(BlockState state, UseOnContext context) {
        return state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false);
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext Level, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.scrape"));
    }

    @Override
    public ToolBehaviorType<ScrapeBehavior> getType() {
        return GTToolBehaviors.SCRAPE;
    }
}
