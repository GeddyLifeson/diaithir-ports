package io.github.tr100000.researcher.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;

public final class ResearcherConfigs {
    private ResearcherConfigs() {}

    public static final ResearcherClientConfig client = new ResearcherClientConfig();
    public static final ResearcherServerConfig server = new ResearcherServerConfig();

    public static void init(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, client.spec);
        container.registerConfig(ModConfig.Type.COMMON, server.spec);
    }
}
