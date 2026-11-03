package net.diaithir.worldsys.legacy;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/** Plain items and blocks for ids that older worlds may hold, so saved stacks and placed blocks keep existing. They carry no behaviour. */
public final class LegacyFeature {
    private static final String ID = "diaithirworldsys";
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final List<DeferredItem<? extends Item>> ALL = new ArrayList<>();

    private static final String[] BLOCK_IDS = {
            "jewelry_deepslate_gem_vein",
            "jewelry_gem_vein",
            "jewelry_jewelers_kit",
            "utilitarian_black_soliciting_carpet",
            "utilitarian_black_trapped_soliciting_carpet",
            "utilitarian_blue_soliciting_carpet",
            "utilitarian_blue_trapped_soliciting_carpet",
            "utilitarian_brown_soliciting_carpet",
            "utilitarian_brown_trapped_soliciting_carpet",
            "utilitarian_cursed_grrass",
            "utilitarian_cyan_soliciting_carpet",
            "utilitarian_cyan_trapped_soliciting_carpet",
            "utilitarian_drit",
            "utilitarian_gray_soliciting_carpet",
            "utilitarian_gray_trapped_soliciting_carpet",
            "utilitarian_green_soliciting_carpet",
            "utilitarian_green_trapped_soliciting_carpet",
            "utilitarian_grrass",
            "utilitarian_inverted_lapis_lamp",
            "utilitarian_inverted_redstone_lamp",
            "utilitarian_lapis_lamp",
            "utilitarian_light_blue_soliciting_carpet",
            "utilitarian_light_blue_trapped_soliciting_carpet",
            "utilitarian_light_gray_soliciting_carpet",
            "utilitarian_light_gray_trapped_soliciting_carpet",
            "utilitarian_lime_soliciting_carpet",
            "utilitarian_lime_trapped_soliciting_carpet",
            "utilitarian_magenta_soliciting_carpet",
            "utilitarian_magenta_trapped_soliciting_carpet",
            "utilitarian_magnet",
            "utilitarian_no_raider_block",
            "utilitarian_no_soliciting_banner",
            "utilitarian_orange_soliciting_carpet",
            "utilitarian_orange_trapped_soliciting_carpet",
            "utilitarian_pink_soliciting_carpet",
            "utilitarian_pink_trapped_soliciting_carpet",
            "utilitarian_purple_soliciting_carpet",
            "utilitarian_purple_trapped_soliciting_carpet",
            "utilitarian_red_snad",
            "utilitarian_red_soliciting_carpet",
            "utilitarian_red_trapped_soliciting_carpet",
            "utilitarian_snad",
            "utilitarian_soul_snad",
            "utilitarian_sound_muffler",
            "utilitarian_tps_meter",
            "utilitarian_well_behaved_dropper",
            "utilitarian_white_soliciting_carpet",
            "utilitarian_white_trapped_soliciting_carpet",
            "utilitarian_yellow_soliciting_carpet",
            "utilitarian_yellow_trapped_soliciting_carpet",
            "simplemagnets_advanced_demagnetization_coil",
            "simplemagnets_basic_demagnetization_coil",
            "easy_villagers_auto_trader",
            "easy_villagers_converter",
            "easy_villagers_incubator",
            "easy_villagers_inventory_viewer",
            "easy_villagers_iron_farm"
    };
    private static final String[] ITEM_IDS = {
            "jewelry_socket_mount",
            "utilitarian_restraining_order",
            "utilitarian_slime_bucket",
            "utilitarian_tiny_charcoal",
            "utilitarian_tiny_coal",
            "utilitarian_trowel",
            "utilitarian_unname_tag",
            "guardvillagers_illusioner_spawn_egg"
    };

    private LegacyFeature() {}

    public static void register(IEventBus modBus) {
        for (String id : BLOCK_IDS) {
            DeferredBlock<Block> b = BLOCKS.registerBlock(id, Block::new, () -> BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.STONE));
            ALL.add(ITEMS.registerSimpleBlockItem(b));
        }
        for (String id : ITEM_IDS) ALL.add(ITEMS.registerSimpleItem(id));
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) ALL.forEach(i -> e.accept(i.get()));
        });
    }
}
