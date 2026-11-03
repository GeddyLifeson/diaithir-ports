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
            "easy_villagers_breeder",
            "easy_villagers_converter",
            "easy_villagers_farmer",
            "easy_villagers_incubator",
            "easy_villagers_inventory_viewer",
            "easy_villagers_iron_farm",
            "easy_villagers_trader"
    };
    private static final String[] ITEM_IDS = {
            "jewelry_citrine",
            "jewelry_citrine_necklace",
            "jewelry_citrine_ring",
            "jewelry_copper_ring",
            "jewelry_diamond_necklace",
            "jewelry_diamond_ring",
            "jewelry_emerald_necklace",
            "jewelry_gold_ring",
            "jewelry_iron_ring",
            "jewelry_jade",
            "jewelry_jade_necklace",
            "jewelry_jade_ring",
            "jewelry_netherite_citrine_necklace",
            "jewelry_netherite_citrine_ring",
            "jewelry_netherite_jade_necklace",
            "jewelry_netherite_jade_ring",
            "jewelry_netherite_ruby_necklace",
            "jewelry_netherite_ruby_ring",
            "jewelry_netherite_sapphire_necklace",
            "jewelry_netherite_sapphire_ring",
            "jewelry_netherite_tanzanite_necklace",
            "jewelry_netherite_tanzanite_ring",
            "jewelry_netherite_topaz_necklace",
            "jewelry_netherite_topaz_ring",
            "jewelry_ruby_necklace",
            "jewelry_sapphire_necklace",
            "jewelry_socket_mount",
            "jewelry_tanzanite",
            "jewelry_tanzanite_necklace",
            "jewelry_tanzanite_ring",
            "jewelry_topaz_necklace",
            "jewelry_unique_arcane_necklace",
            "jewelry_unique_arcane_ring",
            "jewelry_unique_archer_necklace",
            "jewelry_unique_archer_ring",
            "jewelry_unique_attack_necklace",
            "jewelry_unique_attack_ring",
            "jewelry_unique_crit_necklace",
            "jewelry_unique_crit_ring",
            "jewelry_unique_dex_necklace",
            "jewelry_unique_dex_ring",
            "jewelry_unique_fire_necklace",
            "jewelry_unique_fire_ring",
            "jewelry_unique_frost_necklace",
            "jewelry_unique_frost_ring",
            "jewelry_unique_healing_necklace",
            "jewelry_unique_healing_ring",
            "jewelry_unique_spell_necklace",
            "jewelry_unique_spell_ring",
            "jewelry_unique_tank_necklace",
            "jewelry_unique_tank_ring",
            "utilitarian_restraining_order",
            "utilitarian_slime_bucket",
            "utilitarian_tiny_charcoal",
            "utilitarian_tiny_coal",
            "utilitarian_trowel",
            "utilitarian_unname_tag",
            "crafting_on_a_stick_anvil",
            "crafting_on_a_stick_cartography_table",
            "crafting_on_a_stick_chipped_anvil",
            "crafting_on_a_stick_damaged_anvil",
            "crafting_on_a_stick_grindstone",
            "crafting_on_a_stick_loom",
            "crafting_on_a_stick_smithing_table",
            "crafting_on_a_stick_stonecutter",
            "easy_villagers_villager",
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
