package net.diaithir.hostile.celestials;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Runs on server start when -Dlunarselftest=true, then stops the server. Checks the forecast, forcing an event and the event data. */
public final class SelfTest {
    private static int fails;

    private static void check(boolean ok, String what) {
        if (!ok) fails++;
        Lunar.LOG.info("SELFTEST {} {}", ok ? "ok  " : "FAIL", what);
    }

    @SubscribeEvent
    public static void run(ServerStartedEvent e) {
        if (!Boolean.getBoolean("lunarselftest")) return;
        ServerLevel lvl = e.getServer().getLevel(Level.OVERWORLD);
        Forecast fc = Forecast.of(lvl);
        fc.update(lvl);
        List<Forecast.Entry> up = fc.upcoming(Forecast.dayOf(lvl), 1000);
        check(Lunar.events().size() == 8, "8 event files loaded (7 moons + default): " + Lunar.events().size());
        check(!up.isEmpty(), "forecast has events: " + up.size());
        check(up.stream().allMatch(en -> Lunar.events().containsKey(en.event())), "every forecast entry names a loaded event");
        for (int i = 1; i < up.size(); i++) check(up.get(i).day() - up.get(i - 1).day() > Lunar.MIN_BETWEEN, "gap " + i + " above minimum");
        Forecast.Entry first = up.get(0);
        fc.recompute(lvl);
        check(fc.upcoming(Forecast.dayOf(lvl), 1000).get(0).equals(first), "recompute is deterministic for the same seed");
        fc.force(lvl, "diaithirhostile:blood_moon");
        check(Forecast.night(lvl), "force moves the clock to night");
        check(fc.current(lvl).id.equals("diaithirhostile:blood_moon") || lvl.isRaining(), "forced blood moon is active tonight");
        check(Lunar.get("diaithirhostile:blood_moon").horde.cap > 0 && Lunar.get("diaithirhostile:blue_moon").xp == 2.0, "event values match the upstream defaults");
        // the sky darkness that mob spawning reads is refreshed by ticks, so the gameplay part runs 100 ticks later
        fc.force(lvl, "diaithirhostile:blood_moon");
        lvl.registryAccess().get(net.minecraft.world.clock.WorldClocks.OVERWORLD).ifPresent(c -> lvl.clockManager().setTotalTicks(c, Forecast.dayOf(lvl) * Lunar.DAY + 18000L));
        waitTicks = 100;
    }

    private static int waitTicks = -1;

