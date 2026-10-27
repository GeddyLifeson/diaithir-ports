package com.diaithir.addons.oreexcavation;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.zurrtum.create.infrastructure.config.CStress;

import com.diaithir.addons.oreexcavation.block.DrillBlock;
import com.diaithir.addons.oreexcavation.block.ExtractorBlock;
import com.diaithir.addons.oreexcavation.block.IOBlock;
import com.diaithir.addons.oreexcavation.block.KineticInputBlock;
import com.diaithir.addons.oreexcavation.block.MultiblockBlock;
import com.diaithir.addons.oreexcavation.block.SampleDrillBlock;
import com.diaithir.addons.oreexcavation.block.entity.DrillBlockEntity;
import com.diaithir.addons.oreexcavation.block.entity.ExtractorBlockEntity;
import com.diaithir.addons.oreexcavation.block.entity.IOBlockEntity;
import com.diaithir.addons.oreexcavation.block.entity.KineticInputBlockEntity;
import com.diaithir.addons.oreexcavation.block.entity.MultiblockBlockEntity;
import com.diaithir.addons.oreexcavation.block.entity.SampleDrillBlockEntity;
import com.diaithir.addons.oreexcavation.item.MultiBlockItem;
import com.diaithir.addons.oreexcavation.item.OreVeinAtlasItem;
import com.diaithir.addons.oreexcavation.item.OreVeinFinderItem;
import com.diaithir.addons.oreexcavation.menu.OreVeinAtlasMenu;

