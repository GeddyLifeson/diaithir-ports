package com.gregtechceu.gtceu.common.block.explosive;

import com.gregtechceu.gtceu.common.entity.GTExplosiveEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public abstract class GTExplosiveBlock extends Block {

    private final boolean canRedstoneActivate;
    private final boolean explodeOnMine;
    private final int fuseLength;

    /**
     * @param canRedstoneActivate whether redstone signal can prime this explosive
     * @param explodeOnMine       whether mining this block should prime it (sneak mine to drop normally)
     * @param fuseLength          explosion countdown after priming. Vanilla TNT is 80.
     */
    public GTExplosiveBlock(BlockBehaviour.Properties properties, boolean canRedstoneActivate, boolean explodeOnMine,
                            int fuseLength) {
        super(properties.isValidSpawn((state, level, pos, ent) -> false).explosionResistance(1.0f));
        this.canRedstoneActivate = canRedstoneActivate;
        this.explodeOnMine = explodeOnMine;
        this.fuseLength = fuseLength;
    }

    protected abstract GTExplosiveEntity createEntity(@NotNull Level world, @NotNull BlockPos pos,
                                                      @Nullable LivingEntity exploder);

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face,
                             @Nullable LivingEntity igniter) {  /* ported stub */ }

    public void explode(Level level, BlockPos pos, @Nullable LivingEntity exploder) {
        if (!level.isClientSide()) {
            GTExplosiveEntity entity = createEntity(level, pos, exploder);
            entity.setFuse(fuseLength);
            level.addFreshEntity(entity);
            level.playSound(null, entity, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(entity, GameEvent.PRIME_FUSE, pos);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {  /* ported stub */ }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) { return null; /* ported stub */ }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (explodeOnMine && !player.isShiftKeyDown()) {
            this.explode(level, pos, player);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {  /* ported stub */ }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (canRedstoneActivate) {
            if (level.hasNeighborSignal(pos)) {
                explode(level, pos, null);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (explodeOnMine) {
            Entity player = params.getOptionalParameter(LootContextParams.THIS_ENTITY);
            if (player != null && !player.isShiftKeyDown()) {
                return Collections.emptyList();
            }
        }

        return super.getDrops(state, params);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
                                boolean movedByPiston) {  /* ported stub */ }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {  /* ported stub */ }
}
