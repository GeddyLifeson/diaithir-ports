package vectorwing.farmersdelight.common.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.BlockShapes;
import vectorwing.farmersdelight.common.block.*;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public class ModBlocks
{
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, FarmersDelight.MODID);

	private static ResourceKey<Block> blockKey(Identifier id) {
		return ResourceKey.create(Registries.BLOCK, id);
	}

	private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
		return (state) -> state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
	}

	private static ToIntFunction<BlockState> glowingFeastBlockEmission() {
		return (state) -> state.getValue(FeastBlock.SERVINGS) * 3;
	}

	// Workstations
	public static final Supplier<Block> STOVE = BLOCKS.register("stove",
		id -> new StoveBlock(Block.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(id)).lightLevel(litBlockEmission(13))));
	public static final Supplier<Block> COOKING_POT = BLOCKS.register("cooking_pot",
		id -> new CookingPotBlock(Block.Properties.of().setId(blockKey(id)).mapColor(MapColor.METAL).strength(0.5F, 6.0F).sound(SoundType.LANTERN)));
	public static final Supplier<Block> SKILLET = BLOCKS.register("skillet",
		id -> new SkilletBlock(Block.Properties.of().setId(blockKey(id)).mapColor(MapColor.METAL).strength(0.5F, 6.0F).sound(SoundType.LANTERN)));
	public static final Supplier<Block> WOODEN_BASKET = BLOCKS.register("wooden_basket",
		id -> new BasketBlock(Block.Properties.of().setId(blockKey(id)).strength(1.5F).sound(SoundType.WOOD)));
	public static final Supplier<Block> BAMBOO_BASKET = BLOCKS.register("bamboo_basket",
		id -> new BasketBlock(Block.Properties.of().setId(blockKey(id)).strength(1.5F).sound(SoundType.BAMBOO_WOOD)));
	public static final Supplier<Block> CUTTING_BOARD = BLOCKS.register("cutting_board",
		id -> new CuttingBoardBlock(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).strength(2.0F).sound(SoundType.WOOD)));

	// Crop Storage
	public static final Supplier<Block> CARROT_CRATE = BLOCKS.register("carrot_crate",
		id -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final Supplier<Block> POTATO_CRATE = BLOCKS.register("potato_crate",
		id -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final Supplier<Block> BEETROOT_CRATE = BLOCKS.register("beetroot_crate",
		id -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final Supplier<Block> CABBAGE_CRATE = BLOCKS.register("cabbage_crate",
		id -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final Supplier<Block> TOMATO_CRATE = BLOCKS.register("tomato_crate",
		id -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final Supplier<Block> ONION_CRATE = BLOCKS.register("onion_crate",
		id -> new Block(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
	public static final Supplier<Block> RICE_BALE = BLOCKS.register("rice_bale",
		id -> new RiceBaleBlock(Block.Properties.ofFullCopy(Blocks.HAY_BLOCK).setId(blockKey(id))));
	public static final Supplier<Block> RICE_BAG = BLOCKS.register("rice_bag",
		id -> new Block(Block.Properties.ofFullCopy(Blocks.WHITE_WOOL).setId(blockKey(id))));
	public static final Supplier<Block> STRAW_BALE = BLOCKS.register("straw_bale",
		id -> new StrawBaleBlock(Block.Properties.ofFullCopy(Blocks.HAY_BLOCK).setId(blockKey(id))));

	// Building
	public static final Supplier<Block> ROPE = BLOCKS.register("rope",
		id -> new RopeBlock(Block.Properties.ofFullCopy(Blocks.BROWN_CARPET).setId(blockKey(id)).noCollision().noOcclusion().strength(0.2F).sound(SoundType.WOOL)));
	public static final Supplier<Block> SAFETY_NET = BLOCKS.register("safety_net",
		id -> new SafetyNetBlock(Block.Properties.ofFullCopy(Blocks.BROWN_CARPET).setId(blockKey(id)).strength(0.2F).sound(SoundType.WOOL)));
	public static final Supplier<Block> ROPE_FENCE = BLOCKS.register("rope_fence",
		id -> new RopeFenceBlock(Block.Properties.ofFullCopy(Blocks.OAK_FENCE).setId(blockKey(id)).strength(1.0F)));
	public static final Supplier<Block> ROPE_FENCE_GATE = BLOCKS.register("rope_fence_gate",
		id -> new RopeFenceGateBlock(Block.Properties.ofFullCopy(Blocks.OAK_FENCE).setId(blockKey(id)).strength(1.0F)));
	public static final Supplier<Block> OAK_CABINET = BLOCKS.register("oak_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id))));
	public static final Supplier<Block> SPRUCE_CABINET = BLOCKS.register("spruce_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id))));
	public static final Supplier<Block> BIRCH_CABINET = BLOCKS.register("birch_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id))));
	public static final Supplier<Block> JUNGLE_CABINET = BLOCKS.register("jungle_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id))));
	public static final Supplier<Block> ACACIA_CABINET = BLOCKS.register("acacia_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id))));
	public static final Supplier<Block> DARK_OAK_CABINET = BLOCKS.register("dark_oak_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id))));
	public static final Supplier<Block> MANGROVE_CABINET = BLOCKS.register("mangrove_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id))));
	public static final Supplier<Block> CHERRY_CABINET = BLOCKS.register("cherry_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id)).sound(SoundType.CHERRY_WOOD)));
	public static final Supplier<Block> BAMBOO_CABINET = BLOCKS.register("bamboo_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id)).sound(SoundType.BAMBOO_WOOD)));
	public static final Supplier<Block> CRIMSON_CABINET = BLOCKS.register("crimson_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id)).sound(SoundType.NETHER_WOOD)));
	public static final Supplier<Block> WARPED_CABINET = BLOCKS.register("warped_cabinet",
		id -> new CabinetBlock(Block.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(id)).sound(SoundType.NETHER_WOOD)));
	public static final Supplier<Block> CANVAS_RUG = BLOCKS.register("canvas_rug",
		id -> new CanvasRugBlock(Block.Properties.ofFullCopy(Blocks.WHITE_CARPET).setId(blockKey(id)).sound(SoundType.GRASS).strength(0.2F)));
	public static final Supplier<Block> TATAMI = BLOCKS.register("tatami",
		id -> new TatamiBlock(Block.Properties.ofFullCopy(Blocks.WHITE_WOOL).setId(blockKey(id))));
	public static final Supplier<Block> FULL_TATAMI_MAT = BLOCKS.register("full_tatami_mat",
		id -> new TatamiMatBlock(Block.Properties.ofFullCopy(Blocks.WHITE_WOOL).setId(blockKey(id)).strength(0.3F)));
	public static final Supplier<Block> HALF_TATAMI_MAT = BLOCKS.register("half_tatami_mat",
		id -> new TatamiHalfMatBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).setId(blockKey(id)).strength(0.3F).pushReaction(PushReaction.DESTROY)));

	public static final Supplier<Block> CANVAS_SIGN = BLOCKS.register("canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), null));
	public static final Supplier<Block> WHITE_CANVAS_SIGN = BLOCKS.register("white_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.WHITE));
	public static final Supplier<Block> ORANGE_CANVAS_SIGN = BLOCKS.register("orange_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.ORANGE));
	public static final Supplier<Block> MAGENTA_CANVAS_SIGN = BLOCKS.register("magenta_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.MAGENTA));
	public static final Supplier<Block> LIGHT_BLUE_CANVAS_SIGN = BLOCKS.register("light_blue_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.LIGHT_BLUE));
	public static final Supplier<Block> YELLOW_CANVAS_SIGN = BLOCKS.register("yellow_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.YELLOW));
	public static final Supplier<Block> LIME_CANVAS_SIGN = BLOCKS.register("lime_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.LIME));
	public static final Supplier<Block> PINK_CANVAS_SIGN = BLOCKS.register("pink_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.PINK));
	public static final Supplier<Block> GRAY_CANVAS_SIGN = BLOCKS.register("gray_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.GRAY));
	public static final Supplier<Block> LIGHT_GRAY_CANVAS_SIGN = BLOCKS.register("light_gray_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.LIGHT_GRAY));
	public static final Supplier<Block> CYAN_CANVAS_SIGN = BLOCKS.register("cyan_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.CYAN));
	public static final Supplier<Block> PURPLE_CANVAS_SIGN = BLOCKS.register("purple_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.PURPLE));
	public static final Supplier<Block> BLUE_CANVAS_SIGN = BLOCKS.register("blue_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.BLUE));
	public static final Supplier<Block> BROWN_CANVAS_SIGN = BLOCKS.register("brown_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.BROWN));
	public static final Supplier<Block> GREEN_CANVAS_SIGN = BLOCKS.register("green_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.GREEN));
	public static final Supplier<Block> RED_CANVAS_SIGN = BLOCKS.register("red_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.RED));
	public static final Supplier<Block> BLACK_CANVAS_SIGN = BLOCKS.register("black_canvas_sign",
		id -> new StandingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)), DyeColor.BLACK));

	public static final Supplier<Block> CANVAS_WALL_SIGN = BLOCKS.register("canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(CANVAS_SIGN.get().getLootTable()), null));
	public static final Supplier<Block> WHITE_CANVAS_WALL_SIGN = BLOCKS.register("white_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(WHITE_CANVAS_SIGN.get().getLootTable()), DyeColor.WHITE));
	public static final Supplier<Block> ORANGE_CANVAS_WALL_SIGN = BLOCKS.register("orange_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(ORANGE_CANVAS_SIGN.get().getLootTable()), DyeColor.ORANGE));
	public static final Supplier<Block> MAGENTA_CANVAS_WALL_SIGN = BLOCKS.register("magenta_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(MAGENTA_CANVAS_SIGN.get().getLootTable()), DyeColor.MAGENTA));
	public static final Supplier<Block> LIGHT_BLUE_CANVAS_WALL_SIGN = BLOCKS.register("light_blue_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(LIGHT_BLUE_CANVAS_SIGN.get().getLootTable()), DyeColor.LIGHT_BLUE));
	public static final Supplier<Block> YELLOW_CANVAS_WALL_SIGN = BLOCKS.register("yellow_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(YELLOW_CANVAS_SIGN.get().getLootTable()), DyeColor.YELLOW));
	public static final Supplier<Block> LIME_CANVAS_WALL_SIGN = BLOCKS.register("lime_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(LIME_CANVAS_SIGN.get().getLootTable()), DyeColor.LIME));
	public static final Supplier<Block> PINK_CANVAS_WALL_SIGN = BLOCKS.register("pink_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(PINK_CANVAS_SIGN.get().getLootTable()), DyeColor.PINK));
	public static final Supplier<Block> GRAY_CANVAS_WALL_SIGN = BLOCKS.register("gray_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(GRAY_CANVAS_SIGN.get().getLootTable()), DyeColor.GRAY));
	public static final Supplier<Block> LIGHT_GRAY_CANVAS_WALL_SIGN = BLOCKS.register("light_gray_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(LIGHT_GRAY_CANVAS_SIGN.get().getLootTable()), DyeColor.LIGHT_GRAY));
	public static final Supplier<Block> CYAN_CANVAS_WALL_SIGN = BLOCKS.register("cyan_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(CYAN_CANVAS_SIGN.get().getLootTable()), DyeColor.CYAN));
	public static final Supplier<Block> PURPLE_CANVAS_WALL_SIGN = BLOCKS.register("purple_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(PURPLE_CANVAS_SIGN.get().getLootTable()), DyeColor.PURPLE));
	public static final Supplier<Block> BLUE_CANVAS_WALL_SIGN = BLOCKS.register("blue_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(BLUE_CANVAS_SIGN.get().getLootTable()), DyeColor.BLUE));
	public static final Supplier<Block> BROWN_CANVAS_WALL_SIGN = BLOCKS.register("brown_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(BROWN_CANVAS_SIGN.get().getLootTable()), DyeColor.BROWN));
	public static final Supplier<Block> GREEN_CANVAS_WALL_SIGN = BLOCKS.register("green_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(GREEN_CANVAS_SIGN.get().getLootTable()), DyeColor.GREEN));
	public static final Supplier<Block> RED_CANVAS_WALL_SIGN = BLOCKS.register("red_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(RED_CANVAS_SIGN.get().getLootTable()), DyeColor.RED));
	public static final Supplier<Block> BLACK_CANVAS_WALL_SIGN = BLOCKS.register("black_canvas_wall_sign",
		id -> new WallCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_SIGN).setId(blockKey(id)).overrideLootTable(BLACK_CANVAS_SIGN.get().getLootTable()), DyeColor.BLACK));

	public static final Supplier<Block> HANGING_CANVAS_SIGN = BLOCKS.register("hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), null));
	public static final Supplier<Block> WHITE_HANGING_CANVAS_SIGN = BLOCKS.register("white_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.WHITE));
	public static final Supplier<Block> ORANGE_HANGING_CANVAS_SIGN = BLOCKS.register("orange_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.ORANGE));
	public static final Supplier<Block> MAGENTA_HANGING_CANVAS_SIGN = BLOCKS.register("magenta_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.MAGENTA));
	public static final Supplier<Block> LIGHT_BLUE_HANGING_CANVAS_SIGN = BLOCKS.register("light_blue_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.LIGHT_BLUE));
	public static final Supplier<Block> YELLOW_HANGING_CANVAS_SIGN = BLOCKS.register("yellow_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.YELLOW));
	public static final Supplier<Block> LIME_HANGING_CANVAS_SIGN = BLOCKS.register("lime_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.LIME));
	public static final Supplier<Block> PINK_HANGING_CANVAS_SIGN = BLOCKS.register("pink_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.PINK));
	public static final Supplier<Block> GRAY_HANGING_CANVAS_SIGN = BLOCKS.register("gray_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.GRAY));
	public static final Supplier<Block> LIGHT_GRAY_HANGING_CANVAS_SIGN = BLOCKS.register("light_gray_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.LIGHT_GRAY));
	public static final Supplier<Block> CYAN_HANGING_CANVAS_SIGN = BLOCKS.register("cyan_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.CYAN));
	public static final Supplier<Block> PURPLE_HANGING_CANVAS_SIGN = BLOCKS.register("purple_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.PURPLE));
	public static final Supplier<Block> BLUE_HANGING_CANVAS_SIGN = BLOCKS.register("blue_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.BLUE));
	public static final Supplier<Block> BROWN_HANGING_CANVAS_SIGN = BLOCKS.register("brown_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.BROWN));
	public static final Supplier<Block> GREEN_HANGING_CANVAS_SIGN = BLOCKS.register("green_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.GREEN));
	public static final Supplier<Block> RED_HANGING_CANVAS_SIGN = BLOCKS.register("red_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.RED));
	public static final Supplier<Block> BLACK_HANGING_CANVAS_SIGN = BLOCKS.register("black_hanging_canvas_sign",
		id -> new CeilingHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_HANGING_SIGN).setId(blockKey(id)), DyeColor.BLACK));

	public static final Supplier<Block> HANGING_CANVAS_WALL_SIGN = BLOCKS.register("wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(HANGING_CANVAS_SIGN.get().getLootTable()), null));
	public static final Supplier<Block> WHITE_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("white_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(WHITE_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.WHITE));
	public static final Supplier<Block> ORANGE_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("orange_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(ORANGE_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.ORANGE));
	public static final Supplier<Block> MAGENTA_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("magenta_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(MAGENTA_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.MAGENTA));
	public static final Supplier<Block> LIGHT_BLUE_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("light_blue_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(LIGHT_BLUE_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.LIGHT_BLUE));
	public static final Supplier<Block> YELLOW_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("yellow_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(YELLOW_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.YELLOW));
	public static final Supplier<Block> LIME_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("lime_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(LIME_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.LIME));
	public static final Supplier<Block> PINK_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("pink_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(PINK_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.PINK));
	public static final Supplier<Block> GRAY_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("gray_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(GRAY_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.GRAY));
	public static final Supplier<Block> LIGHT_GRAY_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("light_gray_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(LIGHT_GRAY_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.LIGHT_GRAY));
	public static final Supplier<Block> CYAN_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("cyan_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(CYAN_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.CYAN));
	public static final Supplier<Block> PURPLE_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("purple_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(PURPLE_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.PURPLE));
	public static final Supplier<Block> BLUE_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("blue_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(BLUE_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.BLUE));
	public static final Supplier<Block> BROWN_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("brown_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(BROWN_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.BROWN));
	public static final Supplier<Block> GREEN_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("green_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(GREEN_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.GREEN));
	public static final Supplier<Block> RED_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("red_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(RED_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.RED));
	public static final Supplier<Block> BLACK_HANGING_CANVAS_WALL_SIGN = BLOCKS.register("black_wall_hanging_canvas_sign",
		id -> new WallHangingCanvasSignBlock(Block.Properties.ofFullCopy(Blocks.SPRUCE_WALL_HANGING_SIGN).setId(blockKey(id)).overrideLootTable(BLACK_HANGING_CANVAS_SIGN.get().getLootTable()), DyeColor.BLACK));

	// Composting
	public static final Supplier<Block> BROWN_MUSHROOM_COLONY = BLOCKS.register("brown_mushroom_colony",
		id -> new MushroomColonyBlock(Items.BROWN_MUSHROOM.builtInRegistryHolder(), Block.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM).setId(blockKey(id))));
	public static final Supplier<Block> RED_MUSHROOM_COLONY = BLOCKS.register("red_mushroom_colony",
		id -> new MushroomColonyBlock(Items.RED_MUSHROOM.builtInRegistryHolder(), Block.Properties.ofFullCopy(Blocks.RED_MUSHROOM).setId(blockKey(id))));
	public static final Supplier<Block> ORGANIC_COMPOST = BLOCKS.register("organic_compost",
		id -> new OrganicCompostBlock(Block.Properties.ofFullCopy(Blocks.DIRT).setId(blockKey(id)).strength(1.2F).sound(SoundType.CROP)));
	public static final Supplier<Block> RICH_SOIL = BLOCKS.register("rich_soil",
		id -> new RichSoilBlock(Block.Properties.ofFullCopy(Blocks.DIRT).setId(blockKey(id)).randomTicks()));
	public static final Supplier<Block> RICH_SOIL_FARMLAND = BLOCKS.register("rich_soil_farmland",
		id -> new RichSoilFarmlandBlock(Block.Properties.ofFullCopy(Blocks.FARMLAND).setId(blockKey(id))));

	// Pastries
	public static final Supplier<Block> APPLE_PIE = BLOCKS.register("apple_pie",
		id -> new PieBlock(Block.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(id)), ModItems.APPLE_PIE_SLICE));
	public static final Supplier<Block> SWEET_BERRY_CHEESECAKE = BLOCKS.register("sweet_berry_cheesecake",
		id -> new PieBlock(Block.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(id)), ModItems.SWEET_BERRY_CHEESECAKE_SLICE));
	public static final Supplier<Block> CHOCOLATE_PIE = BLOCKS.register("chocolate_pie",
		id -> new PieBlock(Block.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(id)), ModItems.CHOCOLATE_PIE_SLICE));
	public static final Supplier<Block> PUMPKIN_PIE = BLOCKS.register("pumpkin_pie",
		id -> new PieBlock(Block.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(id)), ModItems.PUMPKIN_PIE_SLICE)
		{
			@Override
			public @NotNull ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
				return new ItemStack(Items.PUMPKIN_PIE);
			}
		});

	// Wild Crops
	public static final Supplier<Block> SANDY_SHRUB = BLOCKS.register("sandy_shrub",
		id -> new SandyShrubBlock(Block.Properties.ofFullCopy(Blocks.TALL_GRASS).setId(blockKey(id))));

	public static final Supplier<Block> WILD_CABBAGES = BLOCKS.register("wild_cabbages",
		id -> new WildCropBlock(MobEffects.STRENGTH, 6, Block.Properties.ofFullCopy(Blocks.TALL_GRASS).setId(blockKey(id))));
	public static final Supplier<Block> WILD_ONIONS = BLOCKS.register("wild_onions",
		id -> new WildCropBlock(MobEffects.FIRE_RESISTANCE, 6, Block.Properties.ofFullCopy(Blocks.TALL_GRASS).setId(blockKey(id))));
	public static final Supplier<Block> WILD_TOMATOES = BLOCKS.register("wild_tomatoes",
		id -> new WildCropBlock(MobEffects.POISON, 10, Block.Properties.ofFullCopy(Blocks.TALL_GRASS).setId(blockKey(id))));
	public static final Supplier<Block> WILD_CARROTS = BLOCKS.register("wild_carrots",
		id -> new WildCropBlock(MobEffects.MINING_FATIGUE, 6, Block.Properties.ofFullCopy(Blocks.TALL_GRASS).setId(blockKey(id))));
	public static final Supplier<Block> WILD_POTATOES = BLOCKS.register("wild_potatoes",
		id -> new WildCropBlock(MobEffects.NAUSEA, 8, Block.Properties.ofFullCopy(Blocks.TALL_GRASS).setId(blockKey(id))));
	public static final Supplier<Block> WILD_BEETROOTS = BLOCKS.register("wild_beetroots",
		id -> new WildCropBlock(MobEffects.WATER_BREATHING, 8, Block.Properties.ofFullCopy(Blocks.TALL_GRASS).setId(blockKey(id))));
	public static final Supplier<Block> WILD_RICE = BLOCKS.register("wild_rice",
		id -> new WildRiceBlock(Block.Properties.ofFullCopy(Blocks.TALL_GRASS).setId(blockKey(id))));

	// Crops
	public static final Supplier<Block> CABBAGE_CROP = BLOCKS.register("cabbages",
		id -> new CabbageBlock(Block.Properties.ofFullCopy(Blocks.WHEAT).setId(blockKey(id)).mapColor(MapColor.PLANT)));
	public static final Supplier<Block> ONION_CROP = BLOCKS.register("onions",
		id -> new OnionBlock(Block.Properties.ofFullCopy(Blocks.WHEAT).setId(blockKey(id)).mapColor(MapColor.PLANT)));
	public static final Supplier<Block> BUDDING_TOMATO_CROP = BLOCKS.register("budding_tomatoes",
		id -> new BuddingTomatoBlock(Block.Properties.ofFullCopy(Blocks.WHEAT).setId(blockKey(id)).mapColor(MapColor.PLANT)));
	public static final DeferredHolder<Block, TomatoBlock> TOMATO_CROP = BLOCKS.register("tomatoes",
		id -> new TomatoBlock(Block.Properties.of().setId(blockKey(id)).noCollision().randomTicks().instabreak().sound(SoundType.CROP)));
	public static final DeferredHolder<Block, HangingTomatoBlock> TOMATO_CROP_ON_ROPE = BLOCKS.register("tomatoes_on_rope",
		id -> new HangingTomatoBlock(Block.Properties.ofFullCopy(ModBlocks.TOMATO_CROP.get()).setId(blockKey(id)).pushReaction(PushReaction.NORMAL)));
	public static final Supplier<Block> RICE_CROP = BLOCKS.register("rice",
		id -> new RiceBlock(Block.Properties.ofFullCopy(Blocks.WHEAT).setId(blockKey(id)).mapColor(MapColor.PLANT).strength(0.2F)));
	public static final Supplier<Block> RICE_CROP_PANICLES = BLOCKS.register("rice_panicles",
		id -> new RicePaniclesBlock(Block.Properties.ofFullCopy(Blocks.WHEAT).setId(blockKey(id)).mapColor(MapColor.PLANT)));

	// Feasts
	public static final Supplier<Block> ROAST_CHICKEN_BLOCK = BLOCKS.register("roast_chicken_block",
		id -> new RotatedFeastBlock(Block.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(id)), ModItems.ROAST_CHICKEN, true, BlockShapes.ROAST_CHICKEN_SHAPES, BlockShapes.TRAY_SHAPE));
	public static final Supplier<Block> STUFFED_PUMPKIN_BLOCK = BLOCKS.register("stuffed_pumpkin_block",
		id -> new FeastBlock(Block.Properties.ofFullCopy(Blocks.PUMPKIN).setId(blockKey(id)), ModItems.STUFFED_PUMPKIN, false, true));
	public static final Supplier<Block> HONEY_GLAZED_HAM_BLOCK = BLOCKS.register("honey_glazed_ham_block",
		id -> new RotatedFeastBlock(Block.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(id)), ModItems.HONEY_GLAZED_HAM, true, BlockShapes.HONEY_GLAZED_HAM_SHAPES, BlockShapes.TRAY_SHAPE));
	public static final Supplier<Block> SHEPHERDS_PIE_BLOCK = BLOCKS.register("shepherds_pie_block",
		id -> new RotatedFeastBlock(Block.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(id)), ModItems.SHEPHERDS_PIE, true, BlockShapes.SHEPHERDS_PIE_SHAPES, BlockShapes.TRAY_SHAPE));
	public static final Supplier<Block> GLEAMING_SALAD_BLOCK = BLOCKS.register("gleaming_salad_block",
		id -> new GleamingSaladBlock(Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).lightLevel(glowingFeastBlockEmission()), ModItems.GLEAMING_SALAD, true));
	public static final Supplier<Block> RICE_ROLL_MEDLEY_BLOCK = BLOCKS.register("rice_roll_medley_block",
		id -> new RiceRollMedleyBlock(Block.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(id))));
}
