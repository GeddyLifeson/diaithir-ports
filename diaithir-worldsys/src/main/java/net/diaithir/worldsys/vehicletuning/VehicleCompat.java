package net.diaithir.worldsys.vehicletuning;

import net.neoforged.fml.ModList;

import java.util.List;

/** Presence checks for external vehicle and aircraft mods. */
public final class VehicleCompat {
    public static final List<String> VEHICLE_MODS = List.of("immersivevehicles", "mts", "iv");
    public static final List<String> AIRCRAFT_MODS = List.of("immersiveaircraft", "immersive_aircraft");

    public static List<String> loaded(List<String> ids) {
        ModList mods = ModList.get();
        if (mods == null) return List.of();
        return ids.stream().filter(mods::isLoaded).toList();
    }

    public static boolean hasVehicles() {
        return !loaded(VEHICLE_MODS).isEmpty();
    }

    public static boolean hasAircraft() {
        return !loaded(AIRCRAFT_MODS).isEmpty();
    }

    public static VehicleTuning.Profile activeProfile() {
        return hasVehicles() || hasAircraft() ? VehicleTuning.Profile.BOOSTED : VehicleTuning.Profile.DEFAULT;
    }

    private VehicleCompat() {}
}
