package com.diaithir.addons.newage.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class CNAConfig {
    private static final ClientConfig CLIENT;
    private static final ServerConfig SERVER;
    private static final ModConfigSpec CLIENT_SPEC;
    private static final ModConfigSpec SERVER_SPEC;

    static {
        var c = new ModConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT = c.getLeft();
        CLIENT_SPEC = c.getRight();
        var s = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SERVER = s.getLeft();
        SERVER_SPEC = s.getRight();
    }

    public static ClientConfig getClient() {
        return CLIENT;
    }

    public static ServerConfig getServer() {
        return SERVER;
    }

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }
}
