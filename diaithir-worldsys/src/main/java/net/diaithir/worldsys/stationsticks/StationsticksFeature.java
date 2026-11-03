package net.diaithir.worldsys.stationsticks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

/** Pocket workstation sticks: using one opens the matching vanilla menu without any block. */
public final class StationsticksFeature {
    private static final String ID = "diaithirworldsys";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    private static final List<DeferredItem<Item>> ALL = new ArrayList<>();

    private interface Factory {
        net.minecraft.world.inventory.AbstractContainerMenu create(int id, net.minecraft.world.entity.player.Inventory inv, ContainerLevelAccess access);
    }

    private static void stick(String name, String title, Factory factory) {
        ALL.add(ITEMS.registerItem("crafting_on_a_stick_" + name, p -> new StickItem(p, title, factory), p -> p.stacksTo(1)));
    }

    static {
        stick("cartography_table", "Cartography Table", CartographyTableMenu::new);
        stick("grindstone", "Grindstone", GrindstoneMenu::new);
        stick("loom", "Loom", LoomMenu::new);
        stick("smithing_table", "Smithing Table", SmithingMenu::new);
        stick("stonecutter", "Stonecutter", StonecutterMenu::new);
        stick("anvil", "Anvil", AnvilMenu::new);
        stick("chipped_anvil", "Anvil", AnvilMenu::new);
        stick("damaged_anvil", "Anvil", AnvilMenu::new);
    }

    private static final class StickItem extends Item {
        private final Component title;
        private final Factory factory;

        StickItem(Item.Properties props, String title, Factory factory) {
            super(props);
            this.title = Component.literal(title);
            this.factory = factory;
        }

        @Override
        public InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
            if (!level.isClientSide()) {
                player.openMenu(new SimpleMenuProvider((id, inv, p) -> factory.create(id, inv, ContainerLevelAccess.create(level, BlockPos.ZERO)), title));
            }
            return InteractionResult.SUCCESS;
        }
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) ALL.forEach(i -> e.accept(i.get()));
        });
    }

    private StationsticksFeature() {}
}
