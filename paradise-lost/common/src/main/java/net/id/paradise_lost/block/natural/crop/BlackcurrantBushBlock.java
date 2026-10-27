package net.id.paradise_lost.block.natural.crop;

import net.id.paradise_lost.registry.BlockRegistry;
import net.id.paradise_lost.registry.ItemRegistry;
import net.id.paradise_lost.util.ParadiseLostSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlackcurrantBushBlock extends SweetBerryBushBlock {

    public BlackcurrantBushBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(ItemRegistry.BLACKCURRANT.get());
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (state.getValue(AGE) > 0 && entity instanceof LivingEntity && entity.getType() != EntityType.FOX && entity.getType() != EntityType.BEE) {
            entity.makeStuckInBlock(state, new Vec3(0.900000011920929D, 0.75D, 0.900000011920929D));
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        int i = state.getValue(AGE);
        if (i > 1) {
            tryPickBerries(world, pos, state);
            return InteractionResult.SUCCESS;
        } else {
            return super.useWithoutItem(state, world, pos, player, hit);
        }
    }

    private void tryPickBerries(Level world, BlockPos pos, BlockState state) {
        boolean mature = state.getValue(AGE) == 3;
        BlockState floor = world.getBlockState(pos.below());
        double mod = floor.is(BlockRegistry.FARMLAND.get()) ? 1.5 : 1;
        int berries = world.getRandom().nextInt(2) + 1;
        popResource(world, pos, new ItemStack(ItemRegistry.BLACKCURRANT.get(), (int) (berries + (mature ? 1 : 0) * mod)));
        world.playSound(null, pos, ParadiseLostSoundEvents.BLOCK_BLACKCURRANT_BUSH_PICK_BLUEBERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + world.getRandom().nextFloat() * 0.4F);
        world.setBlock(pos, state.setValue(AGE, 1), Block.UPDATE_CLIENTS);
    }
}
