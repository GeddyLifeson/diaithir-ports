package com.diaithir.overworldbiomes.naturesspirit.util;

import com.diaithir.overworldbiomes.naturesspirit.registration.NSBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.LinkedHashMap;
import java.util.Map;

public class NSVillagers {

  public static final String MOD_ID = "natures_spirit";

  /**
   * Villager type keys. Villager types are a registry in 26.1 (Registries.VILLAGER_TYPE);
   * the module should register these keys (e.g. via DeferredRegister on the villager type registry)
   * and wire them from NeoForge events.
   */
  public static final ResourceKey<?> WISTERIA = ResourceKey.create(Registries.VILLAGER_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "wisteria"));
  public static final ResourceKey<?> CYPRESS = ResourceKey.create(Registries.VILLAGER_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "cypress"));
  public static final ResourceKey<?> ADOBE = ResourceKey.create(Registries.VILLAGER_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "adobe"));
  public static final ResourceKey<?> COCONUT = ResourceKey.create(Registries.VILLAGER_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, "coconut"));

  /** Vanilla desert villager type key. */
  public static final ResourceKey<?> DESERT = ResourceKey.create(Registries.VILLAGER_TYPE, Identifier.withDefaultNamespace("desert"));

  /** Biome to villager type mapping data, to be applied by the module when villager types are available. */
  public static final Map<ResourceKey<Biome>, ResourceKey<?>> BIOME_TO_TYPE = new LinkedHashMap<>();

  public static void registerVillagers() {
    BIOME_TO_TYPE.put(NSBiomes.WISTERIA_FOREST, WISTERIA);
    BIOME_TO_TYPE.put(NSBiomes.CYPRESS_FIELDS, CYPRESS);
    BIOME_TO_TYPE.put(NSBiomes.CARNATION_FIELDS, CYPRESS);
    BIOME_TO_TYPE.put(NSBiomes.LAVENDER_FIELDS, CYPRESS);
    BIOME_TO_TYPE.put(NSBiomes.STRATIFIED_DESERT, ADOBE);
    BIOME_TO_TYPE.put(NSBiomes.LIVELY_DUNES, ADOBE);
    BIOME_TO_TYPE.put(NSBiomes.BLOOMING_DUNES, ADOBE);
    BIOME_TO_TYPE.put(NSBiomes.DRYLANDS, DESERT);
    BIOME_TO_TYPE.put(NSBiomes.WOODED_DRYLANDS, DESERT);
    BIOME_TO_TYPE.put(NSBiomes.TROPICAL_SHORES, COCONUT);
    // Wandering trader offers (red moss block, bluebell, anemone, lotus flower) should be added
    // by the module through NeoForge's WandererTradesEvent.
  }

}
