package net.diaithir.materials;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Building materials (plans/BUILDING_MATERIALS.md): every built stage of the shared property table is a block (cement powder is an item), with strength
 * from the table. Recipes are Foundry station recipes in this jar's data, one per stage, made at the station of the stage's Age.
 */
@Mod(DiaithirMaterials.ID)
public final class DiaithirMaterials {
    public static final String ID = "diaithir_materials";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    /** Built block stages, by table id. */
    public static final Map<String, DeferredBlock<Block>> BLOCK_BY_ID = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<BlockItem>> BLOCK_ITEM_BY_ID = new LinkedHashMap<>();
    static {
        for (Materials.Row row : Materials.all()) {
            if (!row.built() || row.id().equals("cement_powder")) continue;
            DeferredBlock<Block> b = BLOCKS.registerSimpleBlock(row.id(), () -> properties(row));
            BLOCK_BY_ID.put(row.id(), b);
            BLOCK_ITEM_BY_ID.put(row.id(), ITEMS.registerSimpleBlockItem(b));
        }
    }

    /** Strength from the table; the class picks sound and colour, glass lets light through, soft stages break by hand. */
    private static BlockBehaviour.Properties properties(Materials.Row row) {
        BlockBehaviour.Properties p = BlockBehaviour.Properties.of().strength(row.hardness(), row.resistance());
        switch (row.cls()) {
            case "timber" -> p.mapColor(MapColor.WOOD).sound(SoundType.WOOD);
            case "steel" -> p.mapColor(MapColor.METAL).sound(SoundType.METAL);
            case "glass" -> p.mapColor(MapColor.NONE).sound(SoundType.GLASS).noOcclusion();
            case "insulation" -> p.mapColor(MapColor.WOOL).sound(SoundType.WOOL);
            case "roofing" -> p.mapColor(row.id().equals("roofing_thatch") ? MapColor.COLOR_YELLOW : MapColor.STONE).sound(row.id().equals("roofing_thatch") ? SoundType.GRASS : SoundType.STONE);
            default -> p.mapColor(MapColor.STONE).sound(SoundType.STONE);
        }
        if (row.hardness() >= 1.5F && !row.cls().equals("timber")) p.requiresCorrectToolForDrops();
        return p;
    }
    public static final DeferredItem<Item> CEMENT_POWDER = ITEMS.registerSimpleItem("cement_powder");

    public DiaithirMaterials(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) for (DeferredItem<BlockItem> i : BLOCK_ITEM_BY_ID.values()) e.accept(i.get());
            if (e.getTabKey() == CreativeModeTabs.INGREDIENTS) e.accept(CEMENT_POWDER.get());
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> e.getDispatcher().register(Commands.literal(ID)
                .then(Commands.literal("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = MaterialsSelfTest.run();
                    String line = fails.isEmpty() ? "Materials self test PASS" : "Materials self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                }))));
    }
}
