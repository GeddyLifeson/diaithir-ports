package ewewukek.musketmod;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.util.Util;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;

public class Items {
    public static final Item MUSKET = new MusketItem(gun("musket", Config.musketDurability));
    public static final Item MUSKET_WITH_BAYONET = new MusketItem(gun("musket_with_bayonet", Config.musketDurability)
        .attributes(MusketItem.createBayonetAttributes()));
    public static final Item MUSKET_WITH_SCOPE = new ScopedMusketItem(gun("musket_with_scope", Config.scopedMusketDurability));
    public static final Item BLUNDERBUSS = new BlunderbussItem(gun("blunderbuss", Config.blunderbussDurability));
    public static final Item PISTOL = new PistolItem(gun("pistol", Config.pistolDurability));
    public static final Item CARTRIDGE = new CartridgeItem(properties("cartridge"));

    public static final Identifier EMPTY_SLOT_MUSKET = MusketMod.resource("container/slot/musket");
    public static final Identifier EMPTY_SLOT_SPYGLASS = MusketMod.resource("container/slot/spyglass");

    public static final Item MUSKET_UPGRADE = new SmithingTemplateItem(
        Component.translatable(Util.makeDescriptionId("item",
            MusketMod.resource("musket")))
            .withStyle(SmithingTemplateItem.DESCRIPTION_FORMAT),
        Component.translatable(Util.makeDescriptionId("item",
            MusketMod.resource("musket_upgrade.ingredients")))
            .withStyle(SmithingTemplateItem.DESCRIPTION_FORMAT),
        Component.translatable(Util.makeDescriptionId("item",
            MusketMod.resource("musket_upgrade.base_slot_description"))),
        Component.translatable(Util.makeDescriptionId("item",
            MusketMod.resource("musket_upgrade.additions_slot_description"))),
        List.of(EMPTY_SLOT_MUSKET),
        List.of(SmithingTemplateItem.EMPTY_SLOT_SWORD, EMPTY_SLOT_SPYGLASS),
        properties("musket_upgrade_smithing_template")
    );

    private static Item.Properties properties(String path) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, MusketMod.resource(path)));
    }

    private static Item.Properties gun(String path, int durability) {
        return properties(path).durability(durability).enchantable(14);
    }

    public static void registerDataComponentTypes(BiConsumer<String, DataComponentType<?>> helper) {
        helper.accept("loaded", GunItem.LOADED);
        helper.accept("loading_stage", GunItem.LOADING_STAGE);
    }

    public static void register(BiConsumer<String, Item> helper) {
        helper.accept("musket", MUSKET);
        helper.accept("musket_with_bayonet", MUSKET_WITH_BAYONET);
        helper.accept("musket_with_scope", MUSKET_WITH_SCOPE);
        helper.accept("blunderbuss", BLUNDERBUSS);
        helper.accept("pistol", PISTOL);
        helper.accept("cartridge", CARTRIDGE);
        helper.accept("musket_upgrade_smithing_template", MUSKET_UPGRADE);
    }

    public static void addToCreativeTab(ResourceKey<CreativeModeTab> tab, Consumer<Item> helper) {
        if (tab == CreativeModeTabs.COMBAT) {
            helper.accept(MUSKET);
            helper.accept(MUSKET_WITH_BAYONET);
            helper.accept(MUSKET_WITH_SCOPE);
            helper.accept(BLUNDERBUSS);
            helper.accept(PISTOL);
            helper.accept(CARTRIDGE);
        }
        if (tab == CreativeModeTabs.INGREDIENTS) {
            helper.accept(MUSKET_UPGRADE);
        }
    }
}