/** Direct DeferredRegister registration (the Create port has no Registrate). */
public class Registration {
	private static final String ID = CreateOreExcavation.MODID;
	private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
	private static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
	private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);
	private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);

	private static BlockBehaviour.Properties metal() {
		return BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.COPPER).requiresCorrectToolForDrops()
				.strength(10).dynamicShape().noOcclusion();
	}

	private static BlockBehaviour.Properties ghost() {
		return metal().noLootTable().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK);
	}

	public static final DeferredBlock<DrillBlock> DRILL_BLOCK = BLOCKS.registerBlock("drilling_machine", DrillBlock::new, () -> metal());
	public static final DeferredBlock<KineticInputBlock> KINETIC_INPUT = BLOCKS.registerBlock("kinetic_input", KineticInputBlock::new, () -> ghost());
	public static final DeferredBlock<MultiblockBlock> GHOST_BLOCK = BLOCKS.registerBlock("multiblock", MultiblockBlock::new, () -> ghost());
	public static final DeferredBlock<IOBlock> IO_BLOCK = BLOCKS.registerBlock("io_block", IOBlock::new, () -> ghost());
	public static final DeferredBlock<ExtractorBlock> EXTRACTOR_BLOCK = BLOCKS.registerBlock("extractor", ExtractorBlock::new, () -> metal());
	public static final DeferredBlock<SampleDrillBlock> SAMPLE_DRILL_BLOCK = BLOCKS.registerBlock("sample_drill", SampleDrillBlock::new, () -> metal());

	private interface Ctor<T> {
		T create(BlockEntityType<?> t, net.minecraft.core.BlockPos p, net.minecraft.world.level.block.state.BlockState s);
	}

	@SuppressWarnings("unchecked")
	private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> tile(String name,
			Ctor<T> c, Supplier<? extends Block> b) {
		BlockEntityType<T>[] self = new BlockEntityType[1];
		return TILES.register(name, () -> self[0] = new BlockEntityType<T>((p, s) -> c.create(self[0], p, s), java.util.Set.of(b.get())));
	}

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DrillBlockEntity>> DRILL_TILE = tile("drill", DrillBlockEntity::new, DRILL_BLOCK);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KineticInputBlockEntity>> KINETIC_INPUT_TILE = tile("kinetic_input", KineticInputBlockEntity::new, KINETIC_INPUT);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiblockBlockEntity>> GHOST_TILE = tile("multiblock", MultiblockBlockEntity::new, GHOST_BLOCK);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<IOBlockEntity>> IO_TILE = tile("io", IOBlockEntity::new, IO_BLOCK);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExtractorBlockEntity>> EXTRACTOR_TILE = tile("extractor", ExtractorBlockEntity::new, EXTRACTOR_BLOCK);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SampleDrillBlockEntity>> SAMPLE_DRILL_TILE = tile("sample_drill", SampleDrillBlockEntity::new, SAMPLE_DRILL_BLOCK);

	private static <B extends Block, I extends Item> DeferredItem<I> blockItem(DeferredBlock<B> b, Function<Item.Properties, I> f) {
		return ITEMS.registerItem(b.getId().getPath(), f, (java.util.function.UnaryOperator<Item.Properties>) p -> p.stacksTo(1).useBlockDescriptionPrefix());
	}

	public static final DeferredItem<MultiBlockItem> DRILL_ITEM = blockItem(DRILL_BLOCK, p -> new MultiBlockItem(DRILL_BLOCK.get(), p));
	public static final DeferredItem<MultiBlockItem> EXTRACTOR_ITEM = blockItem(EXTRACTOR_BLOCK, p -> new MultiBlockItem(EXTRACTOR_BLOCK.get(), p));
	public static final DeferredItem<net.minecraft.world.item.BlockItem> SAMPLE_DRILL_ITEM = blockItem(SAMPLE_DRILL_BLOCK, p -> new net.minecraft.world.item.BlockItem(SAMPLE_DRILL_BLOCK.get(), p));

	public static final DeferredItem<Item> NORMAL_DRILL_ITEM = ITEMS.registerItem("drill", Item::new, (java.util.function.UnaryOperator<Item.Properties>) p -> p.stacksTo(1));
	public static final DeferredItem<Item> DIAMOND_DRILL_ITEM = ITEMS.registerItem("diamond_drill", Item::new, (java.util.function.UnaryOperator<Item.Properties>) p -> p.stacksTo(1));
	public static final DeferredItem<Item> NETHERITE_DRILL_ITEM = ITEMS.registerItem("netherite_drill", Item::new, (java.util.function.UnaryOperator<Item.Properties>) p -> p.stacksTo(1));
	public static final DeferredItem<Item> RAW_DIAMOND = ITEMS.registerItem("raw_diamond", Item::new);
	public static final DeferredItem<Item> RAW_EMERALD = ITEMS.registerItem("raw_emerald", Item::new);
	public static final DeferredItem<Item> RAW_REDSTONE = ITEMS.registerItem("raw_redstone", Item::new);
	public static final DeferredItem<OreVeinFinderItem> VEIN_FINDER_ITEM = ITEMS.registerItem("vein_finder", OreVeinFinderItem::new, (java.util.function.UnaryOperator<Item.Properties>) p -> p.stacksTo(1));
	public static final DeferredItem<OreVeinAtlasItem> VEIN_ATLAS_ITEM = ITEMS.registerItem("vein_atlas", OreVeinAtlasItem::new, (java.util.function.UnaryOperator<Item.Properties>) p -> p.stacksTo(1));

	public static final DeferredHolder<MenuType<?>, MenuType<OreVeinAtlasMenu>> VEIN_ATLAS_MENU = MENUS.register("vein_atlas",
			() -> IMenuTypeExtension.create((wid, inv, buf) -> new OreVeinAtlasMenu(Registration.VEIN_ATLAS_MENU.get(), wid, inv)));

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("create_ore_excavation",
			() -> CreativeModeTab.builder().title(Component.translatable("itemGroup." + ID))
					.icon(() -> new ItemStack(DIAMOND_DRILL_ITEM.get()))
					.displayItems((params, out) -> {
						out.accept(DRILL_ITEM.get());
						out.accept(EXTRACTOR_ITEM.get());
						out.accept(SAMPLE_DRILL_ITEM.get());
						out.accept(NORMAL_DRILL_ITEM.get());
						out.accept(DIAMOND_DRILL_ITEM.get());
						out.accept(NETHERITE_DRILL_ITEM.get());
						out.accept(RAW_DIAMOND.get());
						out.accept(RAW_EMERALD.get());
						out.accept(RAW_REDSTONE.get());
						out.accept(VEIN_FINDER_ITEM.get());
						out.accept(VEIN_ATLAS_ITEM.get());
					}).build());

	public static void register(IEventBus bus) {
		BLOCKS.register(bus);
		ITEMS.register(bus);
		TILES.register(bus);
		MENUS.register(bus);
		TABS.register(bus);
	}

	public static void postRegister() {
		// The multiblock input blocks pull stress through the Create port's stress table.
		CStress.setImpact(KINETIC_INPUT.get(), 1.0);
	}
}
