package com.diaithir.overworldbiomes.naturesspirit;

import com.diaithir.overworldbiomes.naturesspirit.blocks.block_entities.PizzaToppingVariant;
import com.diaithir.overworldbiomes.naturesspirit.config.NSConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class NatureSpirit {

  public static final String MOD_ID = "natures_spirit";
  public static final Logger LOGGER = LoggerFactory.getLogger("Nature's Spirit");
  public static final Identifier EAT_PIZZA_SLICE = Identifier.fromNamespaceAndPath(MOD_ID, "eat_pizza_slice");
  public static final Identifier EAT_CHEESE = Identifier.fromNamespaceAndPath(MOD_ID, "eat_cheese");
  public static final ResourceKey<Registry<PizzaToppingVariant>> PIZZA_TOPPING_VARIANT = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID, "pizza_topping_variant"));
  public static final NSConfig CONFIG = new NSConfig(FMLPaths.CONFIGDIR.get().resolve(MOD_ID + ".toml"));

  public static final Map<Identifier, Boolean> BUILTIN_PACKS = new LinkedHashMap<>();

  static {
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_vanilla_trees"), CONFIG.vanilla_trees_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_flower_forest"), CONFIG.flower_forest_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_birch_forest"), CONFIG.birch_forest_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_jungle"), CONFIG.jungle_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_swamp"), CONFIG.swamp_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_badlands"), CONFIG.badlands_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_mountain_biomes"), CONFIG.mountain_biomes_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_savannas"), CONFIG.savanna_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_dark_forest"), CONFIG.dark_forest_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "modified_windswept_hills"), CONFIG.windswept_hills_toggle);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "plank_consistency"), false);
    BUILTIN_PACKS.put(Identifier.fromNamespaceAndPath(MOD_ID, "emissive_ores_compatibility"), false);
  }

  public static void init(IEventBus modBus) {
  }

  public static void onInitialize(IEventBus modBus) {
    init(modBus);
  }
}
