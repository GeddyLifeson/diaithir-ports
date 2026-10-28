package com.gregtechceu.gtceu.common.item.tool.behavior;

import com.gregtechceu.gtceu.api.item.datacomponents.AoESymmetrical;
import com.gregtechceu.gtceu.api.item.tool.ToolHelper;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.item.tool.behavior.ToolBehaviorType;
import com.gregtechceu.gtceu.common.data.GTToolBehaviors;
import com.gregtechceu.gtceu.common.data.item.GTItemAbilities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbility;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HarvestCropsBehavior implements IToolBehavior<HarvestCropsBehavior> {

    public static final HarvestCropsBehavior INSTANCE = new HarvestCropsBehavior();
    public static final Codec<HarvestCropsBehavior> CODEC = Codec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, HarvestCropsBehavior> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    protected HarvestCropsBehavior() {/**/}

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility action) {
        return action == GTItemAbilities.HOE_HARVEST;
    }

    @NotNull
    @Override
    public InteractionResult onItemUse(UseOnContext context) { return null; /* ported stub */ }

    private static boolean isBlockCrops(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.getBlockState(pos.above()).isAir()) {
            Block block = level.getBlockState(pos).getBlock();
            return block instanceof CropBlock;
        }
        return false;
    }

    private static boolean harvestBlockRoutine(BlockPos pos, UseOnContext context) { return false; /* ported stub */ }

    @Override
    public void addInformation(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip,
                               @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.gtceu.tool.behavior.crop_harvesting"));
    }

    @Override
    public ToolBehaviorType<HarvestCropsBehavior> getType() {
        return GTToolBehaviors.HARVEST_CROPS;
    }
}
