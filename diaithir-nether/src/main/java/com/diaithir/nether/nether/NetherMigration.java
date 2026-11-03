package com.diaithir.nether.nether;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

/** Legacy-id migration table. Empty by default; fill from a registry dump of an old world. */
public final class NetherMigration {
    private static final Map<Identifier, Identifier> LEGACY = new HashMap<>();

    private NetherMigration() {}

    public static void addLegacy(String oldPath, String newPath) {
        LEGACY.put(Identifier.fromNamespaceAndPath(NetherContent.MOD_ID, oldPath),
                Identifier.fromNamespaceAndPath(NetherContent.MOD_ID, newPath));
    }

    public static Identifier remap(Identifier id) {
        return LEGACY.getOrDefault(id, id);
    }
}
