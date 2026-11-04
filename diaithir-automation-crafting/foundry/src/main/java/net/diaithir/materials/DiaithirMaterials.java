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
 * Building materials (plans/BUILDING_MATERIALS.md): block strength comes from the shared property table. Built so far: the concrete line,
 * pozzolan (II), kiln cement powder (IV), Portland (V), reinforced (VI), precast (VII). Recipes are Foundry station recipes in this jar's data.
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
        for (String id : List.of("concrete_pozzolan", "concrete_portland", "concrete_reinforced", "concrete_precast")) {
            Materials.Row row = Materials.get(id);
            DeferredBlock<Block> b = BLOCKS.registerSimpleBlock(id, () -> BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                    .strength(row.hardness(), row.resistance()).sound(SoundType.STONE).requiresCorrectToolForDrops());
            BLOCK_BY_ID.put(id, b);
            BLOCK_ITEM_BY_ID.put(id, ITEMS.registerSimpleBlockItem(b));
        }
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
