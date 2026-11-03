package net.diaithir.worldsys.vehicletuning;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

/** Wires the vehicle tuning compat layer. */
public final class VehicleTuningFeature {
    private static final Logger LOG = LogUtils.getLogger();

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, e -> {
            var vehicles = VehicleCompat.loaded(VehicleCompat.VEHICLE_MODS);
            var aircraft = VehicleCompat.loaded(VehicleCompat.AIRCRAFT_MODS);
            if (!vehicles.isEmpty() || !aircraft.isEmpty()) LOG.info("Vehicle tuning active: vehicles={} aircraft={} profile={}", vehicles, aircraft, VehicleCompat.activeProfile());
        });
    }

    private VehicleTuningFeature() {}
}
