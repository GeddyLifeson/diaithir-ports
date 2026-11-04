package net.diaithir.fauna;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Bringing an extinct species back: a studied, cleaned fossil in hand, a revival matrix in the other hand, and an expensive item (default a nether star)
 * from the inventory. Gated by the world's Age so nothing is revivable before the endgame: DNA tier 1 from Age VII, tiers 2-3 from Age VIII (Future),
 * tiers 4-5 from Age IX (Rodos). The Age is the larger of the server config and the score of the fake player #age in the objective diaithirfauna_age,
 * which the Age scripts (KubeJS or datapacks) set with /scoreboard players set #age diaithirfauna_age 7. Default 0: nothing revivable.
 */
public final class Revival {
    public static final String OBJECTIVE = "diaithirfauna_age";
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.IntValue AGE;
    private static final ModConfigSpec.ConfigValue<String> COST_ITEM;
    private static final ModConfigSpec.IntValue COST_COUNT;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        AGE = b.comment("Age (7, 8 or 9) treated as reached regardless of the " + OBJECTIVE + " score; 0 leaves it to the score.").defineInRange("age", 0, 0, 20);
        COST_ITEM = b.comment("Item consumed from the inventory by each revival.").define("costItem", "minecraft:nether_star");
        COST_COUNT = b.defineInRange("costCount", 1, 0, 64);
        SPEC = b.build();
    }

    private Revival() {}

    /** Age VII = 7 and so on; 0 until a script sets it. */
    public static int age(ServerLevel level) {
        int a = AGE.get();
        Objective o = level.getServer().getScoreboard().getObjective(OBJECTIVE);
        if (o != null) {
            ReadOnlyScoreInfo info = level.getServer().getScoreboard().getPlayerScoreInfo(ScoreHolder.forNameOnly("#age"), o);
            if (info != null) a = Math.max(a, info.value());
        }
        return a;
    }

    /** Age needed for a DNA tier: 1 is Age VII, 2-3 Age VIII, 4-5 Age IX. */
    public static int ageFor(int tier) { return tier <= 1 ? 7 : tier <= 3 ? 8 : 9; }

    private static Item costItem() {
        return BuiltInRegistries.ITEM.getValue(Identifier.parse(COST_ITEM.get()));
    }

    private static int has(ServerPlayer p, Item item) {
        int n = 0;
        for (ItemStack st : p.getInventory().getNonEquipmentItems()) if (st.is(item)) n += st.getCount();
        return n;
    }

    private static void take(ServerPlayer p, Item item, int n) {
        for (ItemStack st : p.getInventory().getNonEquipmentItems()) {
            if (n <= 0) return;
            if (!st.is(item)) continue;
            int k = Math.min(n, st.getCount());
            st.shrink(k);
            n -= k;
        }
    }

    private static boolean fail(ServerPlayer p, String key, Object... args) {
        p.sendSystemMessage(Component.translatable("message.diaithirfauna.revive." + key, args));
        return false;
    }

    /** Tries the revival with the cleaned fossil in the main hand; spawns at the position (a chamber's top, or the player). True when an animal was made. */
    public static boolean revive(ServerPlayer p, ServerLevel level, BlockPos at) {
        ItemStack fossil = p.getMainHandItem();
        Species s = Fossils.speciesOf(fossil);
        if (!fossil.is(Fossils.CLEANED.get()) || s == null || !s.fossil) return fail(p, "no_fossil");
        Component name = Component.translatable("species.diaithirfauna." + s.id);
        if (!p.getData(Fossils.STUDIED.get()).contains(s.id)) return fail(p, "not_studied", name);
        int need = ageFor(s.dna);
        if (age(level) < need) return fail(p, "too_early", name, need);
        if (!p.getOffhandItem().is(Fossils.MATRIX.get())) return fail(p, "no_matrix");
        Item cost = costItem();
        int count = COST_COUNT.get();
        if (has(p, cost) < count) return fail(p, "no_cost", Component.translatable(cost.getDescriptionId()), count);
        Plan plan = Plan.ALL.get(s.plan);
        if (plan.water() && !level.getFluidState(at).is(net.minecraft.tags.FluidTags.WATER)) return fail(p, "no_water", name);
        FaunaMob m = FaunaMod.TYPES.get(s.plan).get().create(level, EntitySpawnReason.COMMAND);
        m.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        m.setSpecies(s);
        m.addTag("Revived");
        m.setPersistenceRequired();
        level.addFreshEntity(m);
        fossil.shrink(1);
        take(p, cost, count);
        p.sendSystemMessage(Component.translatable("message.diaithirfauna.revive.ok", name));
        return true;
    }
}
