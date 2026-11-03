package com.diaithir.end.nullscape;

import net.neoforged.fml.ModList;

public final class NullscapeCompat {
 public static boolean codex() { return ModList.get().isLoaded(NullscapeConstants.CODEX_MOD); }
 public static boolean tellus() { return ModList.get().isLoaded(NullscapeConstants.TELLUS_MOD); }
 public static boolean terralith() { return ModList.get().isLoaded(NullscapeConstants.TERRALITH_MOD); }

 /** True when another End overhaul would be hard-replaced; new biomes then defer via biome modifier only. */
 public static boolean otherEndOverhaul() { return tellus() || terralith(); }

 private NullscapeCompat() {}
}
