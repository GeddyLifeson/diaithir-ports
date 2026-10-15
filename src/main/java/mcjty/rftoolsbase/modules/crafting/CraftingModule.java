package mcjty.rftoolsbase.modules.crafting;

import mcjty.lib.modules.IModule;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbase.modules.crafting.client.GuiCraftingCard;
import mcjty.rftoolsbase.modules.crafting.data.CraftingCardData;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardContainer;
import mcjty.rftoolsbase.modules.crafting.items.CraftingCardItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

import static mcjty.rftoolsbase.RFToolsBase.tab;
import static mcjty.rftoolsbase.setup.Registration.*;

public class CraftingModule implements IModule {

    public static final DeferredItem<Item> CRAFTING_CARD = ITEMS.register("crafting_card", tab(CraftingCardItem::new));
    public static final Supplier<MenuType<CraftingCardContainer>> CONTAINER_CRAFTING_CARD = CONTAINERS.register("crafting_card", CraftingModule::createCraftingContainer);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CraftingCardData>> ITEM_CRAFTINGCARD_DATA = COMPONENTS.registerComponentType(
            "craftingcard_data",
            builder -> builder
                    .persistent(CraftingCardData.CODEC)
                    .networkSynchronized(CraftingCardData.STREAM_CODEC));

    private static MenuType<CraftingCardContainer> createCraftingContainer() {
        return IMenuTypeExtension.create((windowId, inv, data) -> {
            Player player = SafeClientTools.getClientPlayer();
            CraftingCardContainer container = new CraftingCardContainer(windowId, player.blockPosition(), player);
            container.setupInventories(null, inv);
            return container;
        });
    }

    public CraftingModule(IEventBus bus) {
        bus.addListener(this::registerMenuScreens);
    }

    @Override
    public void init(FMLCommonSetupEvent event) {

    }

    @Override
    public void initClient(FMLClientSetupEvent event) {
    }

    public void registerMenuScreens(RegisterMenuScreensEvent event) {
        GuiCraftingCard.register(event);
    }

    @Override
    public void initConfig(IEventBus bus) {

    }

}
