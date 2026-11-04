package net.diaithir.worldsys.utilityblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registers the angel item, sky sand, redstone clock, fluid hopper and lamp block. */
public final class UtilityBlocksFeature {
    public static final String ID = "diaithirworldsys";
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private static BlockBehaviour.Properties props(MapColor c, SoundType s, float hardness) {
        return BlockBehaviour.Properties.of().mapColor(c).sound(s).strength(hardness);
    }

    public static final DeferredBlock<Block> ANGEL_BLOCK = BLOCKS.registerBlock("angel_block", Block::new, () -> props(MapColor.STONE, SoundType.STONE, 1.5F));
    public static final DeferredBlock<Block> SKY_SAND = BLOCKS.registerBlock("sky_sand", Block::new, () -> props(MapColor.SAND, SoundType.SAND, 0.5F));
    public static final DeferredBlock<Block> LAMP_BLOCK = BLOCKS.registerBlock("utility_lamp", Block::new, () -> props(MapColor.COLOR_YELLOW, SoundType.GLASS, 0.3F).lightLevel(s -> 15));
    public static final DeferredBlock<ClockBlock> REDSTONE_CLOCK = BLOCKS.registerBlock("redstone_clock", ClockBlock::new, () -> props(MapColor.COLOR_RED, SoundType.STONE, 1.0F));
    public static final DeferredBlock<FluidHopperBlock> FLUID_HOPPER = BLOCKS.registerBlock("fluid_hopper", FluidHopperBlock::new, () -> props(MapColor.METAL, SoundType.METAL, 2.0F).noOcclusion());

    public static final DeferredItem<BlockItem> ANGEL_ITEM = ITEMS.registerSimpleBlockItem(ANGEL_BLOCK);
    public static final DeferredItem<BlockItem> SKY_SAND_ITEM = ITEMS.registerSimpleBlockItem(SKY_SAND);
    public static final DeferredItem<BlockItem> LAMP_ITEM = ITEMS.registerSimpleBlockItem(LAMP_BLOCK);
    public static final DeferredItem<BlockItem> CLOCK_ITEM = ITEMS.registerSimpleBlockItem(REDSTONE_CLOCK);
    public static final DeferredItem<BlockItem> HOPPER_ITEM = ITEMS.registerSimpleBlockItem(FLUID_HOPPER);

    private UtilityBlocksFeature() {}

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                e.accept(ANGEL_ITEM.get());
                e.accept(SKY_SAND_ITEM.get());
                e.accept(LAMP_ITEM.get());
                e.accept(CLOCK_ITEM.get());
                e.accept(HOPPER_ITEM.get());
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickItem.class, UtilityBlocksFeature::angel);
    }

    private static void angel(PlayerInteractEvent.RightClickItem e) {
        ItemStack held = e.getItemStack();
        if (!held.is(ANGEL_ITEM.get())) return;
        Player p = e.getEntity();
        Level level = e.getLevel();
        Vec3 eye = p.getEyePosition();
        Vec3 end = eye.add(p.getLookAngle().scale(5.0));
        HitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        if (hit.getType() != HitResult.Type.MISS) return;
        BlockPos pos = BlockPos.containing(end);
        if (!level.getBlockState(pos).canBeReplaced()) return;
        e.setCancellationResult(InteractionResult.SUCCESS);
        e.setCanceled(true);
        if (level.isClientSide()) return;
        level.setBlockAndUpdate(pos, ANGEL_BLOCK.get().defaultBlockState());
        if (!p.getAbilities().instabuild) held.shrink(1);
    }

    public static final class ClockBlock extends Block {
        public ClockBlock(BlockBehaviour.Properties p) {
            super(p);
            registerDefaultState(stateDefinition.any().setValue(POWERED, false));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(POWERED);
        }

        @Override
        protected boolean isSignalSource(BlockState s) {
            return true;
        }

        @Override
        protected int getSignal(BlockState s, BlockGetter g, BlockPos pos, Direction d) {
            return s.getValue(POWERED) ? 15 : 0;
        }

        @Override
        protected void onPlace(BlockState s, Level level, BlockPos pos, BlockState old, boolean moved) {
            if (!level.isClientSide() && !old.is(this)) level.scheduleTick(pos, this, 20);
        }

        @Override
        protected void tick(BlockState s, ServerLevel level, BlockPos pos, RandomSource r) {
            boolean on = s.getValue(POWERED);
            level.setBlock(pos, s.setValue(POWERED, !on), 3);
            level.scheduleTick(pos, this, on ? 18 : 2);
        }
    }

    public static final class FluidHopperBlock extends Block {
        public FluidHopperBlock(BlockBehaviour.Properties p) {
            super(p);
        }

        @Override
        protected void onPlace(BlockState s, Level level, BlockPos pos, BlockState old, boolean moved) {
            if (!level.isClientSide() && !old.is(this)) level.scheduleTick(pos, this, 8);
        }

        @Override
        protected void tick(BlockState s, ServerLevel level, BlockPos pos, RandomSource r) {
            BlockPos up = pos.above();
            BlockState above = level.getBlockState(up);
            FluidState fs = above.getFluidState();
            if (!fs.isEmpty() && fs.isSource() && above.getBlock() == fs.createLegacyBlock().getBlock()) {
                level.setBlock(up, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
            level.scheduleTick(pos, this, 8);
        }
    }
}
