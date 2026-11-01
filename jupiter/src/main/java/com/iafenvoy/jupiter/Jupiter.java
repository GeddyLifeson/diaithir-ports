package com.iafenvoy.jupiter;

import com.iafenvoy.jupiter.compat.ExtraConfigManager;
import com.iafenvoy.jupiter.compat.clothconfig.ClothConfigLoader;
import com.iafenvoy.jupiter.compat.forgeconfigspec.ConfigSpecLoader;
import com.iafenvoy.jupiter.config.ConfigSource;
import com.iafenvoy.jupiter.internal.JupiterSettings;
import com.iafenvoy.jupiter.network.ClientConfigNetwork;
import com.iafenvoy.jupiter.network.ServerConfigNetwork;
import com.iafenvoy.jupiter.network.payload.ConfigErrorPayload;
import com.iafenvoy.jupiter.network.payload.ConfigRequestPayload;
import com.iafenvoy.jupiter.network.payload.ConfigSyncPayload;
import com.iafenvoy.jupiter.test.TestConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class Jupiter {
    public static final String MOD_ID = "jupiter";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init(boolean development) {
        JupiterProxies.SERVER_NETWORKING.registerPayloadType(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);
        JupiterProxies.SERVER_NETWORKING.registerPayloadType(ConfigRequestPayload.TYPE, ConfigRequestPayload.CODEC);
        JupiterProxies.SERVER_NETWORKING.registerPayloadType(ConfigErrorPayload.TYPE, ConfigErrorPayload.CODEC);

        ConfigManager.getInstance().registerServerConfigHandler(JupiterSettings.INSTANCE, ServerConfigManager.PermissionChecker.IS_OPERATOR);
        if (development) ConfigManager.getInstance().registerConfigHandler(new TestConfig());

        Platform platform = JupiterProxies.PLATFORM;
        if (platform.isModLoaded("forge") || platform.isModLoaded("neoforge") || platform.isModLoaded("forgeconfigapiport")) {
            LOGGER.info("Config spec system detected, register to Jupiter Config System.");
            try {
                ExtraConfigManager.registerScanner(ConfigSource.NIGHT_CONFIG, ConfigSpecLoader::scanConfig);
            } catch (Exception e) {
                LOGGER.error("Failed to register config spec loader", e);
            }
        }
        if (platform.isModLoaded("cloth-config") || platform.isModLoaded("cloth-config2") || platform.isModLoaded("cloth_config") || platform.isModLoaded("cloth_config2")) {
            LOGGER.info("Cloth Config API detected, register to Jupiter Config System.");
            try {
                ExtraConfigManager.registerScanner(ConfigSource.CLOTH_CONFIG, ClothConfigLoader::scanConfig);
            } catch (Exception e) {
                LOGGER.error("Failed to register Cloth Config loader", e);
            }
        }
    }

    public static void process() {
        ServerConfigNetwork.init();
    }

    public static void processClient() {
        ClientConfigNetwork.init();
    }
}
