package com.lion.graveyard.trades;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lion.graveyard.Graveyard;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.item.MapItem;
import java.util.List;
import java.util.Map;

/// Trade pool of the Nameless Hanged, read from `data/graveyard/nameless_hanged_trades/`.
///
/// Trades are plain vanilla MerchantOffers built from the JSON (no VillagerTradingPlus dependency).
public final class NamelessHangedTrades {

    /// The folder is his, but a datapack can drop anything into it, so files still have to name him.
    private static final String PROFESSION = "graveyard:nameless_hanged";

    private static final List<JsonObject> PENDING_TRADES = new ArrayList<>();

    private static List<TradeFactory> factories;

    private NamelessHangedTrades() {}

    /// Rebuilt per reload, so `/reload` does not duplicate trades.
    public static void clear() {
        PENDING_TRADES.clear();
        factories = null;
    }

    /// Item components are not bound during the reload listener. Parse trades on first use.
    public static List<TradeFactory> factories() {
        if (factories == null) {
            List<TradeFactory> parsed = new ArrayList<>();

            for (JsonObject trade : PENDING_TRADES) {
                // The parser logs the reason before it returns null.
                TradeFactory factory = parse(trade);

                if (factory != null) {
                    parsed.add(factory);
                }
            }

            factories = parsed;
        }

        return factories;
    }

    public static void deserializeJson(JsonObject jsonRoot) {
        JsonElement profession = jsonRoot.get("profession");

        if (profession == null || !profession.isJsonPrimitive() || !PROFESSION.equals(profession.getAsString())) {
            return;
        }

        JsonElement trades = jsonRoot.get("trades");

        if (trades == null || !trades.isJsonObject()) {
            Graveyard.getLogger().error("Skipping nameless hanged trade file: no 'trades' object.");
            return;
        }

        for (Map.Entry<String, JsonElement> group : trades.getAsJsonObject().entrySet()) {
            if (!group.getValue().isJsonArray()) {
                Graveyard.getLogger().error("Skipping nameless hanged trade group '{}': not an array.", group.getKey());
                continue;
            }

            for (JsonElement element : group.getValue().getAsJsonArray()) {
                if (!element.isJsonObject()) {
                    Graveyard.getLogger().error("Dropped an entry from nameless hanged trade group '{}': not an object.", group.getKey());
                    continue;
                }

                PENDING_TRADES.add(element.getAsJsonObject());
            }
        }
    }

    /// Builds one offer. Returns null when the trade does not apply right now.
    public interface TradeFactory {
        MerchantOffer create(ServerLevel level, Entity trader, RandomSource random);
    }

    private static TradeFactory parse(JsonObject entry) {
        try {
            return build(entry);
        } catch (RuntimeException e) {
            Graveyard.getLogger().warn("Skipping nameless hanged trade entry {}: {}", entry, e.toString());
            return null;
        }
    }

