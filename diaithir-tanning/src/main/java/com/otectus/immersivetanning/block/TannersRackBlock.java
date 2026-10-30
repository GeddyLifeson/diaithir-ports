package com.otectus.immersivetanning.block;

import com.otectus.immersivetanning.advancement.ModAdvancements;
import com.otectus.immersivetanning.blockentity.TannersRackBlockEntity;
import com.otectus.immersivetanning.config.ClientConfig;
import com.otectus.immersivetanning.hide.HideData;
import com.otectus.immersivetanning.hide.HideItems;
import com.otectus.immersivetanning.item.TannersKnifeItem;
import com.otectus.immersivetanning.minigame.SessionManager;
import com.otectus.immersivetanning.registry.ModBlockEntities;
import com.otectus.immersivetanning.registry.ModItems;
import com.otectus.immersivetanning.registry.ModSounds;
import com.otectus.immersivetanning.util.Feedback;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * The Tanner's Rack: a timber frame with a fleshing beam in front. A raw hide drapes over the beam to be fleshed with
 * the Tanner's Knife; a wet tanned hide is laced into the frame with the chosen finish and dries by itself.
 */
public class TannersRackBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    private static final StationShape SHAPE = new StationShape(Shapes.or(
            Block.box(0, 0, 5, 4, 1, 11),
            Block.box(12, 0, 5, 16, 1, 11),
            Block.box(1, 0, 7, 3, 16, 9),
            Block.box(13, 0, 7, 15, 16, 9),
            Block.box(3, 14, 7, 13, 16, 9),
            Block.box(3, 2, 7, 13, 4, 9),
            Block.box(1, 4, 3, 3, 6, 7),
            Block.box(13, 4, 3, 15, 6, 7),
            Block.box(2, 4, 1, 14, 6, 3)));

    public static final MapCodec<TannersRackBlock> CODEC = simpleCodec(TannersRackBlock::new);

    public TannersRackBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE.facing(state.getValue(FACING));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TannersRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.TANNERS_RACK.get(), TannersRackBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        return held.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : handleUse(held, level, pos, player, hand);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return handleUse(ItemStack.EMPTY, level, pos, player, InteractionHand.MAIN_HAND);
    }

    private InteractionResult handleUse(ItemStack held, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive() && held.getItem() instanceof BlockItem) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof TannersRackBlockEntity rack) || !(player instanceof ServerPlayer sp)) {
            return InteractionResult.PASS;
        }
        interact(sp, rack, held, hand);
        return InteractionResult.CONSUME;
    }

    private void interact(ServerPlayer player, TannersRackBlockEntity rack, ItemStack held, InteractionHand hand) {
        if (rack.isLockedFor(player.getUUID())) {
            Feedback.fail(player, "station_in_use");
            return;
        }
        Level level = player.level();
        switch (rack.mode()) {
            case EMPTY -> {
                Optional<HideData> raw = HideItems.resolve(held);
                if (raw.isPresent()) {
                    rack.mountRaw(raw.get());
                    if (!player.getAbilities().instabuild) held.shrink(1);
                    level.playSound(null, rack.getBlockPos(), ModSounds.RACK_MOUNT.get(), SoundSource.BLOCKS, 0.9F, 1.0F);
                    Feedback.info(player, "flesh_with_knife");
                } else if (held.is(ModItems.WET_TANNED_HIDE.get())) {
                    if (player.isSecondaryUseActive()) SessionManager.mountWet(player, rack, held, SessionManager.lastFinish(player));
                    else SessionManager.openFinish(player, rack, held);
                } else if (held.is(ModItems.SCRAPED_HIDE.get())) {
                    Feedback.info(player, "scraped_to_vat");
                } else {
                    Feedback.info(player, "rack_empty");
                }
            }
            case RAW -> {
                if (held.getItem() instanceof TannersKnifeItem) {
                    SessionManager.openFleshing(player, rack, hand);
                } else if (held.isEmpty() && player.isSecondaryUseActive()) {
                    player.getInventory().placeItemBackInInventory(rack.takeRaw());
                } else {
                    Feedback.info(player, "flesh_with_knife");
                }
            }
            case DRYING -> {
                if (rack.isDry()) {
                    ItemStack leather = rack.collectLeather();
                    player.getInventory().placeItemBackInInventory(leather);
                    level.playSound(null, rack.getBlockPos(), ModSounds.BENCH_FINISH.get(), SoundSource.BLOCKS, 0.8F, 1.1F);
                    ModAdvancements.award(player, ModAdvancements.FINISHED);
                } else if (held.isEmpty() && player.isSecondaryUseActive()) {
                    player.getInventory().placeItemBackInInventory(rack.takeWet());
                } else {
                    int percent = Math.round(rack.dryProgress(level.getGameTime()) * 100F);
                    Feedback.info(player, "drying_progress", rack.finish().displayName(), percent);
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof TannersRackBlockEntity rack) || rack.mode() != TannersRackBlockEntity.Mode.DRYING) return;
        float progress = rack.dryProgress(level.getGameTime());
        if (progress >= 0.6F || random.nextFloat() > 0.35F * (1F - progress) * ClientConfig.scaleParticles(10) / 10F) return;
        Direction facing = state.getValue(FACING);
        double along = 0.3 + random.nextDouble() * 0.4;
        double x = pos.getX() + 0.5 + (facing.getAxis() == Direction.Axis.Z ? along - 0.5 : 0);
        double z = pos.getZ() + 0.5 + (facing.getAxis() == Direction.Axis.X ? along - 0.5 : 0);
        level.addParticle(ParticleTypes.DRIPPING_WATER, x, pos.getY() + 0.3, z, 0, 0, 0);
    }
}
