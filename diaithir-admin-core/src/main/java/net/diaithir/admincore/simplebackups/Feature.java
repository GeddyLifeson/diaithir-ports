// Ported from Simple Backups (Apache-2.0) https://github.com/ChaoticTrials/SimpleBackups
// Modifications: Feature entry point; direct ModConfigSpec/NeoForge bus wiring, no @Mod class, ZIP-only, no client/network/compat parts.
package net.diaithir.admincore.simplebackups;

import net.diaithir.admincore.simplebackups.config.CommonConfig;
import net.diaithir.admincore.simplebackups.config.ServerConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, "diaithir_admin_simplebackups-common.toml");
        container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, "diaithir_admin_simplebackups-server.toml");

        if (CommonConfig.backupsDisabledByJvmArg()) {
            SimpleBackups.LOGGER.info("##########################################");
            SimpleBackups.LOGGER.info("#  Backups are disabled by JVM argument  #");
            SimpleBackups.LOGGER.info("##########################################");
        }
    }
}
