package net.diaithir.worldsys.aircrafttuning;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

/** Wires the aircraft tuning compat layer and reports which optional mods are present. */
public final class AircraftTuningFeature {
    private static final Logger LOGGER = LogUtils.getLogger();

    private AircraftTuningFeature() {
    }

    public static boolean aircraftPresent() {
        return ModList.get().isLoaded("immersive_aircraft");
    }

    public static boolean createPresent() {
        return ModList.get().isLoaded("create");
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, e ->
                LOGGER.info("Aircraft tuning: aircraft mod {}, Create {}",
                        aircraftPresent() ? "present" : "absent", createPresent() ? "present" : "absent"));
    }
}