    private static TradeFactory build(JsonObject e) {
        String type = e.get("type").getAsString();
        int maxUses = e.has("max_uses") ? e.get("max_uses").getAsInt() : 12;
        int xp = e.has("villager_experience") ? e.get("villager_experience").getAsInt() : 1;
        int demand = e.has("demand") ? e.get("demand").getAsInt() : 0;
        List<JsonObject> conditions = new ArrayList<>();
        if (e.has("conditions")) {
            e.getAsJsonArray("conditions").forEach(c -> conditions.add(c.getAsJsonObject()));
        }

        switch (type.substring(type.indexOf(':') + 1)) {
            case "sell_item" -> {
                Item sell = item(e.getAsJsonObject("sell"));
                int sellCount = e.getAsJsonObject("sell").get("count").getAsInt();
                ItemCost cost = cost(e.getAsJsonObject("priceIn"));
                return (level, trader, random) -> !met(conditions, level) ? null
                        : new MerchantOffer(cost, Optional.empty(), new ItemStack(sell, sellCount), 0, maxUses, xp, 0.05F, demand);
            }
            case "sell_enchanted_tool" -> {
                Item sell = item(e.getAsJsonObject("sell"));
                JsonObject base = e.getAsJsonObject("basePriceIn");
                Item baseItem = item(base);
                int baseCount = base.get("count").getAsInt();
                return (level, trader, random) -> {
                    if (!met(conditions, level)) {
                        return null;
                    }
                    int enchLevel = 5 + random.nextInt(15);
                    ItemStack stack = EnchantmentHelper.enchantItem(random, new ItemStack(sell), enchLevel, level.registryAccess(), Optional.empty());
                    int count = Math.min(baseCount + enchLevel, 64);
                    return new MerchantOffer(new ItemCost(baseItem, count), stack, maxUses, xp, 0.2F);
                };
            }
            case "sell_map" -> {
                TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, Identifier.parse(e.get("structure_id").getAsString()));
                String name = e.get("name").getAsString();
                ItemCost cost = cost(e.getAsJsonObject("priceIn"));
                ItemCost buy = e.has("buy") ? cost(e.getAsJsonObject("buy")) : null;
                return (level, trader, random) -> {
                    if (!met(conditions, level)) {
                        return null;
                    }
                    BlockPos pos = level.findNearestMapStructure(tag, trader.blockPosition(), 100, true);
                    if (pos == null) {
                        return null;
                    }
                    ItemStack map = MapItem.create(level, pos.getX(), pos.getZ(), (byte) 2, true, true);
                    MapItem.renderBiomePreviewMap(level, map);
                    MapItemSavedData.addTargetDecoration(map, pos, "+", MapDecorationTypes.RED_X);
                    map.set(DataComponents.ITEM_NAME, Component.translatable(name));
                    return new MerchantOffer(cost, Optional.ofNullable(buy), map, 0, maxUses, xp, 0.2F, 0);
                };
            }
            case "weighted_pool" -> {
                List<TradeFactory> subs = new ArrayList<>();
                List<Integer> weights = new ArrayList<>();
                int total = 0;
                for (var el : e.getAsJsonArray("pool")) {
                    JsonObject o = el.getAsJsonObject();
                    TradeFactory f = parse(o.getAsJsonObject("trade"));
                    if (f != null) {
                        int w = Math.max(1, o.has("weight") ? o.get("weight").getAsInt() : 1);
                        subs.add(f);
                        weights.add(w);
                        total += w;
                    }
                }
                if (subs.isEmpty()) {
                    throw new IllegalArgumentException("empty pool");
                }
                final int sum = total;
                return (level, trader, random) -> {
                    int roll = random.nextInt(sum);
                    for (int i = 0; i < subs.size(); i++) {
                        roll -= weights.get(i);
                        if (roll < 0) {
                            return subs.get(i).create(level, trader, random);
                        }
                    }
                    return null;
                };
            }
            default -> throw new IllegalArgumentException("unknown trade type " + type);
        }
    }

    private static boolean met(List<JsonObject> conditions, ServerLevel level) {
        long dayTime = level.getDefaultClockTime() % 24000L;
        boolean night = dayTime >= 13000L && dayTime < 23000L;
        for (JsonObject c : conditions) {
            switch (c.get("type").getAsString()) {
                case "day_night" -> {
                    if (night != "night".equals(c.get("time").getAsString())) {
                        return false;
                    }
                }
                case "moon_phase" -> {
                    if (c.has("require_night") && c.get("require_night").getAsBoolean() && !night) {
                        return false;
                    }
                    int phase = (int) (level.getDefaultClockTime() / 24000L % 8L + 8L) % 8;
                    boolean ok = false;
                    for (JsonElement p : c.getAsJsonArray("phases")) {
                        ok |= p.getAsInt() == phase;
                    }
                    if (!ok) {
                        return false;
                    }
                }
                default -> {
                }
            }
        }
        return true;
    }

    private static Item item(JsonObject o) {
        String id = o.get("item").getAsString();
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id))
                .orElseThrow(() -> new IllegalArgumentException("unknown item " + id));
    }

    private static ItemCost cost(JsonObject o) {
        return new ItemCost(item(o), o.get("count").getAsInt());
    }
}
