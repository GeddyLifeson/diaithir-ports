package rearth.belts;

import rearth.belts.items.BeltItem;
import rearth.belts.items.TooltipBlockItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public class ItemContent {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Belts.MOD_ID, Registries.ITEM);
    
    public static final RegistrySupplier<Item> CHUTE = ITEMS.register("chute", () -> new TooltipBlockItem(BlockContent.CHUTE_BLOCK.get(), properties("chute")));
    public static final RegistrySupplier<Item> CONVEYOR_SUPPORT = ITEMS.register("conveyor_support", () -> new TooltipBlockItem(BlockContent.CONVEYOR_SUPPORT_BLOCK.get(), properties("conveyor_support")));
    public static final RegistrySupplier<Item> BELT = ITEMS.register("belt", () -> new BeltItem(properties("belt"), 1));
    public static final RegistrySupplier<Item> IMPROVED_BELT = ITEMS.register("improved_belt", () -> new BeltItem(properties("improved_belt"), 2));
    public static final RegistrySupplier<Item> BELT_TIER3 = ITEMS.register("belt_tier3", () -> new BeltItem(properties("belt_tier3"), 3));
    public static final RegistrySupplier<Item> BELT_TIER4 = ITEMS.register("belt_tier4", () -> new BeltItem(properties("belt_tier4"), 4));
    public static final RegistrySupplier<Item> FOUNDRY_SPLITTER = ITEMS.register("foundry_splitter", () -> new TooltipBlockItem(BlockContent.FOUNDRY_SPLITTER_BLOCK.get(), properties("foundry_splitter")));
    public static final RegistrySupplier<Item> FOUNDRY_TUNNEL = ITEMS.register("foundry_tunnel", () -> new TooltipBlockItem(BlockContent.FOUNDRY_TUNNEL_BLOCK.get(), properties("foundry_tunnel")));
    public static final RegistrySupplier<Item> FOUNDRY_LOADER = ITEMS.register("foundry_loader", () -> new TooltipBlockItem(BlockContent.FOUNDRY_LOADER_BLOCK.get(), properties("foundry_loader")));

    public static RegistrySupplier<Item> beltForTier(int tier) {
        return switch (tier) {
            case 2 -> IMPROVED_BELT;
            case 3 -> BELT_TIER3;
            case 4 -> BELT_TIER4;
            default -> BELT;
        };
    }

    private static Item.Properties properties(String path) {
        return new Item.Properties().arch$tab(ItemGroupContent.BELTS_GROUP).setId(ResourceKey.create(Registries.ITEM, Belts.id(path)));
    }
    
}
