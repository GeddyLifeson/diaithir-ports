package net.diaithir.hostile.hordes.neoforge;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.diaithir.hostile.hordes.init.HordeHoardModGameRules;
import net.diaithir.hostile.hordes.init.HordeHoardModMobEffects;
import net.diaithir.hostile.hordes.neoforge.platform.NeoForgePlatformHelper;
import net.diaithir.hostile.hordes.platform.Services;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.RegisterEvent;

public class HordeHoardModNeoForge {

    public static void init(IEventBus modEventBus) {
        HordeHoardConstants.LOGGER.info("Horde Hoard loaded! ({})", Services.PLATFORM.getPlatformName());

        // DeferredRegister for mob effects — registers when the mod event bus dispatches.
        NeoForgePlatformHelper.MOB_EFFECTS.register(modEventBus);

        // Game rules can't be registered at static init / mod constructor time on NeoForge —
        // BuiltInRegistries.GAME_RULE is already frozen. RegisterEvent fires for each
        // registry during the registration phase, before freezing — that's the right hook.
        modEventBus.addListener(HordeHoardModNeoForge::onRegister);

        // Static init for the mob effect Holder field (DeferredRegister handles the actual
        // registry write).
        HordeHoardModMobEffects.touch();

        // Game-bus event subscriptions are in HordeHoardNeoForgeEvents (statically subscribed
        // via @EventBusSubscriber).
    }

    private static void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.GAME_RULE)) {
            HordeHoardModGameRules.register();
        }
    }
}
