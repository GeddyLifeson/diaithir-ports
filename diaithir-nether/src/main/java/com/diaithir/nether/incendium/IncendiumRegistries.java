package com.diaithir.nether.incendium;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IncendiumRegistries {
    public static final String NS = "diaithir_nether";
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NS);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, NS);

    public static final DeferredHolder<Item, Item> SLAG_INGOT = simple("slag_ingot");
    public static final DeferredHolder<Item, Item> EMBER_BLADE = simple("ember_blade");
    public static final DeferredHolder<Item, Item> FOUNDRY_HAMMER = simple("foundry_hammer");
    public static final DeferredHolder<Item, Item> HEAT_LANCE = simple("heat_lance");
    public static final DeferredHolder<Item, Item> CINDER_SHIELD = simple("cinder_shield");
    public static final DeferredHolder<Item, Item> SLAG_HELMET = simple("slag_helmet");
    public static final DeferredHolder<Item, Item> SLAG_CHESTPLATE = simple("slag_chestplate");
    public static final DeferredHolder<Item, Item> SLAG_LEGGINGS = simple("slag_leggings");
    public static final DeferredHolder<Item, Item> SLAG_BOOTS = simple("slag_boots");

    public static final DeferredHolder<EntityType<?>, EntityType<EmberSentry>> EMBER_SENTRY = ENTITIES.register("ember_sentry",
            () -> EntityType.Builder.of(EmberSentry::new, MobCategory.MONSTER).sized(0.6F, 1.8F).fireImmune()
                    .build(key("ember_sentry")));
    public static final DeferredHolder<EntityType<?>, EntityType<SlagBrute>> SLAG_BRUTE = ENTITIES.register("slag_brute",
            () -> EntityType.Builder.of(SlagBrute::new, MobCategory.MONSTER).sized(1.1F, 2.6F).fireImmune()
                    .build(key("slag_brute")));
    public static final DeferredHolder<EntityType<?>, EntityType<AshLurker>> ASH_LURKER = ENTITIES.register("ash_lurker",
            () -> EntityType.Builder.of(AshLurker::new, MobCategory.MONSTER).sized(0.7F, 1.4F).fireImmune()
                    .build(key("ash_lurker")));

    private static ResourceKey<EntityType<?>> key(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(NS, path));
    }

    private static net.neoforged.neoforge.registries.DeferredItem<Item> simple(String path) {
        return ITEMS.registerItem(path, Item::new);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        ENTITIES.register(bus);
    }

    private IncendiumRegistries() {}
}
