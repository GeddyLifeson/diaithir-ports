package net.diaithir.creatureutilities;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Fishing crates, ported from Fishing Crates by tsuniko (GPL-3.0). The crates reach fishing loot through the
 * global loot modifiers in data/diaithircreatureutilities/loot_modifiers; the loot inside is in loot_table/blocks.
 */
@Mod(CreatureUtilities.ID)
public final class CreatureUtilities {
    public static final String ID = "diaithircreatureutilities";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);

    public static final DeferredBlock<net.minecraft.world.level.block.Block> WOODEN_CRATE = BLOCKS.registerSimpleBlock("wooden_crate",
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(1.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<net.minecraft.world.level.block.Block> IRON_CRATE = BLOCKS.registerSimpleBlock("iron_crate",
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.METAL).sound(SoundType.METAL).strength(2.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<net.minecraft.world.level.block.Block> GOLDEN_CRATE = BLOCKS.registerSimpleBlock("golden_crate",
        () -> BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).sound(SoundType.METAL).strength(3.0F).requiresCorrectToolForDrops());

    public static final DeferredItem<BlockItem> WOODEN_CRATE_ITEM = ITEMS.registerSimpleBlockItem(WOODEN_CRATE);
    public static final DeferredItem<BlockItem> IRON_CRATE_ITEM = ITEMS.registerSimpleBlockItem(IRON_CRATE);
    public static final DeferredItem<BlockItem> GOLDEN_CRATE_ITEM = ITEMS.registerSimpleBlockItem(GOLDEN_CRATE);

    public CreatureUtilities(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        bus.addListener(CreatureUtilities::tab);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(CreatureUtilities::tooltip);
    }

    private static void tab(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            e.accept(WOODEN_CRATE_ITEM);
            e.accept(IRON_CRATE_ITEM);
            e.accept(GOLDEN_CRATE_ITEM);
        }
    }

    private static void tooltip(ItemTooltipEvent e) {
        var item = e.getItemStack().getItem();
        if (item == WOODEN_CRATE_ITEM.get() || item == IRON_CRATE_ITEM.get() || item == GOLDEN_CRATE_ITEM.get()) {
            e.getToolTip().add(Component.translatable("tooltip." + ID + ".crate_info").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
