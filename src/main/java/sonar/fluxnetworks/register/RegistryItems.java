package sonar.fluxnetworks.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.common.item.*;

public class RegistryItems {
    private static final Identifier FLUX_DUST_KEY = FluxNetworks.location("flux_dust");
    private static final Identifier FLUX_CORE_KEY = FluxNetworks.location("flux_core");
    private static final Identifier FLUX_CONFIGURATOR_KEY = FluxNetworks.location("flux_configurator");
    private static final Identifier ADMIN_CONFIGURATOR_KEY = FluxNetworks.location("admin_configurator");

    public static final DeferredItem<BlockItem> FLUX_BLOCK = holder(RegistryBlocks.FLUX_BLOCK_KEY);
    public static final DeferredItem<FluxDeviceItem> FLUX_PLUG = holder(RegistryBlocks.FLUX_PLUG_KEY);
    public static final DeferredItem<FluxDeviceItem> FLUX_POINT = holder(RegistryBlocks.FLUX_POINT_KEY);
    public static final DeferredItem<FluxDeviceItem> FLUX_CONTROLLER = holder(RegistryBlocks.FLUX_CONTROLLER_KEY);
    public static final DeferredItem<FluxStorageItem> BASIC_FLUX_STORAGE = holder(RegistryBlocks.BASIC_FLUX_STORAGE_KEY);
    public static final DeferredItem<FluxStorageItem> HERCULEAN_FLUX_STORAGE = holder(RegistryBlocks.HERCULEAN_FLUX_STORAGE_KEY);
    public static final DeferredItem<FluxStorageItem> GARGANTUAN_FLUX_STORAGE = holder(RegistryBlocks.GARGANTUAN_FLUX_STORAGE_KEY);
    public static final DeferredItem<FluxDustItem> FLUX_DUST = holder(FLUX_DUST_KEY);
    public static final DeferredItem<Item> FLUX_CORE = holder(FLUX_CORE_KEY);
    public static final DeferredItem<ItemFluxConfigurator> FLUX_CONFIGURATOR = holder(FLUX_CONFIGURATOR_KEY);
    public static final DeferredItem<ItemAdminConfigurator> ADMIN_CONFIGURATOR = holder(ADMIN_CONFIGURATOR_KEY);

    static <T extends Item> DeferredItem<T> holder(Identifier location) {
        return DeferredItem.createItem(location);
    }

    static void register(RegisterEvent.RegisterHelper<Item> helper) {
        helper.register(RegistryBlocks.FLUX_BLOCK_KEY, new BlockItem(RegistryBlocks.FLUX_BLOCK.get(), props(RegistryBlocks.FLUX_BLOCK_KEY).useBlockDescriptionPrefix()));
        helper.register(RegistryBlocks.FLUX_PLUG_KEY, new FluxDeviceItem(RegistryBlocks.FLUX_PLUG.get(), props(RegistryBlocks.FLUX_PLUG_KEY).useBlockDescriptionPrefix()));
        helper.register(RegistryBlocks.FLUX_POINT_KEY, new FluxDeviceItem(RegistryBlocks.FLUX_POINT.get(), props(RegistryBlocks.FLUX_POINT_KEY).useBlockDescriptionPrefix()));
        helper.register(RegistryBlocks.FLUX_CONTROLLER_KEY, new FluxDeviceItem(RegistryBlocks.FLUX_CONTROLLER.get(), props(RegistryBlocks.FLUX_CONTROLLER_KEY).useBlockDescriptionPrefix()));
        helper.register(RegistryBlocks.BASIC_FLUX_STORAGE_KEY, new FluxStorageItem(RegistryBlocks.BASIC_FLUX_STORAGE.get(), props(RegistryBlocks.BASIC_FLUX_STORAGE_KEY).useBlockDescriptionPrefix()));
        helper.register(RegistryBlocks.HERCULEAN_FLUX_STORAGE_KEY, new FluxStorageItem(RegistryBlocks.HERCULEAN_FLUX_STORAGE.get(), props(RegistryBlocks.HERCULEAN_FLUX_STORAGE_KEY).useBlockDescriptionPrefix()));
        helper.register(RegistryBlocks.GARGANTUAN_FLUX_STORAGE_KEY, new FluxStorageItem(RegistryBlocks.GARGANTUAN_FLUX_STORAGE.get(), props(RegistryBlocks.GARGANTUAN_FLUX_STORAGE_KEY).useBlockDescriptionPrefix()));

        helper.register(FLUX_DUST_KEY, new FluxDustItem(props(FLUX_DUST_KEY)));
        helper.register(FLUX_CORE_KEY, new Item(props(FLUX_CORE_KEY)));

        helper.register(FLUX_CONFIGURATOR_KEY, new ItemFluxConfigurator(props(FLUX_CONFIGURATOR_KEY).stacksTo(1)));
        helper.register(ADMIN_CONFIGURATOR_KEY, new ItemAdminConfigurator(props(ADMIN_CONFIGURATOR_KEY).stacksTo(1)));
    }

    // 26.1 items need their id in their properties.
    private static Item.Properties props(Identifier key) {
        return new Item.Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, key));
    }

    private RegistryItems() {}
}
