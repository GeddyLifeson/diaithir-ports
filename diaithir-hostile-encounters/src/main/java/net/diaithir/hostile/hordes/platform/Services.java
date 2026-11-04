package net.diaithir.hostile.hordes.platform;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.diaithir.hostile.hordes.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException(
                        "Failed to load service for " + clazz.getName()));
        HordeHoardConstants.LOGGER.debug("Loaded {} for service {}",
                loadedService.getClass().getName(), clazz.getName());
        return loadedService;
    }
}
