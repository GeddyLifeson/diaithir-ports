package fr.tathan.sky_aesthetics.helper;

import net.neoforged.fml.ModList;

public class PlatformHelper {
    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }
}
