package net.diaithir.portable;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Tiered storage blocks, backpacks and upgrades, and a bottomless single-item store. Clean-room original. */
@Mod(DiaithirPortable.ID)
public final class DiaithirPortable {
    public static final String ID = "diaithirportable";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ID);

    public static final Map<Tier, DeferredBlock<StorageBlock>> STORAGE = new EnumMap<>(Tier.class);
    public static final Map<Tier, DeferredItem<BlockItem>> STORAGE_ITEMS = new EnumMap<>(Tier.class);
    public static final Map<Tier, DeferredItem<BackpackItem>> BACKPACKS = new EnumMap<>(Tier.class);
    public static final Map<UpgradeKind, DeferredItem<UpgradeItem>> UPGRADES = new EnumMap<>(UpgradeKind.class);

    static {
        for (Tier t : Tier.values()) {
            DeferredBlock<StorageBlock> b = BLOCKS.registerBlock(t.id + "_storage", p -> new StorageBlock(p, t),
                    () -> BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F, 6.0F).sound(SoundType.WOOD));
            STORAGE.put(t, b);
            STORAGE_ITEMS.put(t, ITEMS.registerSimpleBlockItem(b));
        }
        for (UpgradeKind k : UpgradeKind.values()) {
            UPGRADES.put(k, ITEMS.registerItem(k.id, p -> new UpgradeItem(p, k)));
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StorageBlockEntity>> STORAGE_BE = BLOCK_ENTITIES.register("storage",
            () -> new BlockEntityType<>(StorageBlockEntity::new, STORAGE.values().stream().map(DeferredBlock::get).toArray(StorageBlock[]::new)));

    public static final DeferredHolder<MenuType<?>, MenuType<PortableMenu>> PORTABLE_MENU = MENUS.register("portable",
            () -> IMenuTypeExtension.create(PortableMenu::client));

    public static final DeferredItem<DeepNullItem> DEEP_NULL = ITEMS.registerItem("deep_null", DeepNullItem::new);

    static {
        for (Tier t : Tier.values()) {
            BACKPACKS.put(t, ITEMS.registerItem(t.id + "_backpack", p -> new BackpackItem(STORAGE.get(t).get(), p)));
        }
    }

    public DiaithirPortable(IEventBus bus, ModContainer container) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
        PortableData.COMPONENTS.register(bus);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
        bus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                List<Item> all = new ArrayList<>();
                STORAGE_ITEMS.values().forEach(i -> all.add(i.get()));
                BACKPACKS.values().forEach(i -> all.add(i.get()));
                UPGRADES.values().forEach(i -> all.add(i.get()));
                all.add(DEEP_NULL.get());
                all.forEach(e::accept);
            }
        });
        bus.addListener(RegisterMenuScreensEvent.class, e -> e.register(PORTABLE_MENU.get(), PortableScreen::new));
    }

    public static FeatureFlags unusedFlags() {
        return null;
    }
}
