package org.diaithir.mega;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * Volume removed from each deposit, kept per dimension, chunk and block id. A chunk holds {@link #INITIAL_M3} of each material to start with; the
 * dig engine draws it down and the yield follows (see {@link Assay}).
 */
public final class Deposits extends SavedData {
    public static final double INITIAL_M3 = 20_000;

    static final Codec<Deposits> CODEC = Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).xmap(Deposits::new, d -> d.removed);
    static final SavedDataType<Deposits> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(MegaMod.MOD_ID, "deposits"), Deposits::new, CODEC);

    private final Map<String, Double> removed;

    private Deposits() { this(Map.of()); }

    private Deposits(Map<String, Double> removed) { this.removed = new HashMap<>(removed); }

    public static Deposits get(MinecraftServer server) { return server.overworld().getDataStorage().computeIfAbsent(TYPE); }

    public static String key(String dim, int chunkX, int chunkZ, String blockId) { return dim + "|" + chunkX + "|" + chunkZ + "|" + blockId; }

    public Dig.Deposit deposit(String key) { return new Dig.Deposit(INITIAL_M3, INITIAL_M3 - removed.getOrDefault(key, 0.0)); }

    public void store(String key, Dig.Deposit d) {
        removed.put(key, d.initial() - d.left());
        setDirty();
    }
}
