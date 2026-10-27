package com.kyanite.deeperdarker.content;

import com.kyanite.deeperdarker.DeeperDarker;
import com.kyanite.deeperdarker.content.blocks.*;
import com.kyanite.deeperdarker.content.blocks.entity.DDHangingSignBlockEntity;
import com.kyanite.deeperdarker.content.blocks.entity.DDSignBlockEntity;
import com.kyanite.deeperdarker.content.blocks.flammable.*;
import com.kyanite.deeperdarker.content.blocks.vegetation.*;
import com.kyanite.deeperdarker.world.DDConfiguredFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("NullableProblems")
public class DDBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(DeeperDarker.MOD_ID);

    private static final BlockSetType ECHO_SET = BlockSetType.register(new BlockSetType("echo"));
    public static final WoodType ECHO = WoodType.register(new WoodType("echo", ECHO_SET));

    public static final DeferredBlock<RotatedPillarBlock> ECHO_LOG = register("echo_log", id -> new RotatedFlammableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).setId(blockKey(id)).mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? MapColor.COLOR_LIGHT_GRAY : MapColor.COLOR_PURPLE), 5, 5));
    public static final DeferredBlock<RotatedPillarBlock> ECHO_WOOD = register("echo_wood", id -> new RotatedFlammableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).setId(blockKey(id)).mapColor(MapColor.COLOR_PURPLE), 5, 5));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_ECHO_LOG = register("stripped_echo_log", id -> new RotatedFlammableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY), 5, 5));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_ECHO_WOOD = register("stripped_echo_wood", id -> new RotatedFlammableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY), 5, 5));
    public static final DeferredBlock<Block> ECHO_PLANKS = register("echo_planks", id -> new FlammableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY), 5, 20));
    public static final DeferredBlock<StairBlock> ECHO_STAIRS = register("echo_stairs", id -> new FlammableStairBlock(ECHO_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY), 5, 20));
    public static final DeferredBlock<SlabBlock> ECHO_SLAB = register("echo_slab", id -> new FlammableSlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY), 5, 20));
    public static final DeferredBlock<FenceBlock> ECHO_FENCE = register("echo_fence", id -> new FlammableFenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY), 5, 20));
    public static final DeferredBlock<FenceGateBlock> ECHO_FENCE_GATE = register("echo_fence_gate", id -> new FlammableFenceGateBlock(ECHO, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY), 5, 20));
    public static final DeferredBlock<DoorBlock> ECHO_DOOR = register("echo_door", id -> new DoorBlock(ECHO_SET, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final DeferredBlock<TrapDoorBlock> ECHO_TRAPDOOR = register("echo_trapdoor", id -> new TrapDoorBlock(ECHO_SET, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final DeferredBlock<PressurePlateBlock> ECHO_PRESSURE_PLATE = register("echo_pressure_plate", id -> new PressurePlateBlock(ECHO_SET, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY)));
    public static final DeferredBlock<ButtonBlock> ECHO_BUTTON = register("echo_button", id -> new ButtonBlock(ECHO_SET, 30, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON).setId(blockKey(id))));
    public static final DeferredBlock<LeavesBlock> ECHO_LEAVES = register("echo_leaves", id -> new FlammableLeavesBlock(0.01f, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).setId(blockKey(id)).mapColor(MapColor.COLOR_PURPLE), 30, 60));
    public static final TreeGrower ECHO_TREE = new TreeGrower("echo", Optional.empty(), Optional.of(DDConfiguredFeatures.TREE_ECHO), Optional.empty());
    public static final DeferredBlock<SaplingBlock> ECHO_SAPLING = register("echo_sapling", id -> new SaplingBlock(ECHO_TREE, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING).setId(blockKey(id))) {
        @Override
        protected boolean mayPlaceOn(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
            return pState.is(DDBlocks.ECHO_SOIL.get()) || pState.is(DDBlocks.SCULK_GRIME.get());
        }
    });
    public static final DeferredBlock<StandingSignBlock> ECHO_SIGN = BLOCKS.register("echo_sign", id -> new StandingSignBlock(ECHO, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY)) {
        @Override
        public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
            return new DDSignBlockEntity(pPos, pState);
        }
    });
    public static final DeferredBlock<WallSignBlock> ECHO_WALL_SIGN = BLOCKS.register("echo_wall_sign", id -> new WallSignBlock(ECHO, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN).setId(blockKey(id)).overrideLootTable(ECHO_SIGN.get().getLootTable()).overrideDescription(ECHO_SIGN.get().getDescriptionId()).mapColor(MapColor.COLOR_LIGHT_GRAY)) {
        @Override
        public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
            return new DDSignBlockEntity(pPos, pState);
        }
    });
    public static final DeferredBlock<CeilingHangingSignBlock> ECHO_HANGING_SIGN = BLOCKS.register("echo_hanging_sign", id -> new CeilingHangingSignBlock(ECHO, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY)) {
        @Override
        public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
            return new DDHangingSignBlockEntity(pPos, pState);
        }
    });
    public static final DeferredBlock<WallHangingSignBlock> ECHO_WALL_HANGING_SIGN = BLOCKS.register("echo_wall_hanging_sign", id -> new WallHangingSignBlock(ECHO, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(ECHO_HANGING_SIGN.get().getLootTable()).overrideDescription(ECHO_HANGING_SIGN.get().getDescriptionId()).mapColor(MapColor.COLOR_LIGHT_GRAY)) {
        @Override
        public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
            return new DDHangingSignBlockEntity(pPos, pState);
        }
    });
    public static final DeferredBlock<Block> POTTED_ECHO_SAPLING = BLOCKS.register("potted_echo_sapling", id -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, ECHO_SAPLING, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_OAK_SAPLING).setId(blockKey(id))));

    private static final BlockSetType BLOOM_SET = BlockSetType.register(new BlockSetType("bloom"));
    public static final WoodType BLOOM = WoodType.register(new WoodType("bloom", BLOOM_SET));
    
    public static final DeferredBlock<Block> BLOOMING_STEM = register("blooming_stem", id -> new BloomingStemBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).strength(1f).randomTicks().sound(SoundType.WOOD).mapColor(MapColor.COLOR_CYAN).ignitedByLava().noOcclusion()));
    public static final DeferredBlock<Block> STRIPPED_BLOOMING_STEM = register("stripped_blooming_stem", id -> new BloomingStemBlock(BlockBehaviour.Properties.ofFullCopy(BLOOMING_STEM.get()).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN)));
    public static final DeferredBlock<Block> BLOOM_PLANKS = register("bloom_planks", id -> new FlammableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN), 5, 20));
    public static final DeferredBlock<StairBlock> BLOOM_STAIRS = register("bloom_stairs", id -> new FlammableStairBlock(BLOOM_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN), 5, 20));
    public static final DeferredBlock<SlabBlock> BLOOM_SLAB = register("bloom_slab", id -> new FlammableSlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN), 5, 20));
    public static final DeferredBlock<FenceBlock> BLOOM_FENCE = register("bloom_fence", id -> new FlammableFenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN), 5, 20));
    public static final DeferredBlock<FenceGateBlock> BLOOM_FENCE_GATE = register("bloom_fence_gate", id -> new FlammableFenceGateBlock(BLOOM, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN), 5, 20));
    public static final DeferredBlock<DoorBlock> BLOOM_DOOR = register("bloom_door", id -> new DoorBlock(BLOOM_SET, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN)));
    public static final DeferredBlock<TrapDoorBlock> BLOOM_TRAPDOOR = register("bloom_trapdoor", id -> new TrapDoorBlock(BLOOM_SET, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN)));
    public static final DeferredBlock<PressurePlateBlock> BLOOM_PRESSURE_PLATE = register("bloom_pressure_plate", id -> new PressurePlateBlock(BLOOM_SET, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN)));
    public static final DeferredBlock<ButtonBlock> BLOOM_BUTTON = register("bloom_button", id -> new ButtonBlock(BLOOM_SET, 30, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON).setId(blockKey(id))));
    public static final DeferredBlock<StandingSignBlock> BLOOM_SIGN = BLOCKS.register("bloom_sign", id -> new StandingSignBlock(BLOOM, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN).setId(blockKey(id)).mapColor(MapColor.GLOW_LICHEN)) {
        @Override
        public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
            return new DDSignBlockEntity(pPos, pState);
        }
    });
    public static final DeferredBlock<WallSignBlock> BLOOM_WALL_SIGN = BLOCKS.register("bloom_wall_sign", id -> new WallSignBlock(BLOOM, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN).setId(blockKey(id)).overrideLootTable(BLOOM_SIGN.get().getLootTable()).overrideDescription(BLOOM_SIGN.get().getDescriptionId()).mapColor(MapColor.COLOR_LIGHT_GRAY)) {
        @Override
        public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
            return new DDSignBlockEntity(pPos, pState);
        }
    });
    public static final DeferredBlock<CeilingHangingSignBlock> BLOOM_HANGING_SIGN = BLOCKS.register("bloom_hanging_sign", id -> new CeilingHangingSignBlock(BLOOM, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_GRAY)) {
        @Override
        public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
            return new DDHangingSignBlockEntity(pPos, pState);
        }
    });
    public static final DeferredBlock<WallHangingSignBlock> BLOOM_WALL_HANGING_SIGN = BLOCKS.register("bloom_wall_hanging_sign", id -> new WallHangingSignBlock(BLOOM, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(BLOOM_HANGING_SIGN.get().getLootTable()).overrideDescription(BLOOM_HANGING_SIGN.get().getDescriptionId()).mapColor(MapColor.COLOR_LIGHT_GRAY)) {
        @Override
        public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
            return new DDHangingSignBlockEntity(pPos, pState);
        }
    });
    public static final DeferredBlock<Block> POTTED_BLOOMING_STEM = BLOCKS.register("potted_blooming_stem", id -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, BLOOMING_STEM, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_OAK_SAPLING).setId(blockKey(id))));

    public static final DeferredBlock<Block> SCULK_STONE = register("sculk_stone", id -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(id)).sound(DDSounds.SCULK_STONE).mapColor(MapColor.COLOR_CYAN).requiresCorrectToolForDrops()));
    public static final DeferredBlock<StairBlock> SCULK_STONE_STAIRS = register("sculk_stone_stairs", id -> new StairBlock(SCULK_STONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> SCULK_STONE_SLAB = register("sculk_stone_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> SCULK_STONE_WALL = register("sculk_stone_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> COBBLED_SCULK_STONE = register("cobbled_sculk_stone", id -> new Block(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> COBBLED_SCULK_STONE_STAIRS = register("cobbled_sculk_stone_stairs", id -> new StairBlock(COBBLED_SCULK_STONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(COBBLED_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> COBBLED_SCULK_STONE_SLAB = register("cobbled_sculk_stone_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> COBBLED_SCULK_STONE_WALL = register("cobbled_sculk_stone_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> POLISHED_SCULK_STONE = register("polished_sculk_stone", id -> new Block(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> POLISHED_SCULK_STONE_STAIRS = register("polished_sculk_stone_stairs", id -> new StairBlock(POLISHED_SCULK_STONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(POLISHED_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> POLISHED_SCULK_STONE_SLAB = register("polished_sculk_stone_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(POLISHED_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> POLISHED_SCULK_STONE_WALL = register("polished_sculk_stone_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(POLISHED_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SCULK_STONE_BRICKS = register("sculk_stone_bricks", id -> new Block(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> SCULK_STONE_BRICK_STAIRS = register("sculk_stone_brick_stairs", id -> new StairBlock(SCULK_STONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> SCULK_STONE_BRICK_SLAB = register("sculk_stone_brick_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> SCULK_STONE_BRICK_WALL = register("sculk_stone_brick_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SCULK_STONE_TILES = register("sculk_stone_tiles", id -> new Block(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> SCULK_STONE_TILE_STAIRS = register("sculk_stone_tile_stairs", id -> new StairBlock(SCULK_STONE_TILES.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE_TILES.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> SCULK_STONE_TILE_SLAB = register("sculk_stone_tile_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE_TILES.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> SCULK_STONE_TILE_WALL = register("sculk_stone_tile_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE_TILES.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SMOOTH_SCULK_STONE = register("smooth_sculk_stone", id -> new Block(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> SMOOTH_SCULK_STONE_STAIRS = register("smooth_sculk_stone_stairs", id -> new StairBlock(SMOOTH_SCULK_STONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(SMOOTH_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> SMOOTH_SCULK_STONE_SLAB = register("smooth_sculk_stone_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(SMOOTH_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> SMOOTH_SCULK_STONE_WALL = register("smooth_sculk_stone_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(SMOOTH_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> CUT_SCULK_STONE = register("cut_sculk_stone", id -> new Block(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> CUT_SCULK_STONE_STAIRS = register("cut_sculk_stone_stairs", id -> new StairBlock(CUT_SCULK_STONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(CUT_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> CUT_SCULK_STONE_SLAB = register("cut_sculk_stone_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(CUT_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> CUT_SCULK_STONE_WALL = register("cut_sculk_stone_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(CUT_SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> CHISELED_SCULK_STONE = register("chiseled_sculk_stone", id -> new Block(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));

    public static final DeferredBlock<Block> SCULK_GRIME = register("sculk_grime", id -> new MudBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).setId(blockKey(id)).strength(0.6f).mapColor(MapColor.COLOR_CYAN)));
    public static final DeferredBlock<Block> SCULK_GRIME_BRICKS = register("sculk_grime_bricks", id -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).setId(blockKey(id)).mapColor(MapColor.TERRACOTTA_GREEN)));
    public static final DeferredBlock<SlabBlock> SCULK_GRIME_BRICK_SLAB = register("sculk_grime_brick_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_GRIME_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> SCULK_GRIME_BRICK_STAIRS = register("sculk_grime_brick_stairs", id -> new StairBlock(SCULK_GRIME_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(SCULK_GRIME_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> SCULK_GRIME_BRICK_WALL = register("sculk_grime_brick_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_GRIME_BRICKS.get()).setId(blockKey(id))));

    public static final DeferredBlock<Block> BLOOMING_SCULK_STONE = register("blooming_sculk_stone", id -> new Block(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id)).strength(1.5f, 4f).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<Block> BLOOMING_MOSS_BLOCK = register("blooming_moss_block", id -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK).setId(blockKey(id)).sound(SoundType.SCULK).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<Block> ECHO_SOIL = register("echo_soil", id -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).setId(blockKey(id)).strength(0.3f).mapColor(MapColor.COLOR_PURPLE)));

    public static final DeferredBlock<Block> GLOOMSLATE = register("gloomslate", id -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).setId(blockKey(id)).strength(2.5f, 4.5f).mapColor(MapColor.TERRACOTTA_BROWN).requiresCorrectToolForDrops()));
    public static final DeferredBlock<StairBlock> GLOOMSLATE_STAIRS = register("gloomslate_stairs", id -> new StairBlock(GLOOMSLATE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> GLOOMSLATE_SLAB = register("gloomslate_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> GLOOMSLATE_WALL = register("gloomslate_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> COBBLED_GLOOMSLATE = register("cobbled_gloomslate", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> COBBLED_GLOOMSLATE_STAIRS = register("cobbled_gloomslate_stairs", id -> new StairBlock(COBBLED_GLOOMSLATE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(COBBLED_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> COBBLED_GLOOMSLATE_SLAB = register("cobbled_gloomslate_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> COBBLED_GLOOMSLATE_WALL = register("cobbled_gloomslate_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> POLISHED_GLOOMSLATE = register("polished_gloomslate", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> POLISHED_GLOOMSLATE_STAIRS = register("polished_gloomslate_stairs", id -> new StairBlock(POLISHED_GLOOMSLATE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(POLISHED_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> POLISHED_GLOOMSLATE_SLAB = register("polished_gloomslate_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(POLISHED_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> POLISHED_GLOOMSLATE_WALL = register("polished_gloomslate_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(POLISHED_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> GLOOMSLATE_BRICKS = register("gloomslate_bricks", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> GLOOMSLATE_BRICK_STAIRS = register("gloomslate_brick_stairs", id -> new StairBlock(GLOOMSLATE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> GLOOMSLATE_BRICK_SLAB = register("gloomslate_brick_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> GLOOMSLATE_BRICK_WALL = register("gloomslate_brick_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> ENRICHED_GLOOMSLATE_BRICKS = register("enriched_gloomslate_bricks", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> GLOOMSLATE_TILES = register("gloomslate_tiles", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> GLOOMSLATE_TILE_STAIRS = register("gloomslate_tile_stairs", id -> new StairBlock(GLOOMSLATE_TILES.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE_TILES.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> GLOOMSLATE_TILE_SLAB = register("gloomslate_tile_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE_TILES.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> GLOOMSLATE_TILE_WALL = register("gloomslate_tile_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE_TILES.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SMOOTH_GLOOMSLATE = register("smooth_gloomslate", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> SMOOTH_GLOOMSLATE_STAIRS = register("smooth_gloomslate_stairs", id -> new StairBlock(SMOOTH_GLOOMSLATE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(SMOOTH_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> SMOOTH_GLOOMSLATE_SLAB = register("smooth_gloomslate_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(SMOOTH_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> SMOOTH_GLOOMSLATE_WALL = register("smooth_gloomslate_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(SMOOTH_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> CUT_GLOOMSLATE = register("cut_gloomslate", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<StairBlock> CUT_GLOOMSLATE_STAIRS = register("cut_gloomslate_stairs", id -> new StairBlock(CUT_GLOOMSLATE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(CUT_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<SlabBlock> CUT_GLOOMSLATE_SLAB = register("cut_gloomslate_slab", id -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(CUT_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<WallBlock> CUT_GLOOMSLATE_WALL = register("cut_gloomslate_wall", id -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(CUT_GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<FenceBlock> GLOOMSLATE_BRICK_FENCE = register("gloomslate_brick_fence", id -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE_BRICKS.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> CHISELED_GLOOMSLATE = register("chiseled_gloomslate", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));

    public static final DeferredBlock<Block> SCULK_GLEAM = register("sculk_gleam", id -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).setId(blockKey(id)).sound(DDSounds.SCULK_GLEAM).lightLevel(_ -> 15).mapColor(MapColor.SAND)));
    public static final DeferredBlock<Block> POROUS_SCULK_GLEAM = register("porous_sculk_gleam", id -> new PorousSculkGleamBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_GLEAM.get()).setId(blockKey(id)).randomTicks().lightLevel(state -> state.getValue(PorousSculkGleamBlock.GEL_LEVEL) + 5)));
    public static final DeferredBlock<Block> GLOOMSLATE_LIGHT = register("gloomslate_light", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id)).lightLevel(_ -> 15).mapColor(MapColor.SAND)));
    public static final DeferredBlock<Block> LITE_BLOCK = register("lite_block", id -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.VERDANT_FROGLIGHT).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<Block> BORDERED_LITE_BLOCK = register("bordered_lite_block", id -> new Block(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE_LIGHT.get()).setId(blockKey(id)).mapColor(MapColor.COLOR_LIGHT_BLUE)));

    public static final DeferredBlock<Block> GLOOMY_SCULK = register("gloomy_sculk", id -> new GloomySculkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).setId(blockKey(id)).strength(0.3f).mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final DeferredBlock<GeyserBlock> GLOOMY_GEYSER = register("gloomy_geyser", id -> new GeyserBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).setId(blockKey(id)).lightLevel(_ -> 9).mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final DeferredBlock<Block> CRYSTALLIZED_AMBER = register("crystallized_amber", id -> new CrystallizedAmberBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).strength(0.3f, 3f).lightLevel(_ -> 1).sound(SoundType.GLASS).mapColor(MapColor.COLOR_ORANGE).noOcclusion()));
    public static final DeferredBlock<Block> GLEAM_GEL_BLOCK = register("gleam_gel_block", id -> new GleamGelBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).sound(SoundType.HONEY_BLOCK).noCollision().mapColor(MapColor.SNOW)));
    public static final DeferredBlock<Block> SOUNDPROOF_GLASS = register("soundproof_glass", id -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(id))));

    public static final DeferredBlock<Block> SCULK_STONE_COAL_ORE = register("sculk_stone_coal_ore", id -> new DropExperienceBlock(UniformInt.of(1, 4), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SCULK_STONE_IRON_ORE = register("sculk_stone_iron_ore", id -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SCULK_STONE_COPPER_ORE = register("sculk_stone_copper_ore", id -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SCULK_STONE_GOLD_ORE = register("sculk_stone_gold_ore", id -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SCULK_STONE_REDSTONE_ORE = register("sculk_stone_redstone_ore", id -> new RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id)).randomTicks().lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 9 : 0)));
    public static final DeferredBlock<Block> SCULK_STONE_EMERALD_ORE = register("sculk_stone_emerald_ore", id -> new DropExperienceBlock(UniformInt.of(5, 10), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SCULK_STONE_LAPIS_ORE = register("sculk_stone_lapis_ore", id -> new DropExperienceBlock(UniformInt.of(4, 8), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> SCULK_STONE_DIAMOND_ORE = register("sculk_stone_diamond_ore", id -> new DropExperienceBlock(UniformInt.of(5, 10), BlockBehaviour.Properties.ofFullCopy(SCULK_STONE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> GLOOMSLATE_COAL_ORE = register("gloomslate_coal_ore", id -> new DropExperienceBlock(UniformInt.of(1, 4), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> GLOOMSLATE_IRON_ORE = register("gloomslate_iron_ore", id -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> GLOOMSLATE_COPPER_ORE = register("gloomslate_copper_ore", id -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> GLOOMSLATE_GOLD_ORE = register("gloomslate_gold_ore", id -> new DropExperienceBlock(ConstantInt.of(0), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> GLOOMSLATE_REDSTONE_ORE = register("gloomslate_redstone_ore", id -> new RedStoneOreBlock(BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id)).randomTicks().lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 9 : 0)));
    public static final DeferredBlock<Block> GLOOMSLATE_EMERALD_ORE = register("gloomslate_emerald_ore", id -> new DropExperienceBlock(UniformInt.of(5, 10), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> GLOOMSLATE_LAPIS_ORE = register("gloomslate_lapis_ore", id -> new DropExperienceBlock(UniformInt.of(4, 8), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));
    public static final DeferredBlock<Block> GLOOMSLATE_DIAMOND_ORE = register("gloomslate_diamond_ore", id -> new DropExperienceBlock(UniformInt.of(5, 10), BlockBehaviour.Properties.ofFullCopy(GLOOMSLATE.get()).setId(blockKey(id))));

    public static final DeferredBlock<Block> GLOWING_FLOWERS = register("glowing_flowers", id -> new GlowingFlowersBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.PINK_PETALS).setId(blockKey(id)).lightLevel(_ -> 9).mapColor(MapColor.GLOW_LICHEN)));
    public static final DeferredBlock<Block> GLOWING_GRASS = register("glowing_grass", id -> new GlowingGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).setId(blockKey(id)).lightLevel(_ -> 11).mapColor(MapColor.GLOW_LICHEN)));
    public static final DeferredBlock<Block> GLOOMY_GRASS = register("gloomy_grass", id -> new GloomyGrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SHORT_GRASS).setId(blockKey(id)).lightLevel(_ -> 1).mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final DeferredBlock<Block> GLOOMY_CACTUS = register("gloomy_cactus", id -> new GloomyCactusBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).strength(0.5f).lightLevel(_ -> 6).sound(SoundType.WOOL).mapColor(MapColor.COLOR_ORANGE)));
    public static final DeferredBlock<SculkTendrilsBlock> SCULK_TENDRILS = register("sculk_tendrils", id -> new SculkTendrilsBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).sound(SoundType.SCULK).randomTicks().noCollision().instabreak().mapColor(MapColor.COLOR_CYAN)));
    public static final DeferredBlock<SculkTendrilsPlantBlock> SCULK_TENDRILS_PLANT = BLOCKS.register("sculk_tendrils_plant", id -> new SculkTendrilsPlantBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).sound(SoundType.SCULK).noCollision().instabreak().mapColor(MapColor.COLOR_CYAN)));
    public static final DeferredBlock<SculkVinesBlock> SCULK_VINES = register("sculk_vines", id -> new SculkVinesBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).sound(SoundType.SCULK).randomTicks().noCollision().instabreak().mapColor(MapColor.COLOR_CYAN)));
    public static final DeferredBlock<SculkVinesPlantBlock> SCULK_VINES_PLANT = BLOCKS.register("sculk_vines_plant", id -> new SculkVinesPlantBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).sound(SoundType.SCULK).noCollision().instabreak().mapColor(MapColor.COLOR_CYAN)));
    public static final DeferredBlock<GlowingRootsBlock> GLOWING_ROOTS = register("glowing_roots", id -> new GlowingRootsBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).sound(SoundType.CAVE_VINES).randomTicks().noCollision().instabreak().mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<GlowingRootsPlantBlock> GLOWING_ROOTS_PLANT = BLOCKS.register("glowing_roots_plant", id -> new GlowingRootsPlantBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).sound(SoundType.CAVE_VINES).noCollision().instabreak().mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<GlowingVinesBlock> GLOWING_VINES = BLOCKS.register("glowing_vines", id -> new GlowingVinesBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).sound(SoundType.CAVE_VINES).randomTicks().noCollision().instabreak().mapColor(MapColor.COLOR_LIGHT_BLUE).noLootTable()));
    public static final DeferredBlock<GlowingVinesPlantBlock> GLOWING_VINES_PLANT = BLOCKS.register("glowing_vines_plant", id -> new GlowingVinesPlantBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).lightLevel(state -> state.getValue(GlowingVinesPlantBlock.BERRIES) ? 14 : 0).sound(SoundType.CAVE_VINES).noCollision().instabreak().mapColor(MapColor.COLOR_LIGHT_BLUE)));
    public static final DeferredBlock<Block> ICE_LILY = BLOCKS.register("ice_lily", id -> new IceLilyBlock(BlockBehaviour.Properties.of().setId(blockKey(id)).lightLevel(_ -> 7).sound(SoundType.LILY_PAD).noOcclusion().instabreak().mapColor(MapColor.COLOR_LIGHT_BLUE).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<Block> LILY_FLOWER = BLOCKS.register("lily_flower", id -> new LilyFlowerBlock(MobEffects.GLOWING, 6f, BlockBehaviour.Properties.of().setId(blockKey(id)).lightLevel(_ -> 7).noCollision().instabreak().sound(SoundType.GRASS).mapColor(MapColor.PLANT).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> ANCIENT_VASE = register("ancient_vase", id -> new AncientVaseBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE).setId(blockKey(id)).strength(2f, 6f).sound(DDSounds.VASE).mapColor(MapColor.DEEPSLATE)));
    public static final DeferredBlock<Block> GLOOMSLATE_POT = BLOCKS.register("gloomslate_pot", id -> new GloomslatePotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DECORATED_POT).setId(blockKey(id)).mapColor(MapColor.TERRACOTTA_BROWN)));
    public static final DeferredItem<BlockItem> GLOOMSLATE_POT_ITEM = DDItems.ITEMS.registerSimpleBlockItem("gloomslate_pot", GLOOMSLATE_POT, () -> new Item.Properties().component(DataComponents.POT_DECORATIONS, PotDecorations.EMPTY).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY));
    public static final DeferredBlock<Block> INFESTED_SCULK = register("infested_sculk", id -> new InfestedSculkBlock(Blocks.SCULK, BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).setId(blockKey(id))));
    public static final DeferredBlock<Block> SCULK_JAW = register("sculk_jaw", id -> new SculkJawBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK).setId(blockKey(id)).randomTicks().mapColor(MapColor.COLOR_BLACK)));

    public static final DeferredBlock<OthersidePortalBlock> OTHERSIDE_PORTAL = BLOCKS.register("otherside_portal", id -> new OthersidePortalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).setId(blockKey(id)).noLootTable()));

    private static net.minecraft.resources.ResourceKey<Block> blockKey(net.minecraft.resources.Identifier id) {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, id);
    }

    private static <T extends Block> DeferredBlock<T> register(String name, java.util.function.Function<net.minecraft.resources.Identifier, T> properties) {
        DeferredBlock<T> block = BLOCKS.register(name, properties);
        DDItems.ITEMS.registerSimpleBlockItem(name, block);
        return block;
    }
}
