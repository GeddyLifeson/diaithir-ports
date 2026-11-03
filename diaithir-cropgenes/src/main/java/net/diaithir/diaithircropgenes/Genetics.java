package net.diaithir.diaithircropgenes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Genetics {
    public static final String[] SPECIES = {"wheat_line", "barley", "flax", "sunroot", "cinder_bean", "amber_rice"};

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CropgenesMod.ID + "_genes");
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CropgenesMod.ID);
    private static final DeferredRegister<BlockEntityType<?>> BE_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CropgenesMod.ID);

    public static final List<DeferredBlock<GeneticCropBlock>> CROPS = new ArrayList<>();
    public static final List<DeferredItem<Item>> SEEDS = new ArrayList<>();
    public static final List<DeferredItem<Item>> PRODUCE = new ArrayList<>();

    public static final Supplier<BlockEntityType<GeneBlockEntity>> GENE_BE =
            BE_TYPES.register("gene_crop", () -> new BlockEntityType<>(GeneBlockEntity::new, CROPS.stream().map(c -> (Block) c.get()).toArray(Block[]::new)));

    static {
        for (int i = 0; i < SPECIES.length; i++) {
            final int idx = i;
            String s = SPECIES[i];
            DeferredItem<Item> produce = ITEMS.registerSimpleItem(s + "_crop");
            PRODUCE.add(produce);
            DeferredBlock<GeneticCropBlock> crop = BLOCKS.registerBlock(s + "_plant",
                    p -> new GeneticCropBlock(idx, p),
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT));
            CROPS.add(crop);
            SEEDS.add(ITEMS.registerItem(s + "_seeds", p -> new GeneSeedItem(idx, p)));
        }
    }

    private Genetics() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BE_TYPES.register(bus);
    }

    public static final class GeneBlockEntity extends BlockEntity {
        public int yield = 1;
        public int speed = 1;

        public GeneBlockEntity(BlockPos pos, BlockState state) {
            super(GENE_BE.get(), pos, state);
        }

        @Override
        protected void saveAdditional(ValueOutput out) {
            super.saveAdditional(out);
            out.putInt("gene_yield", yield);
            out.putInt("gene_speed", speed);
        }

        @Override
        protected void loadAdditional(ValueInput in) {
            super.loadAdditional(in);
            yield = clamp(in.getIntOr("gene_yield", 1));
            speed = clamp(in.getIntOr("gene_speed", 1));
        }
    }

    public static int clamp(int v) {
        return Math.max(1, Math.min(5, v));
    }

    public static final class GeneSeedItem extends Item {
        public final int species;

        public GeneSeedItem(int species, Item.Properties p) {
            super(p);
            this.species = species;
        }

        public static int[] read(ItemStack stack) {
            CustomData d = stack.get(DataComponents.CUSTOM_DATA);
            if (d == null) return new int[] {1, 1};
            CompoundTag t = d.copyTag();
            return new int[] {clamp(t.getIntOr("gene_yield", 1)), clamp(t.getIntOr("gene_speed", 1))};
        }

        public static void write(ItemStack stack, int y, int sp) {
            CompoundTag t = new CompoundTag();
            t.putInt("gene_yield", clamp(y));
            t.putInt("gene_speed", clamp(sp));
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(t));
        }

        @Override
        public InteractionResult useOn(net.minecraft.world.item.context.UseOnContext ctx) {
            Level level = ctx.getLevel();
            BlockPos pos = ctx.getClickedPos();
            BlockState st = level.getBlockState(pos);
            ItemStack stack = ctx.getItemInHand();
            // crossing
            if (st.getBlock() instanceof GeneticCropBlock gb && gb.species != species
                    && st.getValue(GeneticCropBlock.AGE) >= GeneticCropBlock.MAX) {
                if (level.isClientSide()) return InteractionResult.SUCCESS;
                if (level.getBlockEntity(pos) instanceof GeneBlockEntity be) {
                    int[] g = read(stack);
                    var r = level.getRandom();
                    be.yield = clamp((be.yield + g[0]) / 2 + r.nextInt(3) - 1);
                    be.speed = clamp((be.speed + g[1]) / 2 + r.nextInt(3) - 1);
                    be.setChanged();
                    if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) stack.shrink(1);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.PASS;
            }
            // planting
            BlockPos above = pos.above();
            if (level.getBlockState(pos).is(Blocks.FARMLAND) && level.getBlockState(above).isAir()) {
                if (level.isClientSide()) return InteractionResult.SUCCESS;
                level.setBlock(above, CROPS.get(species).get().defaultBlockState(), 3);
                if (level.getBlockEntity(above) instanceof GeneBlockEntity be) {
                    int[] g = read(stack);
                    be.yield = g[0];
                    be.speed = g[1];
                    be.setChanged();
                }
                if (ctx.getPlayer() == null || !ctx.getPlayer().isCreative()) stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
    }

    public static final class GeneticCropBlock extends Block implements EntityBlock {
        public static final int MAX = 7;
        public static final IntegerProperty AGE = BlockStateProperties_AGE.AGE;
        public final int species;

        public GeneticCropBlock(int species, BlockBehaviour.Properties p) {
            super(p.randomTicks().noCollision().instabreak().noOcclusion());
            this.species = species;
            registerDefaultState(stateDefinition.any().setValue(AGE, 0));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(AGE);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new GeneBlockEntity(pos, state);
        }

        @Override
        protected boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
            return level.getBlockState(pos.below()).is(Blocks.FARMLAND);
        }

        @Override
        protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource rnd) {
            int age = state.getValue(AGE);
            if (age >= MAX) return;
            int sp = level.getBlockEntity(pos) instanceof GeneBlockEntity be ? be.speed : 1;
            if (rnd.nextInt(Math.max(1, 7 - sp)) == 0) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            }
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (state.getValue(AGE) < MAX) return InteractionResult.PASS;
            if (level.isClientSide()) return InteractionResult.SUCCESS;
            int y = 1;
            int sp = 1;
            if (level.getBlockEntity(pos) instanceof GeneBlockEntity be) {
                y = be.yield;
                sp = be.speed;
            }
            var rnd = level.getRandom();
            int count = y + rnd.nextInt(2);
            Block.popResource(level, pos, new ItemStack(PRODUCE.get(species).get(), count));
            ItemStack seed = new ItemStack(SEEDS.get(species).get(), 1 + (rnd.nextInt(5) < y ? 1 : 0));
            GeneSeedItem.write(seed, y, sp);
            Block.popResource(level, pos, seed);
            level.setBlock(pos, state.setValue(AGE, 0), 2);
            return InteractionResult.SUCCESS;
        }
    }

    private static final class BlockStateProperties_AGE {
        static final IntegerProperty AGE = net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_7;
    }
}
