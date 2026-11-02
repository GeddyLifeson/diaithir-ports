package com.diaithir.overworld_biomes.naturescompass;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import com.mojang.serialization.Codec;

import java.util.function.Supplier;

@Mod(BiomeCompassMod.MOD_ID)
public final class BiomeCompassMod {
    public static final String MOD_ID = "diaithir_overworld_biomes";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Supplier<DataComponentType<Identifier>> TARGET_BIOME = COMPONENTS.registerComponentType("target_biome",
            b -> b.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC));
    public static final Supplier<DataComponentType<GlobalPos>> LOCATED_POS = COMPONENTS.registerComponentType("located_pos",
            b -> b.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC));
    public static final Supplier<DataComponentType<Integer>> SEARCH_STATE = COMPONENTS.registerComponentType("search_state",
            b -> b.persistent(Codec.INT).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<Item, BiomeCompassItem> BIOME_COMPASS = ITEMS.registerItem("biome_compass",
            BiomeCompassItem::new, p -> p.stacksTo(1));

    public static final Supplier<CreativeModeTab> MAIN_TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MOD_ID + ".main"))
            .icon(() -> BIOME_COMPASS.get().getDefaultInstance())
            .displayItems((params, out) -> out.accept(BIOME_COMPASS.get()))
            .build());

    public BiomeCompassMod(IEventBus bus) {
        ITEMS.register(bus);
        COMPONENTS.register(bus);
        TABS.register(bus);
        bus.addListener(RegisterPayloadHandlersEvent.class, CompassNetwork::register);
        bus.addListener(RegisterEvent.class, MigrationTable::onRegister);
        net.neoforged.fml.ModContainer c = ModLoadingContext.get().getActiveContainer();
        c.registerConfig(ModConfig.Type.SERVER, CompassConfig.SERVER_SPEC);
        c.registerConfig(ModConfig.Type.CLIENT, CompassConfig.CLIENT_SPEC);
        CompassEvents.init();
    }
}
