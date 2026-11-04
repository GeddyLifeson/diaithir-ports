package net.diaithir.worldsys.caravans;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Better Caravans fold (Nikrecs, MIT): roaming merchant caravans. A wandering trader leads two leashed pack animals
 * (camels in hot biomes, llamas in cold ones, mules elsewhere), carries a weighted pick of trades from the datapack's
 * caravan_trades/default.json, walks on in a steady heading and leaves after about twenty minutes. Spawns near
 * players at random; {@code /caravan spawn|despawn|info} for game masters. Mini-raids, caravan stops, discounts and
 * loot chests from upstream are not ported.
 */
public final class CaravansFeature {
    static final String TAG = "diaithir_caravan";
    private static final int CHECK_TICKS = 1200;      // one roll a minute per player
    private static final double CHANCE = 0.04;        // about one caravan per 25 player-minutes
    private static final int LIFETIME = 24000;        // 20 minutes
    private static List<Trade> TRADES;

    record Trade(Identifier buy, int buyCount, Identifier sell, int sellCount, int maxUses, int xp, float mult, int weight) {}

    private CaravansFeature() {}

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> commands(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> {
            int t = e.getServer().getTickCount();
            if (t % 100 == 0) for (ServerLevel level : e.getServer().getAllLevels()) steer(level);
            if (t % CHECK_TICKS != 0) return;
            for (ServerLevel level : e.getServer().getAllLevels()) {
                if (level.dimension() != Level.OVERWORLD) continue;
                for (ServerPlayer p : level.players()) {
                    if (level.getRandom().nextDouble() < CHANCE && nearby(level, p.blockPosition(), 160).isEmpty()) spawn(level, p.blockPosition());
                }
            }
        });
    }

    static synchronized List<Trade> trades() {
        if (TRADES != null) return TRADES;
        List<Trade> out = new ArrayList<>();
        try (Reader r = new InputStreamReader(CaravansFeature.class.getResourceAsStream("/data/diaithirworldsys/caravan_trades/default.json"), StandardCharsets.UTF_8)) {
            for (JsonElement el : JsonParser.parseReader(r).getAsJsonObject().getAsJsonArray("trades")) {
                JsonObject o = el.getAsJsonObject();
                out.add(new Trade(Identifier.parse(o.get("buy_item").getAsString()), o.get("buy_count").getAsInt(),
                        Identifier.parse(o.get("sell_item").getAsString()), o.get("sell_count").getAsInt(),
                        o.get("max_uses").getAsInt(), o.get("villager_xp").getAsInt(), o.get("price_multiplier").getAsFloat(),
                        o.has("weight") ? o.get("weight").getAsInt() : 1));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("bad caravan trades", ex);
        }
        return TRADES = out;
    }

    /** Weighted pick of n distinct trades. Pure apart from the random source. */
    static List<Trade> pick(List<Trade> pool, int n, RandomSource rnd) {
        List<Trade> left = new ArrayList<>(pool), out = new ArrayList<>();
        while (out.size() < n && !left.isEmpty()) {
            int total = left.stream().mapToInt(Trade::weight).sum();
            int roll = rnd.nextInt(Math.max(1, total));
            for (Trade t : left) {
                roll -= t.weight();
                if (roll < 0) { out.add(t); left.remove(t); break; }
            }
        }
        return out;
    }

    static List<WanderingTrader> nearby(ServerLevel level, BlockPos pos, int r) {
        return level.getEntitiesOfClass(WanderingTrader.class, new AABB(pos).inflate(r), t -> t.entityTags().contains(TAG));
    }

    static WanderingTrader spawn(ServerLevel level, BlockPos near) {
        RandomSource rnd = level.getRandom();
        double ang = rnd.nextDouble() * Math.PI * 2;
        int x = near.getX() + (int) (Math.cos(ang) * 70), z = near.getZ() + (int) (Math.sin(ang) * 70);
        BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        if (!level.isLoaded(pos) || !level.getFluidState(pos.below()).isEmpty()) return null;
        WanderingTrader trader = EntityType.WANDERING_TRADER.create(level, EntitySpawnReason.EVENT);
        if (trader == null) return null;
        trader.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        trader.addTag(TAG);
        trader.setDespawnDelay(LIFETIME);
        MerchantOffers offers = new MerchantOffers();
        for (Trade t : pick(trades(), 4 + rnd.nextInt(3), rnd)) {
            offers.add(new MerchantOffer(new ItemCost(BuiltInRegistries.ITEM.getValue(t.buy()), t.buyCount()), Optional.empty(),
                    new ItemStack(BuiltInRegistries.ITEM.getValue(t.sell()), t.sellCount()), 0, t.maxUses(), t.xp(), t.mult()));
        }
        MerchantOffers live = trader.getOffers(); // overrideOffers is a no-op on villagers, so fill the live list
        live.clear();
        live.addAll(offers);
        level.addFreshEntity(trader);
        var biome = level.getBiome(pos);
        EntityType<? extends Mob> animal = biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.IS_SAVANNA) || biome.value().getBaseTemperature() > 1.5F
                ? EntityType.CAMEL : biome.value().getBaseTemperature() < 0.2F ? EntityType.LLAMA : EntityType.MULE;
        Mob lead = trader;
        for (int i = 0; i < 2; i++) {
            Mob m = animal.create(level, EntitySpawnReason.EVENT);
            if (m == null) break;
            m.snapTo(pos.getX() + 0.5 + (i + 1), pos.getY(), pos.getZ() + 0.5, 0, 0);
            m.addTag(TAG);
            m.setPersistenceRequired();
            level.addFreshEntity(m);
            m.setLeashedTo(lead, true);
            lead = m;
        }
        return trader;
    }

    /** Every 5 s: walk each caravan trader on towards the far side of its starting heading. */
    private static void steer(ServerLevel level) {
        for (WanderingTrader t : level.getEntities(EntityType.WANDERING_TRADER, x -> x.entityTags().contains(TAG))) {
            if (t.isTrading() || !t.getNavigation().isDone()) continue;
            double ang = (t.getId() * 2.399963) % (Math.PI * 2);
            BlockPos target = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    t.blockPosition().offset((int) (Math.cos(ang) * 24), 0, (int) (Math.sin(ang) * 24)));
            t.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.6);
        }
    }

    private static void commands(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("caravan").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("spawn").executes(c -> {
                    ServerLevel level = c.getSource().getLevel();
                    WanderingTrader t = spawn(level, BlockPos.containing(c.getSource().getPosition()));
                    if (t == null) { c.getSource().sendFailure(Component.literal("Failed to spawn caravan at this location.")); return 0; }
                    c.getSource().sendSuccess(() -> Component.literal("Caravan spawned at " + t.blockPosition().toShortString()), true);
                    return 1;
                }))
                .then(Commands.literal("despawn").executes(c -> {
                    ServerLevel level = c.getSource().getLevel();
                    int n = 0;
                    for (var e : level.getEntitiesOfClass(Mob.class, new AABB(BlockPos.containing(c.getSource().getPosition())).inflate(200), m -> m.entityTags().contains(TAG))) {
                        e.discard(); n++;
                    }
                    int removed = n;
                    c.getSource().sendSuccess(() -> Component.literal("Removed " + removed + " caravan members"), true);
                    return n;
                }))
                .then(Commands.literal("info").executes(c -> {
                    var list = nearby(c.getSource().getLevel(), BlockPos.containing(c.getSource().getPosition()), 200);
                    c.getSource().sendSuccess(() -> Component.literal(list.isEmpty() ? "No nearby caravan found." : list.stream()
                            .map(t -> "Caravan at " + t.blockPosition().toShortString() + ", trades " + t.getOffers().size()).reduce((a, b) -> a + "\n" + b).get()), false);
                    return list.size();
                })));
    }
}
