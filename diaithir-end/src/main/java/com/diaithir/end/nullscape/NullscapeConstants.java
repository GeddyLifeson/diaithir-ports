package com.diaithir.end.nullscape;

import net.minecraft.resources.Identifier;

public final class NullscapeConstants {
 public static final String MOD_ID = "diaithir_end";
 public static final String LEGACY_NS = "nullscape";
 public static final String CODEX_MOD = "codex";
 public static final String TELLUS_MOD = "tellus";
 public static final String TERRALITH_MOD = "terralith";
 public static final int AGE = 7;
 public static final int MAGIC_QUANTUM_J = 100;

 private NullscapeConstants() {}

 public static Identifier id(String path) {
 return Identifier.fromNamespaceAndPath(MOD_ID, path);
 }
}
