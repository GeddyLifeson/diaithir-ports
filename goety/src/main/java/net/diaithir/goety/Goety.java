package net.diaithir.goety;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Goety for NeoForge 26.1: dark rites, wands, foci and the dead who answer them (port of Goety by Polarice3, MIT). */
@Mod(Goety.MOD_ID)
public final class Goety {
    public static final String MOD_ID = "goety";

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("goety", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.goety"))
        .icon(() -> new ItemStack(GoetyItems.PLAIN.get("dark_ingot").get()))
        .displayItems((params, out) -> {
            GoetyItems.ALL.forEach(i -> out.accept(i.get()));
            GoetyEntities.SPAWN_EGGS.values().forEach(i -> out.accept(i.get()));
        })
        .build());

    public Goety(IEventBus bus) {
        GoetyBlocks.register(bus);
        GoetyComponents.register(bus);
        GoetyEntities.register(bus);
        GoetyItems.register(bus);
        TABS.register(bus);
        NeoForge.EVENT_BUS.register(GoetyEvents.class);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> SelfTest.register(e.getDispatcher()));
    }
}
