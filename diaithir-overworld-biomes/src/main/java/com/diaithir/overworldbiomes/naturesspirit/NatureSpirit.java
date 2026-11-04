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

  /** Registers all Nature's Spirit content on the mod event bus (called from the module constructor). */
  public static void init(IEventBus modBus) {
    com.diaithir.overworldbiomes.naturesspirit.registration.NSDataComponents.init(modBus);
    com.diaithir.overworldbiomes.naturesspirit.registration.NSSounds.init(modBus);
    com.diaithir.overworldbiomes.naturesspirit.registration.NSParticleTypes.init(modBus);
    com.diaithir.overworldbiomes.naturesspirit.registration.NSCriteria.init(modBus);
    com.diaithir.overworldbiomes.naturesspirit.registration.NSEntityTypes.init(modBus);
    com.diaithir.overworldbiomes.naturesspirit.registration.NSItemGroups.init(modBus);
    com.diaithir.overworldbiomes.naturesspirit.registration.NSWorldGen.init(modBus);
    // Touching these classes runs their static initialisers, which queue every block and item.
    com.diaithir.overworldbiomes.naturesspirit.registration.NSColoredBlocks.init(modBus);
    com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.init(modBus);
    com.diaithir.overworldbiomes.naturesspirit.util.NSVillagers.registerVillagers();
    com.diaithir.overworldbiomes.naturesspirit.util.NSEvents.init(modBus);
    modBus.addListener((net.neoforged.neoforge.registries.DataPackRegistryEvent.NewRegistry e) ->
        e.dataPackRegistry(PIZZA_TOPPING_VARIANT, PizzaToppingVariant.CODEC, PizzaToppingVariant.CODEC));
    modBus.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, (net.neoforged.neoforge.registries.RegisterEvent e) -> {
      if (e.getRegistryKey().equals(net.minecraft.core.registries.Registries.BLOCK)) {
        // Class initialisation constructs every block and item while the registries are open.
        com.diaithir.overworldbiomes.naturesspirit.registration.NSWoods.registerWoods();
        com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.flushBlocks(e);
      } else if (e.getRegistryKey().equals(net.minecraft.core.registries.Registries.ITEM)) {
        com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.flushItems(e);
      }
    });
    modBus.addListener(NatureSpirit::addCreativeEntries);
    modBus.addListener(NatureSpirit::attachSignBlocks);
    modBus.addListener(NatureSpirit::commonSetup);
    net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(NatureSpirit::fuelTime);
    if (net.neoforged.fml.loading.FMLEnvironment.getDist().isClient()) {
      com.diaithir.overworldbiomes.naturesspirit.client.NSClient.init(modBus);
    }
  }

  private static void addCreativeEntries(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent e) {
    var entries = com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.TAB_ENTRIES.get(e.getTabKey());
    if (entries == null) return;
    for (var t : entries) {
      try {
        var item = t.item().get();
        if (item == null || item.asItem() == net.minecraft.world.item.Items.AIR) continue;
        e.accept(item);
      } catch (RuntimeException ex) {
        LOGGER.debug("tab entry skipped: {}", ex.toString());
      }
    }
  }

  private static void attachSignBlocks(net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent e) {
    java.util.List<net.minecraft.world.level.block.Block> signs = new java.util.ArrayList<>();
    java.util.List<net.minecraft.world.level.block.Block> hanging = new java.util.ArrayList<>();
    for (var b : com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.PENDING_BLOCKS.values()) {
      if (b instanceof net.minecraft.world.level.block.StandingSignBlock || b instanceof net.minecraft.world.level.block.WallSignBlock) signs.add(b);
      if (b instanceof net.minecraft.world.level.block.CeilingHangingSignBlock || b instanceof net.minecraft.world.level.block.WallHangingSignBlock) hanging.add(b);
    }
    if (!signs.isEmpty()) e.modify(net.minecraft.world.level.block.entity.BlockEntityType.SIGN, signs.toArray(new net.minecraft.world.level.block.Block[0]));
    if (!hanging.isEmpty()) e.modify(net.minecraft.world.level.block.entity.BlockEntityType.HANGING_SIGN, hanging.toArray(new net.minecraft.world.level.block.Block[0]));
  }

  private static void commonSetup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) {
    e.enqueueWork(() -> {
      var fire = (net.minecraft.world.level.block.FireBlock) net.minecraft.world.level.block.Blocks.FIRE;
      for (var en : com.diaithir.overworldbiomes.naturesspirit.registration.sets.WoodSet.FLAMMABLES.entrySet()) {
        try {
          var m = net.minecraft.world.level.block.FireBlock.class.getDeclaredMethod("setFlammable", net.minecraft.world.level.block.Block.class, int.class, int.class);
          m.setAccessible(true);
          m.invoke(fire, en.getKey(), en.getValue()[0], en.getValue()[1]);
        } catch (ReflectiveOperationException ex) {
          LOGGER.warn("flammable not set for {}: {}", en.getKey(), ex.toString());
        }
      }
      for (var en : com.diaithir.overworldbiomes.naturesspirit.registration.sets.WoodSet.COMPOSTABLES.entrySet()) {
        net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.put(en.getKey().asItem(), (float) en.getValue());
      }
      for (var en : com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.COMPOSTABLE_BLOCKS.entrySet()) {
        net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.put(en.getKey().asItem(), (float) en.getValue());
      }
      for (var en : com.diaithir.overworldbiomes.naturesspirit.registration.NSRegistryHelper.COMPOSTABLES.entrySet()) {
        net.minecraft.world.level.block.ComposterBlock.COMPOSTABLES.put(en.getKey(), (float) en.getValue());
      }
    });
  }

  private static void fuelTime(net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent e) {
    var ticks = com.diaithir.overworldbiomes.naturesspirit.registration.sets.WoodSet.FUELS.get(e.getItemStack().getItem());
    if (ticks != null) e.setBurnTime(ticks);
  }

  public static void onInitialize(IEventBus modBus) {
    init(modBus);
  }
}
