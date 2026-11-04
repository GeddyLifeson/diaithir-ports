package net.diaithir.worldsys.moreprofessions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * More Professions fold (Pepsied, MIT): six extra jobs for fletcher villagers. A villager working at a fletching table
 * with an item frame above it holding a job token (redstone block, wooden axe, wooden pickaxe, honeycomb, wooden sword,
 * compass) while it has no trade xp takes that job and its trade list is replaced with the job's, two trades per level.
 * Trades are the datapack's own, converted by tools/convert_more_professions.py into moreprofessions/jobs.json.
 */
public final class MoreProfessionsFeature {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, "diaithirworldsys");
    /** Job id, or empty. */
    static final DeferredHolder<AttachmentType<?>, AttachmentType<String>> JOB = ATTACHMENTS.register("more_prof_job",
            () -> AttachmentType.builder(() -> "").serialize(Codec.STRING.fieldOf("v")).build());
    /** How many of the villager's offers have been replaced so far. */
    static final DeferredHolder<AttachmentType<?>, AttachmentType<Integer>> DONE = ATTACHMENTS.register("more_prof_done",
            () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("v")).build());

    private static Map<String, Job> JOBS;

    record Trade(ItemCost buy, ItemCost buyB, ItemStack sell, int maxUses, int xp, float mult) {
        MerchantOffer offer() {
            return new MerchantOffer(buy, Optional.ofNullable(buyB), sell.copy(), maxUses, xp, mult);
        }
    }

    /** levels.get(n) is the list of slots for level n+1; a slot is a list of variants, one is rolled. */
    record Job(String id, Identifier token, List<List<List<Trade>>> levels) {}

    private MoreProfessionsFeature() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> {
            if (e.getServer().getTickCount() % 80 != 0) return;
            for (ServerLevel level : e.getServer().getAllLevels()) {
                for (Villager v : level.getEntities(EntityType.VILLAGER, x -> true)) {
                    try {
                        update(level, v);
                    } catch (RuntimeException ex) {
                        System.err.println("[more_professions] " + ex);
                    }
                }
            }
        });
    }

    static synchronized Map<String, Job> jobs() {
        if (JOBS != null) return JOBS;
        Map<String, Job> out = new LinkedHashMap<>();
        try (Reader r = new InputStreamReader(MoreProfessionsFeature.class.getResourceAsStream(
                "/data/diaithirworldsys/moreprofessions/jobs.json"), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            for (var e : root.entrySet()) {
                JsonObject j = e.getValue().getAsJsonObject();
                List<List<List<Trade>>> levels = new ArrayList<>();
                for (JsonElement lv : j.getAsJsonArray("levels")) {
                    List<List<Trade>> slots = new ArrayList<>();
                    for (JsonElement slot : lv.getAsJsonArray()) {
                        List<Trade> variants = new ArrayList<>();
                        for (JsonElement t : slot.getAsJsonArray()) variants.add(trade(t.getAsJsonObject()));
                        slots.add(variants);
                    }
                    levels.add(slots);
                }
                out.put(e.getKey(), new Job(e.getKey(), Identifier.parse(j.get("token").getAsString()), levels));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("bad more professions data", ex);
        }
        return JOBS = out;
    }

    private static ItemCost cost(JsonObject o) {
        return new ItemCost(BuiltInRegistries.ITEM.getValue(Identifier.parse(o.get("id").getAsString())), o.get("count").getAsInt());
    }

    private static Trade trade(JsonObject o) {
        JsonObject sell = o.getAsJsonObject("sell");
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse(sell.get("id").getAsString())), sell.get("count").getAsInt());
        return new Trade(cost(o.getAsJsonObject("buy")), o.has("buy_b") ? cost(o.getAsJsonObject("buy_b")) : null, stack,
                o.get("max_uses").getAsInt(), o.get("xp").getAsInt(), o.get("price_multiplier").getAsFloat());
    }

    /** Pure: the offer for offer index i (0 based); two offers per level. */
    static MerchantOffer roll(Job job, int index, RandomSource rnd) {
        int level = Math.min(index / 2, job.levels().size() - 1);
        List<List<Trade>> slots = job.levels().get(level);
        List<Trade> variants = slots.get(rnd.nextInt(slots.size()));
        return variants.get(rnd.nextInt(variants.size())).offer();
    }

    private static Job jobAt(ServerLevel level, Villager v) {
        Optional<GlobalPos> site = v.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (site.isEmpty() || !site.get().dimension().equals(level.dimension())) return null;
        BlockPos above = site.get().pos().above();
        for (ItemFrame frame : level.getEntitiesOfClass(ItemFrame.class, new AABB(above))) {
            Identifier id = BuiltInRegistries.ITEM.getKey(frame.getItem().getItem());
            for (Job j : jobs().values()) if (j.token().equals(id)) return j;
        }
        return null;
    }

    static void update(ServerLevel level, Villager v) {
        String current = v.getData(JOB);
        boolean fletcher = v.getVillagerData().profession().is(VillagerProfession.FLETCHER);
        if (!current.isEmpty()) {
            Job j = fletcher ? jobAt(level, v) : null;
            if (j == null || !j.id().equals(current)) {
                if (v.getVillagerXp() == 0) { // job token gone before any trade: back to a plain villager
                    v.setData(JOB, "");
                    v.setData(DONE, 0);
                    // profession round trip makes the villager drop its offers and roll vanilla fletcher trades again
                    v.setVillagerData(v.getVillagerData().withProfession(level.registryAccess(), VillagerProfession.NONE));
                    v.setVillagerData(v.getVillagerData().withProfession(level.registryAccess(), VillagerProfession.FLETCHER));
                }
                else if (j == null && !fletcher) { v.setData(JOB, ""); v.setData(DONE, 0); }
                return;
            }
        } else {
            if (!fletcher || v.getVillagerXp() != 0) return;
            Job j = jobAt(level, v);
            if (j == null) return;
            current = j.id();
            v.setData(JOB, current);
            v.setData(DONE, 0);
        }
        Job job = jobs().get(current);
        if (job == null) return;
        MerchantOffers offers = v.getOffers();
        int done = v.getData(DONE);
        while (done < offers.size()) {
            offers.set(done, roll(job, done, level.getRandom()));
            done++;
        }
        v.setData(DONE, done);
    }
}
