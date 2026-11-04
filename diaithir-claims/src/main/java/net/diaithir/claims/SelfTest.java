package net.diaithir.claims;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.common.util.BlockSnapshot;

/** /claim selftest: fires break, place, use and explosion-list events at the caller's chunk from a stranger and from the owner. */
final class SelfTest {
    private SelfTest() {}

    static int run(ServerPlayer owner) {
        ServerLevel level = (ServerLevel) owner.level();
        LevelChunk ch = level.getChunkAt(owner.blockPosition());
        BlockPos pos = new BlockPos(ch.getPos().getMinBlockX() + 2, owner.blockPosition().getY() - 1, ch.getPos().getMinBlockZ() + 2);
        BlockState before = level.getBlockState(pos);
        Optional<Claim> old = DiaithirClaims.at(ch);
        ch.setData(DiaithirClaims.CLAIM, Optional.of(new Claim(owner.getUUID(), owner.getGameProfile().name(), List.of())));
        FakePlayer stranger = FakePlayerFactory.get(level, new GameProfile(UUID.nameUUIDFromBytes("stranger".getBytes()), "Stranger"));
        List<String> out = new ArrayList<>();
        try {
            out.add(check("stranger break denied", brk(level, pos, stranger), true));
            out.add(check("owner break allowed", brk(level, pos, owner), false));
            out.add(check("stranger place denied", place(level, pos, stranger), true));
            out.add(check("owner place allowed", place(level, pos, owner), false));
            out.add(check("stranger use denied", use(level, pos, stranger), true));
            out.add(check("owner use allowed", use(level, pos, owner), false));
            List<BlockPos> boom = new ArrayList<>(List.of(pos, pos.above()));
            Guard.strip(level, boom);
            out.add(check("explosion list emptied", boom.isEmpty(), true));
            ch.setData(DiaithirClaims.CLAIM, Optional.of(new Claim(owner.getUUID(), owner.getGameProfile().name(), List.of(stranger.getUUID()))));
            out.add(check("trusted break allowed", brk(level, pos, stranger), false));
            ch.setData(DiaithirClaims.CLAIM, Optional.of(new Claim(owner.getUUID(), owner.getGameProfile().name(), List.of()).withFlags(Claim.PUBLIC_USE)));
            out.add(check("public_use flag lets a stranger use", use(level, pos, stranger), false));
            out.add(check("public_use flag still denies break", brk(level, pos, stranger), true));
            ch.setData(DiaithirClaims.CLAIM, Optional.of(new Claim(owner.getUUID(), owner.getGameProfile().name(), List.of()).withFlags(Claim.ALLOW_EXPLOSIONS)));
            List<BlockPos> boom2 = new ArrayList<>(List.of(pos));
            Guard.strip(level, boom2);
            out.add(check("explosions flag keeps blocks in the list", boom2.size() == 1, true));
            ch.setData(DiaithirClaims.CLAIM, Optional.of(new Claim(owner.getUUID(), owner.getGameProfile().name(), List.of())));
            out.add(check("team: stranger denied before invite", brk(level, pos, stranger), true));
            Teams.create(owner.getUUID(), "selftest_team");
            Teams.invite(owner.getUUID(), stranger.getUUID());
            Teams.join(stranger.getUUID());
            out.add(check("team: member may break", brk(level, pos, stranger), false));
            Teams.leave(stranger.getUUID());
            Teams.leave(owner.getUUID());
            out.add(check("team: left member denied again", brk(level, pos, stranger), true));
        } finally {
            ch.setData(DiaithirClaims.CLAIM, old);
            level.setBlock(pos, before, 3);
        }
        for (String l : out) {
            owner.sendSystemMessage(Component.literal(l));
            level.getServer().sendSystemMessage(Component.literal("[claims selftest] " + l));
        }
        return out.stream().allMatch(l -> l.startsWith("PASS")) ? 1 : 0;
    }

    private static String check(String what, boolean got, boolean want) {
        return (got == want ? "PASS " : "FAIL ") + what;
    }

    private static boolean brk(ServerLevel level, BlockPos pos, net.minecraft.world.entity.player.Player who) {
        BreakBlockEvent e = new BreakBlockEvent(level, pos, level.getBlockState(pos), who);
        NeoForge.EVENT_BUS.post(e);
        return e.isCanceled();
    }

    private static boolean place(ServerLevel level, BlockPos pos, net.minecraft.world.entity.player.Player who) {
        BlockEvent.EntityPlaceEvent e = new BlockEvent.EntityPlaceEvent(BlockSnapshot.create(level.dimension(), level, pos), Blocks.STONE.defaultBlockState(), who);
        NeoForge.EVENT_BUS.post(e);
        return e.isCanceled();
    }

    private static boolean use(ServerLevel level, BlockPos pos, net.minecraft.world.entity.player.Player who) {
        PlayerInteractEvent.RightClickBlock e = new PlayerInteractEvent.RightClickBlock(who, InteractionHand.MAIN_HAND, pos,
            new BlockHitResult(Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false));
        NeoForge.EVENT_BUS.post(e);
        return e.isCanceled();
    }
}
