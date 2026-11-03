package net.diaithir.bosses;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * /bossesgatetest: plays the whole Shardfall Gate rite with a fake fighter on a headless server: refusals, keystones (attested and
 * offered), the hour step, the defend step with real server ticks and waves, then the opening, once only. Test world state is cleared at the end.
 */
final class GateTest {
    private static boolean listening, running, opened;
    private static int fails, checks;
    private static long started;
    private static FakePlayer fp;
    private static CommandSourceStack src;
    private static BlockPos gate;
    private static Integer oldForce;
    private static int oldSeconds;
    private static boolean sawWave;

    private static void check(String what, boolean ok) {
        checks++;
        if (!ok) { fails++; System.out.println("[bossesselftest] gate FAIL: " + what); }
    }

    private static String lastMsg() { return Gate.locked(src.getServer()) == null ? "unlocked" : "locked"; }

    static int start(CommandSourceStack s) {
        if (running) return 0;
        src = s;
        fails = checks = 0;
        opened = sawWave = false;
        ServerLevel level = s.getServer().overworld();
        for (int cx = -3; cx <= 2; cx++) for (int cz = -3; cz <= 2; cz++) { level.getChunk(cx, cz); level.setChunkForced(cx, cz, true); }
        BlockPos c0 = new BlockPos(0, 230, 0);
        for (int x = -30; x <= 30; x++) for (int z = -30; z <= 30; z++) {
            level.setBlock(c0.offset(x, 0, z), Blocks.STONE.defaultBlockState(), 2);
            for (int h = 1; h <= 10; h++) level.setBlock(c0.offset(x, h, z), Blocks.AIR.defaultBlockState(), 2);
        }
        gate = c0.above();
        level.setBlock(gate, DiaithirBosses.SHARDFALL_GATE.get().defaultBlockState(), 2);
        fp = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("00000000-0000-4000-8000-00000000b057"), "[gate-test]"));
        fp.setPos(gate.getX() + 0.5, gate.getY(), gate.getZ() + 2.5);
        fp.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        oldForce = Ages.forced;
        oldSeconds = Gate.CFG.defend.seconds;
        Gate.CFG.defend.seconds = 12;
        Gate.TEST_PLAYERS.add(fp);
        Gate.quiet = true;
        wipe(s);
        AscendantData d = AscendantData.get(s.getServer());
        var srv = s.getServer();

        // 1. refusals
        Ages.forced = 5;
        check("closed at the start", !Gate.isOpen(srv));
        check("age refuses", Gate.locked(srv) != null);
        Ages.forced = Ages.RODOS;
        check("Last Wheel and rush required", Gate.locked(srv) != null);
        d.defeat("last_wheel", 0);
        check("rush still required", Gate.locked(srv) != null);
        d.defeat(Rush.CFG.id, 0);
        check("unlocked after both", Gate.locked(srv) == null);
        check("16 keystones listed", Gate.CFG.keystones.size() == 16);
        check("rush.cleared is read from boss data", Gate.missing(srv).size() == 15 && !Gate.missing(srv).contains("rush.cleared"));

        // 2. keystones: using the Gate without them changes nothing and the hour step does not open
        Gate.use(level, gate, fp, ItemStack.EMPTY);
        check("no hour step before keystones", !d.beaten(Gate.HOUR_DONE) && Gate.DEFEND_START.isEmpty());
        check("unknown resource is not attested", !Gate.attest(srv, "nonsense"));
        check("attest is new once", Gate.attest(srv, "resonator.shard") && !Gate.attest(srv, "resonator.shard"));
        // an offered item (test mapping stands in for a tag item) is taken and attests its resource
        Gate.TEST_ITEMS.put(Items.DIAMOND, "focus.shard");
        ItemStack dia = new ItemStack(Items.DIAMOND, 2);
        Gate.use(level, gate, fp, dia);
        check("offered item consumed", dia.getCount() == 1);
        check("offered item attested", !Gate.missing(srv).contains("focus.shard"));
        ItemStack stick = new ItemStack(Items.STICK);
        Gate.use(level, gate, fp, stick);
        check("wrong item kept", stick.getCount() == 1);
        for (Gate.Keystone k : Gate.CFG.keystones) Gate.attest(srv, k.resource);
        check("all keystones in", Gate.missing(srv).isEmpty());

        // 3. hour step
        Gate.testClock = 6000L;
        Gate.use(level, gate, fp, ItemStack.EMPTY);
        check("wrong hour refused", !d.beaten(Gate.HOUR_DONE));
        Gate.testClock = 18000L;
        Gate.use(level, gate, fp, ItemStack.EMPTY);
        check("midnight accepted", d.beaten(Gate.HOUR_DONE));

        // 4. defend step runs on real ticks
        Gate.use(level, gate, fp, ItemStack.EMPTY);
        check("defend started", Gate.DEFEND_START.size() == 1);
        started = level.getGameTime();
        running = true;
        if (!listening) {
            listening = true;
            NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> tick(e.getServer().overworld()));
            NeoForge.EVENT_BUS.addListener(Gate.GateOpenedEvent.class, e -> opened = true);
        }
        s.sendSuccess(() -> Component.literal("gate test started: defend step " + Gate.CFG.defend.seconds + " s"), false);
        return 1;
    }

    private static void wipe(CommandSourceStack s) {
        AscendantData d = AscendantData.get(s.getServer());
        d.clearDefeats();
        Gate.DEFEND_START.clear();
    }

    private static void tick(ServerLevel level) {
        if (!running) return;
        if (!Gate.DEFEND_START.isEmpty() && !level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(gate).inflate(48), m -> m.entityTags().contains(Ritual.WAVE_TAG)).isEmpty()) sawWave = true;
        if (Gate.isOpen(level.getServer())) { finish(level); return; }
        if (level.getGameTime() - started > 20L * 60) { check("opened before the timeout", false); finish(level); }
    }

    private static void finish(ServerLevel level) {
        running = false;
        var srv = level.getServer();
        long took = (level.getGameTime() - started) / 20;
        check("Gate open", Gate.isOpen(srv));
        check("opening event posted", opened);
        check("a wave appeared during the defence", sawWave);
        check("defend step took about its time (" + took + " s)", took >= Gate.CFG.defend.seconds - 1 && took <= Gate.CFG.defend.seconds + 5);
        check("waves cleared at the end", level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(gate).inflate(48), m -> m.entityTags().contains(Ritual.WAVE_TAG)).isEmpty());
        Gate.use(level, gate, fp, ItemStack.EMPTY);
        check("open Gate does not restart", Gate.DEFEND_START.isEmpty());
        Gate.open(level, gate);
        check("a second opening is a no-op", Gate.isOpen(srv) && Gate.DEFEND_START.isEmpty());
        Gate.TEST_PLAYERS.clear();
        Gate.TEST_ITEMS.clear();
        Gate.testClock = null;
        Gate.quiet = false;
        Gate.CFG.defend.seconds = oldSeconds;
        Ages.forced = oldForce;
        for (int cx = -3; cx <= 2; cx++) for (int cz = -3; cz <= 2; cz++) level.setChunkForced(cx, cz, false);
        wipe(src);
        System.out.println("[bossesselftest] gate " + (fails == 0 ? "OK" : "FAIL") + ": checks=" + checks + " bad=" + fails + " " + lastMsg());
    }

    private GateTest() {}
}
