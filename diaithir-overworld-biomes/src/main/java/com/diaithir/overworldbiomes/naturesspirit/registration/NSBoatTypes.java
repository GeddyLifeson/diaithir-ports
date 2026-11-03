package com.diaithir.overworldbiomes.naturesspirit.registration;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Boat type bookkeeping for Nature's Spirit wood types.
 * <p>
 * In Minecraft 26.1 the vanilla {@code Boat.Type} enum is closed and cannot be extended
 * the way the Fabric version did, so boat types are represented here by a lightweight
 * {@link BoatType} record that keeps the registry name and the planks/base block.
 * All public members referenced by other classes (type constants, item lookup helpers,
 * {@code addBoatTypeItems}, {@code setBoatTypeBaseItem}, {@code init}) are preserved.
 */
public class NSBoatTypes {

  /**
   * Replacement for the Yarn {@code BoatEntity.Type}.
   */
  public static final class BoatType {
    private final String name;
    public Block baseBlock;

    public BoatType(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public String getSerializedName() {
      return name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public record ItemPair(Item boat, Item chestBoat) {
  }

  private static final Map<BoatType, ItemPair> BOAT_TYPES_TO_ITEMS = new Object2ObjectLinkedOpenHashMap<>();

  public static void addBoatTypeItems(BoatType type, Item boatItem, Item chestBoatItem) {
    BOAT_TYPES_TO_ITEMS.put(type, new ItemPair(boatItem, chestBoatItem));
  }

  public static Optional<Item> getBoatItem(BoatType type) {
    ItemPair pair = BOAT_TYPES_TO_ITEMS.get(type);
    return pair == null ? Optional.empty() : Optional.ofNullable(pair.boat());
  }

  public static Optional<Item> getChestBoatItem(BoatType type) {
    ItemPair pair = BOAT_TYPES_TO_ITEMS.get(type);
    return pair == null ? Optional.empty() : Optional.ofNullable(pair.chestBoat());
  }

  public static @NotNull List<Item> getAllBoatItems() {
    List<Item> items = new ArrayList<>();
    BOAT_TYPES_TO_ITEMS.forEach((type, pair) -> {
      items.add(pair.boat());
      items.add(pair.chestBoat());
    });
    return items;
  }

  public static void setBoatTypeBaseItem(BoatType type, Block baseBlock) {
    type.baseBlock = baseBlock;
  }

  public static BoatType REDWOOD = new BoatType("redwood");
  public static BoatType SUGI = new BoatType("sugi");
  public static BoatType WISTERIA = new BoatType("wisteria");
  public static BoatType FIR = new BoatType("fir");
  public static BoatType WILLOW = new BoatType("willow");
  public static BoatType ASPEN = new BoatType("aspen");
  public static BoatType MAPLE = new BoatType("maple");
  public static BoatType CYPRESS = new BoatType("cypress");
  public static BoatType OLIVE = new BoatType("olive");
  public static BoatType JOSHUA = new BoatType("joshua");
  public static BoatType GHAF = new BoatType("ghaf");
  public static BoatType PALO_VERDE = new BoatType("palo_verde");
  public static BoatType COCONUT = new BoatType("coconut");
  public static BoatType CEDAR = new BoatType("cedar");
  public static BoatType LARCH = new BoatType("larch");
  public static BoatType MAHOGANY = new BoatType("mahogany");
  public static BoatType SAXAUL = new BoatType("saxaul");

  public static void init() {
  }
}
