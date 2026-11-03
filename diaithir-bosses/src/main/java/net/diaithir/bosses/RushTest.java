package net.diaithir.bosses;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * /bossesrushtest: runs the whole boss rush with a fake fighter on a headless server. The real server ticks drive Rush; this class only
 * kills each boss a moment after it appears, then checks the order, the rewards, the best time and the cleared flag.
 */
final class RushTest {
    private static boolean listening, running;
    private static int waited, killed, lastIndex = -1, bossAge;
    private static long started;
    private static FakePlayer fp;
    private static CommandSourceStack src;
    private static Integer oldForce;
    private static int oldBetween;

    static int start(CommandSourceStack s) {
        if (running) return 0;
        src = s;
        ServerLevel level = s.getServer().overworld();
        for (int cx = -3; cx <= 2; cx++) for (int cz = -3; cz <= 2; cz++) { level.getChunk(cx, cz); level.setChunkForced(cx, cz, true); }
        BlockPos c0 = new BlockPos(0, 230, 0);
        for (int x = -40; x <= 40; x++) for (int z = -40; z <= 40; z++) {
            level.setBlock(c0.offset(x, 0, z), Blocks.STONE.defaultBlockState(), 2);
            for (int h = 1; h <= 14; h++) level.setBlock(c0.offset(x, h, z), Blocks.AIR.defaultBlockState(), 2);
        }
        fp = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("00000000-0000-4000-8000-00000000b056"), "[rush-test]"));
        fp.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100000);
        fp.setHealth(100000);
        fp.setPos(c0.getX() + 0.5, c0.getY() + 1, c0.getZ() + 0.5);
        oldForce = Ages.forced;
        Ages.forced = Ages.RODOS;
        AscendantData data = AscendantData.get(s.getServer());
        data.clearDefeats();
        for (String id : Rush.CFG.requires_kills) data.defeat(id, 0);
        oldBetween = Rush.CFG.arena.between_ticks;
        Rush.CFG.arena.between_ticks = 5;
        Rush.TEST_PLAYERS.add(fp);
        killed = 0;
        waited = 0;
        lastIndex = -1;
        started = level.getGameTime();
        Component why = Rush.refusal(s.getServer());
        if (why != null) { s.sendFailure(why); finish(false, "refused: " + why.getString()); return 0; }
        Rush.start(level, Vec3.atBottomCenterOf(c0.above()), List.of(fp));
        running = true;
        if (!listening) {
            listening = true;
            NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, e -> tick(e.getServer().overworld()));
        }
        s.sendSuccess(() -> Component.literal("rush test started: " + Rush.CFG.order.size() + " bosses"), false);
        return 1;
    }

    private static void tick(ServerLevel level) {
        if (!running) return;
        if (!Rush.active()) { done(level); return; }
        if (level.getGameTime() - started > 20L * 60 * 10) { finish(false, "timed out at boss " + Rush.index()); return; }
        var b = Rush.currentBoss();
        if (b instanceof Mob m && m.isAlive()) {
            if (Rush.index() != lastIndex) { lastIndex = Rush.index(); waited = 0; }
            if (++waited >= 10) {
                m.kill(level);
                killed++;
            }
        }
    }

    private static void done(ServerLevel level) {
        AscendantData data = AscendantData.get(level.getServer());
        int n = Rush.CFG.order.size();
        boolean ok = killed == n && data.beaten(Rush.CFG.id) && data.best() >= 0
            && fp.getInventory().countItem(Items.NETHER_STAR) >= 1 && fp.getInventory().countItem(DiaithirBosses.RUSH_LAUREL.get()) >= 1;
        finish(ok, "killed=" + killed + "/" + n + " cleared=" + data.beaten(Rush.CFG.id) + " best_ticks=" + data.best()
            + " nether_star=" + fp.getInventory().countItem(Items.NETHER_STAR) + " laurel=" + fp.getInventory().countItem(DiaithirBosses.RUSH_LAUREL.get()));
    }

    private static void finish(boolean ok, String msg) {
        running = false;
        Rush.stop("rush test over");
        Rush.TEST_PLAYERS.clear();
        Rush.CFG.arena.between_ticks = oldBetween;
        Ages.forced = oldForce;
        ServerLevel lv = src.getServer().overworld();
        for (int cx = -3; cx <= 2; cx++) for (int cz = -3; cz <= 2; cz++) lv.setChunkForced(cx, cz, false);
        AscendantData.get(src.getServer()).clearDefeats();
        System.out.println("[bossesselftest] rush " + (ok ? "OK" : "FAIL") + ": " + msg);
    }

    private RushTest() {}
}
