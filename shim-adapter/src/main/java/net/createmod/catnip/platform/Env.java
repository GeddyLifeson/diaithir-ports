package net.createmod.catnip.platform;

import net.neoforged.fml.loading.FMLEnvironment;

public enum Env {
    CLIENT, SERVER;
    public static Env current() { return FMLEnvironment.getDist().isClient() ? CLIENT : SERVER; }
    public boolean isClient() { return this == CLIENT; }
    public boolean isServer() { return this == SERVER; }
}