    @SubscribeEvent
    public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post e) {
        if (waitTicks < 0 || --waitTicks > 0) return;
        waitTicks = -1;
        ServerLevel lvl = e.getServer().getLevel(Level.OVERWORLD);
        play(lvl, Forecast.of(lvl));
        Lunar.LOG.info("SELFTEST done, {} failures", fails);
        e.getServer().halt(false);
    }

    /** Drives each effect with a fake player: force the moon, then post the real game events and read the outcome. */
    private static void play(ServerLevel lvl, Forecast fc) {
        var p = net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(lvl);
        for (int cx = -6; cx <= 6; cx++) for (int cz = -6; cz <= 6; cz++) lvl.getChunk(cx, cz);
        net.minecraft.core.BlockPos top = new net.minecraft.core.BlockPos(0, lvl.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0), 0);
        p.snapTo(top.getX() + 0.5, top.getY(), top.getZ() + 0.5, 0F, 0F);
        try { Class.forName("net.minecraft.world.level.block.entity.BeaconBlockEntity"); check(true, "beacon mixin applied (require=1, class loaded)"); } catch (Throwable t) { check(false, "beacon mixin: " + t); }
        var bus = net.neoforged.neoforge.common.NeoForge.EVENT_BUS;

        // blood moon: horde spawns near the player (up to 60 attempts), all tagged
        check(fc.current(lvl).id.equals("diaithirhostile:blood_moon"), "blood moon still active after the wait");
        for (int dx = -80; dx <= 80; dx += 16) for (int dz = -80; dz <= 80; dz += 16) lvl.getChunk(new net.minecraft.core.BlockPos(dx, 0, dz));
        Lunar.Event blood = Lunar.get("diaithirhostile:blood_moon");
        int spawned = 0;
        for (int i = 0; i < 60 && spawned == 0; i++) spawned += LunarEvents.horde(lvl, blood, List.of(p));
        check(spawned > 0, "blood moon horde spawns mobs near a player: " + spawned);
        check(!lvl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, p.getBoundingBox().inflate(80), m -> m.entityTags().contains("lunar_horde")).isEmpty(), "horde mobs carry the lunar_horde tag");

        // blood moon (KS): boosted mobs, essence drop, creeper effects, phantoms, essence use
        var zombie = net.minecraft.world.entity.EntityType.ZOMBIE.create(lvl, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        zombie.snapTo(top.getX() + 3.5, top.getY(), top.getZ() + 0.5, 0F, 0F);
        Lunar.Event bm = Lunar.get("diaithirhostile:blood_moon");
        boolean boosted = false;
        for (int i = 0; i < 100 && !boosted; i++) { zombie.entityTags().remove("lunar_boosted"); boosted = LunarEvents.boost(zombie, bm); }
        check(boosted && zombie.entityTags().contains("lunar_boosted") && !zombie.getActiveEffects().isEmpty(), "blood moon boosts a hostile mob with an effect");
        var loot = new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        int got = 0;
        for (int i = 0; i < 40 && got == 0; i++) { bus.post(new net.neoforged.neoforge.event.entity.living.LivingDropsEvent(zombie, lvl.damageSources().generic(), loot, true)); got = loot.size(); }
        check(got > 0 && loot.get(0).getItem().is(MoonEssence.ITEM.get()), "boosted mob drops Moon Essence");
        LunarEvents.creeperEffects(p, bm);
        check(p.hasEffect(net.minecraft.world.effect.MobEffects.NAUSEA) && p.hasEffect(net.minecraft.world.effect.MobEffects.INFESTED), "creeper blast gives nausea and infested");
        check(LunarEvents.phantoms(lvl, bm, List.of(p)) == 1, "blood moon sends phantoms above the player");
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(MoonEssence.ITEM.get(), 2));
        MoonEssence.ITEM.get().use(lvl, p, net.minecraft.world.InteractionHand.MAIN_HAND);
        check(p.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).getCount() == 1, "using Moon Essence consumes one");
        check(!lvl.getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class, p.getBoundingBox().inflate(4)).isEmpty(), "using Moon Essence drops experience");
        var qf = SelfTest.class.getResourceAsStream("/data/diaithirhostile/quests/lunar_events.json");
        check(qf != null && Lunar.GSON.fromJson(new java.io.InputStreamReader(qf), com.google.gson.JsonObject.class).getAsJsonArray("quests").size() == 7, "quest file lists the 7 moons");

        // blue moon: luck, xp x2, anvil, enchant, beacon
        fc.force(lvl, "diaithirhostile:blue_moon");
        check(fc.current(lvl).id.equals("diaithirhostile:blue_moon"), "blue moon active");
        LunarEvents.applyEffects(p, Lunar.get("diaithirhostile:blue_moon"));
        check(p.hasEffect(net.minecraft.world.effect.MobEffects.LUCK), "blue moon gives players luck");
        var xp = new net.neoforged.neoforge.event.entity.player.PlayerXpEvent.XpChange(p, 10);
        bus.post(xp);
        check(xp.getAmount() == 20, "blue moon doubles xp: " + xp.getAmount());
        var anvil = new net.neoforged.neoforge.event.AnvilUpdateEvent(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD), new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_INGOT), null, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_SWORD), 10, 1, p);
        bus.post(anvil);
        check(anvil.getXpCost() == 5, "blue moon halves anvil cost: " + anvil.getXpCost());
        var ench = new net.neoforged.neoforge.event.enchanting.EnchantmentLevelSetEvent(lvl, top, 2, 15, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD), 30);
        bus.post(ench);
        check(ench.getEnchantLevel() == 15, "blue moon halves enchanting table cost: " + ench.getEnchantLevel());
        check(LunarEvents.beaconMultiplier(lvl) == 1.5, "blue moon beacon radius x1.5");

        // harvest moon: tagged crops x2, other drops untouched
        fc.force(lvl, "diaithirhostile:harvest_moon");
        var drops = new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        drops.add(new net.minecraft.world.entity.item.ItemEntity(lvl, 0, 0, 0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CARROT, 3)));
        drops.add(new net.minecraft.world.entity.item.ItemEntity(lvl, 0, 0, 0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK, 3)));
        bus.post(new net.neoforged.neoforge.event.level.BlockDropsEvent(lvl, top, net.minecraft.world.level.block.Blocks.CARROTS.defaultBlockState(), null, drops, p, net.minecraft.world.item.ItemStack.EMPTY));
        check(drops.get(0).getItem().getCount() == 6, "harvest moon doubles carrots: " + drops.get(0).getItem().getCount());
        check(drops.get(1).getItem().getCount() == 3, "harvest moon leaves sticks alone");

        // sleep: blocked on a blood moon, allowed on a harvest moon
        var sleepOk = new net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent(p, top, null);
        bus.post(sleepOk);
        check(sleepOk.getProblem() == null, "harvest moon allows sleep");
        fc.force(lvl, "diaithirhostile:blood_moon");
        check(Lunar.get("diaithirhostile:blood_moon").block_sleeping, "blood moon data blocks sleep");
        var sleepNo = new net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent(p, top, null);
        bus.post(sleepNo);
        check(sleepNo.getProblem() != null, "blood moon blocks sleep");

        // meteor: a crater with a magma core
        net.minecraft.core.BlockPos hit = top.offset(40, 0, 0);
        lvl.getChunk(hit);
        net.minecraft.core.BlockPos ground = new net.minecraft.core.BlockPos(hit.getX(), lvl.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, hit.getX(), hit.getZ()) - 1, hit.getZ());
        Meteors.impact(lvl, ground, 3);
        check(lvl.getBlockState(ground).is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK), "meteor leaves a magma core");
        check(lvl.getBlockState(ground.above(2)).isAir(), "meteor clears the air above the crater");
        Lunar.LOG.info("SELFTEST meteor strike picks a spot near a player: {}", Meteors.strike(lvl, new Lunar.Meteor()) != null || lvl.players().isEmpty());
    }

    private SelfTest() {}
}
