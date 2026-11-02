package com.diaithir.end.nullscape;

import java.util.Map;
import net.minecraft.resources.Identifier;

public final class NullscapeMigration {
 public static final Map<String, String> LEGACY_ALIASES = Map.ofEntries(
 Map.entry("shattered_plateau", "shattered_plateau"),
 Map.entry("hollow_spires", "hollow_spires"),
 Map.entry("ashen_basin", "ashen_basin"),
 Map.entry("veil_reach", "veil_reach"),
 Map.entry("fractured_ridge", "fractured_ridge"),
 Map.entry("echo_flats", "echo_flats"),
 Map.entry("end", "end_surreal"),
 Map.entry("void_stone", "void_stone"),
 Map.entry("ash_sediment", "ash_sediment"),
 Map.entry("rubble", "rubble"),
 Map.entry("stone_spike", "stone_spike"),
 Map.entry("sediment_patch", "sediment_patch"));

 /**
 * Resolves a legacy path to the current identifier, or null if there is no alias.
 */
 public static Identifier resolve(String legacyPath) {
 String target = LEGACY_ALIASES.get(legacyPath);
 if (target == null) return null;
 return NullscapeConstants.id(target);
 }

 private NullscapeMigration() {}
}
