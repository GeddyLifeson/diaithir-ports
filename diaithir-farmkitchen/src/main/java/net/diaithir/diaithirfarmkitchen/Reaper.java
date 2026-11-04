package net.diaithir.diaithirfarmkitchen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.List;

/**
 * Reaper (farm.reaper, Age V; lives in diaithir-farmkitchen, was first added in diaithir-carts): a steel cutter bar that stands at the edge of a field. Every 2 seconds it cuts every fully grown crop in the strip in front of it
 * (5 wide, 5 deep, at its own height), replants each one at age 0, and pushes the drops into whatever item handler sits behind it (chest, barrel, hopper,
 * storage-network port or Create belt). A full or missing container makes it spill on the ground instead. Redstone switches it off. Clean-room, from the public idea of
 * a reaper/harvester only. {@link #harvest} is shared so a tractor bridge can call the same field work.
 */
public final class Reaper extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final int INTERVAL = 40, WIDTH = 2, DEPTH = 5, MAX_PER_PASS = 12;

    public Reaper(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING); }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide() && !old.is(this)) level.scheduleTick(pos, this, INTERVAL);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.hasNeighborSignal(pos)) work(level, pos, state.getValue(FACING));
        level.scheduleTick(pos, this, INTERVAL);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockState next = state.setValue(FACING, state.getValue(FACING).getClockWise());
            level.setBlock(pos, next, 3);
            player.sendOverlayMessage(Component.literal("Cutting " + next.getValue(FACING).getName()));
        }
        return InteractionResult.SUCCESS;
    }

    /** One pass: cuts the strip and stores the drops behind the reaper. Returns the number of crops cut. */
    public static int work(ServerLevel level, BlockPos pos, Direction facing) {
        return harvest(level, pos, facing, WIDTH, DEPTH, MAX_PER_PASS, stack -> store(level, pos, facing, stack));
    }

    /** Cuts up to {@code max} mature crops in the strip {@code depth} deep and {@code 2*half+1} wide in front of {@code from}; each drop stack goes to {@code sink}. Returns the number cut. */
    public static int harvest(ServerLevel level, BlockPos from, Direction facing, int half, int depth, int max, java.util.function.Consumer<ItemStack> sink) {
        int cut = 0;
        Direction side = facing.getClockWise();
        for (int d = 1; d <= depth && cut < max; d++) {
            for (int w = -half; w <= half && cut < max; w++) {
                BlockPos p = from.relative(facing, d).relative(side, w);
                BlockState s = level.getBlockState(p);
                if (!(s.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(s)) continue;
                for (ItemStack drop : Block.getDrops(s, level, p, level.getBlockEntity(p))) sink.accept(drop);
                level.setBlock(p, crop.getStateForAge(0), 3);
                cut++;
            }
        }
        return cut;
    }

    private static void store(ServerLevel level, BlockPos pos, Direction facing, ItemStack stack) {
        var handler = level.getCapability(Capabilities.Item.BLOCK, pos.relative(facing.getOpposite()), facing);
        int left = stack.getCount();
        if (handler != null) {
            try (Transaction tx = Transaction.openRoot()) {
                int in = 0;
                for (int i = 0; i < handler.size() && in < left; i++) in += handler.insert(i, ItemResource.of(stack), left - in, tx);
                tx.commit();
                left -= in;
            }
        }
        if (left > 0) Block.popResource(level, pos, stack.copyWithCount(left));
    }

    /** Headless check: a ripe 5x5 wheat field in front, a chest behind, one pass. Builds a few blocks above the given position (sky light is already lit there, which crops need) and clears it afterwards. */
    static String test(ServerLevel level, BlockPos base) {
        BlockPos pos = base.above(4);
        Direction facing = Direction.SOUTH;
        var air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -2, -2), pos.offset(4, 2, 8))) level.setBlock(p, air, 2);
        level.setBlock(pos, FarmkitchenMod.REAPER.get().defaultBlockState().setValue(FACING, facing), 2);
        level.setBlock(pos.north(), net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState(), 2);
        var wheat = (net.minecraft.world.level.block.CropBlock) net.minecraft.world.level.block.Blocks.WHEAT;
        int planted = 0;
        for (int d = 1; d <= DEPTH; d++) for (int w = -WIDTH; w <= WIDTH; w++) {
            level.setBlock(pos.relative(facing, d).relative(Direction.WEST, w).below(), net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 2);
            level.setBlock(pos.relative(facing, d).relative(Direction.WEST, w), wheat.getStateForAge(7), 2);
            planted++;
        }
        for (BlockPos soil : new BlockPos[] {pos.relative(facing, 1).relative(Direction.WEST, 3).below(), pos.relative(facing, 6).below()}) {
            level.setBlock(soil, net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState(), 2);
        }
        level.setBlock(pos.relative(facing, 1).relative(Direction.WEST, 3), wheat.getStateForAge(7), 2); // outside the strip: must stay ripe
        level.setBlock(pos.relative(facing, 6), wheat.getStateForAge(7), 2);                              // too deep: must stay ripe
        level.setBlock(pos.relative(facing, 2), wheat.getStateForAge(3), 2);                              // unripe: must stay at 3
        int cut = harvest(level, pos, facing, WIDTH, DEPTH, 99, s -> store(level, pos, facing, s));
        String err = null;
        if (cut != planted - 1) err = "cut " + cut + " of " + (planted - 1) + " ripe crops in the strip";
        else if (level.getBlockState(pos.relative(facing, 1)).getValue(net.minecraft.world.level.block.CropBlock.AGE) != 0) err = "crop not replanted at age 0";
        else if (level.getBlockState(pos.relative(facing, 2)).getValue(net.minecraft.world.level.block.CropBlock.AGE) != 3) err = "unripe crop was cut";
        else if (level.getBlockState(pos.relative(facing, 1).relative(Direction.WEST, 3)).getValue(net.minecraft.world.level.block.CropBlock.AGE) != 7) err = "cut outside the strip";
        else if (level.getBlockState(pos.relative(facing, 6)).getValue(net.minecraft.world.level.block.CropBlock.AGE) != 7) err = "cut beyond the depth";
        else {
            int wheatItems = 0;
            var chest = level.getBlockEntity(pos.north());
            if (chest instanceof net.minecraft.world.Container c) {
                for (int i = 0; i < c.getContainerSize(); i++) if (c.getItem(i).is(net.minecraft.world.item.Items.WHEAT)) wheatItems += c.getItem(i).getCount();
            }
            if (wheatItems < cut) err = "chest holds " + wheatItems + " wheat for " + cut + " crops";
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-4, -2, -2), pos.offset(4, 2, 8))) level.setBlock(p, air, 2);
        return err;
    }
}
