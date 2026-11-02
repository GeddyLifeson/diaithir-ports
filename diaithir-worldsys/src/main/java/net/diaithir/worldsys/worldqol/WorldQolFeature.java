package net.diaithir.worldsys.worldqol;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.BlockTags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;

/** Small world quality-of-life tweaks: farmland protection, sapling rescue, sneak harvest. */
public final class WorldQolFeature {
    private WorldQolFeature() {}

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(BlockEvent.FarmlandTrampleEvent.class, e -> e.setCanceled(true));
        NeoForge.EVENT_BUS.addListener(EntityLeaveLevelEvent.class, WorldQolFeature::leave);
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, WorldQolFeature::harvest);
    }

    private static void leave(EntityLeaveLevelEvent e) {
        if (!(e.getEntity() instanceof ItemEntity item) || !(item.level() instanceof ServerLevel level)) return;
        if (item.getRemovalReason() != net.minecraft.world.entity.Entity.RemovalReason.DISCARDED || item.getAge() < 5900) return;
        ItemStack stack = item.getItem();
        if (!(stack.getItem() instanceof BlockItem bi) || !(bi.getBlock() instanceof SaplingBlock)) return;
        BlockPos pos = item.blockPosition();
        if (!level.getBlockState(pos).isAir() || !level.getBlockState(pos.below()).is(BlockTags.DIRT)) return;
        level.setBlock(pos, bi.getBlock().defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void harvest(PlayerInteractEvent.RightClickBlock e) {
        if (e.getHand() != InteractionHand.MAIN_HAND || !e.getEntity().isShiftKeyDown() || !e.getItemStack().isEmpty()) return;
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = e.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(state)) return;
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, e.getEntity(), ItemStack.EMPTY);
        ItemStack seed = new ItemStack(crop.asItem());
        boolean removed = false;
        for (ItemStack drop : drops) {
            if (!removed && drop.is(seed.getItem())) {
                drop.shrink(1);
                removed = true;
            }
            if (!drop.isEmpty()) Block.popResource(level, pos, drop);
        }
        level.setBlock(pos, crop.getStateForAge(0), Block.UPDATE_ALL);
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
    }
}
