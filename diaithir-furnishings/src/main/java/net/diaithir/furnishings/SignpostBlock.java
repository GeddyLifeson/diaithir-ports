package net.diaithir.furnishings;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A wooden post. Use a sign on it to fix an arm (two at most, upper first), click to turn the arm an eighth of a turn,
 * sneak-click with an empty hand to write on it, use an axe to take an arm off again.
 */
public final class SignpostBlock extends BaseEntityBlock {
    public static final MapCodec<SignpostBlock> CODEC = simpleCodec(SignpostBlock::new);
    private static final VoxelShape POST = Block.box(6, 0, 6, 10, 16, 10);
    /** Arm board geometry in blocks: 16 long, starts 3 px behind the post's axis, sits against the post's face. */
    public static final double REACH_START = -3 / 16.0, REACH_END = 13 / 16.0, FACE = 2.5 / 16.0, HALF_THICK = 0.5 / 16.0;
    /** Bottom of each arm board, in pixels above the block floor. */
    public static final int[] ARM_Y = {9, 1};
    public static final int ARM_HEIGHT = 6;

    public SignpostBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SignpostEntity(pos, state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return POST;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = POST;
        if (level.getBlockEntity(pos) instanceof SignpostEntity post) {
            for (int i = 0; i < SignpostEntity.ARMS; i++) {
                SignpostEntity.Arm arm = post.arm(i);
                if (arm != null) {
                    shape = Shapes.or(shape, armShape(i, arm.dir()));
                }
            }
        }
        return shape;
    }

    /** The axis-aligned box that holds the turned board; coarse at the diagonals, which only makes it easier to hit. */
    public static VoxelShape armShape(int slot, int dir) {
        double yaw = Math.toRadians(dir * 45.0);
        double ux = -Math.sin(yaw), uz = Math.cos(yaw);
        double nx = -Math.cos(yaw), nz = -Math.sin(yaw);
        double minX = 9, minZ = 9, maxX = -9, maxZ = -9;
        for (double along : new double[] {REACH_START, REACH_END}) {
            for (double across : new double[] {FACE - HALF_THICK, FACE + HALF_THICK}) {
                double x = 0.5 + ux * along + nx * across;
                double z = 0.5 + uz * along + nz * across;
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
            }
        }
        // Only the part inside this block's cell can be part of its shape.
        minX = Mth.clamp(minX, 0, 1);
        maxX = Mth.clamp(maxX, 0, 1);
        minZ = Mth.clamp(minZ, 0, 1);
        maxZ = Mth.clamp(maxZ, 0, 1);
        if (maxX - minX < 1e-4 || maxZ - minZ < 1e-4) {
            return Shapes.empty();
        }
        double y0 = (ARM_Y[slot] + 1) / 16.0;
        return Shapes.box(minX, y0, minZ, maxX, y0 + ARM_HEIGHT / 16.0, maxZ);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction,
                                     BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
            : super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
    }

    /** Which arm a click landed on: the upper half picks the top arm, unless only the other exists. */
    static int armAt(SignpostEntity post, double localY) {
        int preferred = localY >= 0.5 ? 0 : 1;
        if (post.arm(preferred) != null) {
            return preferred;
        }
        return post.arm(1 - preferred) != null ? 1 - preferred : -1;
    }

    /** Heading for a new arm: the way the player is looking, rounded to an eighth of a turn. */
    static int headingOf(Player player) {
        return Math.floorMod(Math.round(player.getYRot() / 45.0F), SignpostEntity.DIRECTIONS);
    }

    static boolean isSign(ItemStack stack) {
        return stack.is(ItemTags.SIGNS);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SignpostEntity post)) {
            return InteractionResult.PASS;
        }
        if (isSign(stack)) {
            int slot = post.freeSlot();
            if (slot < 0) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            if (level.isClientSide()) {
                ClientHooks.openEditor(pos, slot);
                return InteractionResult.SUCCESS;
            }
            String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            post.setArm(slot, new SignpostEntity.Arm(slot, id, headingOf(player), "", ""));
            stack.consume(1, player);
            level.playSound(null, pos, getSoundType(state).getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.9F);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (stack.is(ItemTags.AXES)) {
            int slot = armAt(post, hit.getLocation().y - pos.getY());
            if (slot < 0) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                SignpostEntity.Arm arm = post.arm(slot);
                post.setArm(slot, null);
                if (!player.hasInfiniteMaterials()) {
                    player.getInventory().placeItemBackInInventory(arm.signStack());
                }
                level.playSound(null, pos, getSoundType(state).getBreakSound(), SoundSource.BLOCKS, 0.8F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SignpostEntity post)) {
            return InteractionResult.PASS;
        }
        int slot = armAt(post, hit.getLocation().y - pos.getY());
        if (slot < 0) {
            return InteractionResult.PASS;
        }
        if (player.isSecondaryUseActive()) {
            if (level.isClientSide()) {
                ClientHooks.openEditor(pos, slot);
            }
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            SignpostEntity.Arm arm = post.arm(slot);
            post.setArm(slot, arm.withDir(arm.dir() + 1));
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }
}
