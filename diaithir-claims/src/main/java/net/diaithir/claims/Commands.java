package net.diaithir.claims;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

/** /claim, /claim drop, /claim trust|untrust, /claim map, /claim info. */
public final class Commands {
    private Commands() {}

    private static LevelChunk here(ServerPlayer p) {
        return p.level().getChunkAt(p.blockPosition());
    }

    private static int say(CommandSourceStack s, String t) {
        s.sendSystemMessage(Component.literal(t));
        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        var claim = net.minecraft.commands.Commands.literal("claim")
            .executes(c -> claim(c.getSource().getPlayerOrException()))
            .then(net.minecraft.commands.Commands.literal("drop").executes(c -> drop(c.getSource().getPlayerOrException())))
            .then(net.minecraft.commands.Commands.literal("info").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                Optional<Claim> cl = DiaithirClaims.at(here(p));
                return say(c.getSource(), cl.map(x -> "Claimed by " + x.ownerName() + ", " + x.friends().size() + " trusted.").orElse("Unclaimed."));
            }))
            .then(net.minecraft.commands.Commands.literal("trust").then(net.minecraft.commands.Commands.argument("who", EntityArgument.player())
                .executes(c -> trust(c.getSource().getPlayerOrException(), EntityArgument.getPlayer(c, "who"), true))))
            .then(net.minecraft.commands.Commands.literal("untrust").then(net.minecraft.commands.Commands.argument("who", EntityArgument.player())
                .executes(c -> trust(c.getSource().getPlayerOrException(), EntityArgument.getPlayer(c, "who"), false))))
            .then(net.minecraft.commands.Commands.literal("selftest").requires(src -> src.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                .executes(c -> {
                    ServerPlayer who = c.getSource().getPlayer();
                    if (who == null) {
                        net.minecraft.server.level.ServerLevel lv = c.getSource().getServer().overworld();
                        who = net.neoforged.neoforge.common.util.FakePlayerFactory.get(lv, new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes("selftest-owner".getBytes()), "SelfTestOwner"));
                        who.setPos(8, 70, 8);
                        lv.getChunk(0, 0);
                    }
                    return SelfTest.run(who);
                }))
            .then(net.minecraft.commands.Commands.literal("map").executes(c -> map(c.getSource().getPlayerOrException()))
                .then(net.minecraft.commands.Commands.literal("chat").executes(c -> chatMap(c.getSource().getPlayerOrException()))))
            .then(net.minecraft.commands.Commands.literal("load").executes(c -> load(c.getSource().getPlayerOrException(), true)))
            .then(net.minecraft.commands.Commands.literal("unload").executes(c -> load(c.getSource().getPlayerOrException(), false)))
            .then(net.minecraft.commands.Commands.literal("dropall").executes(c -> dropAll(c.getSource().getPlayerOrException())))
            .then(net.minecraft.commands.Commands.literal("area").then(net.minecraft.commands.Commands.argument("radius", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 4))
                .executes(c -> area(c.getSource().getPlayerOrException(), com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "radius")))))
            .then(net.minecraft.commands.Commands.literal("flag")
                .then(net.minecraft.commands.Commands.literal("public_use").then(net.minecraft.commands.Commands.argument("on", com.mojang.brigadier.arguments.BoolArgumentType.bool())
                    .executes(c -> flag(c.getSource().getPlayerOrException(), Claim.PUBLIC_USE, com.mojang.brigadier.arguments.BoolArgumentType.getBool(c, "on")))))
                .then(net.minecraft.commands.Commands.literal("explosions").then(net.minecraft.commands.Commands.argument("on", com.mojang.brigadier.arguments.BoolArgumentType.bool())
                    .executes(c -> flag(c.getSource().getPlayerOrException(), Claim.ALLOW_EXPLOSIONS, com.mojang.brigadier.arguments.BoolArgumentType.getBool(c, "on"))))))
            .then(net.minecraft.commands.Commands.literal("team")
                .then(net.minecraft.commands.Commands.literal("create").then(net.minecraft.commands.Commands.argument("name", StringArgumentType.word())
                    .executes(c -> teamMsg(c.getSource(), Teams.create(c.getSource().getPlayerOrException().getUUID(), StringArgumentType.getString(c, "name")), "Team created."))))
                .then(net.minecraft.commands.Commands.literal("invite").then(net.minecraft.commands.Commands.argument("who", EntityArgument.player())
                    .executes(c -> {
                        ServerPlayer o = EntityArgument.getPlayer(c, "who");
                        int r = teamMsg(c.getSource(), Teams.invite(c.getSource().getPlayerOrException().getUUID(), o.getUUID()), "Invited " + o.getGameProfile().name() + ".");
                        if (r == 1) o.sendSystemMessage(Component.literal("You were invited to a claim team. Type /claim team join."));
                        return r;
                    })))
                .then(net.minecraft.commands.Commands.literal("join").executes(c -> teamMsg(c.getSource(), Teams.join(c.getSource().getPlayerOrException().getUUID()), "Joined.")))
                .then(net.minecraft.commands.Commands.literal("leave").executes(c -> teamMsg(c.getSource(), Teams.leave(c.getSource().getPlayerOrException().getUUID()), "Left.")))
                .then(net.minecraft.commands.Commands.literal("info").executes(c -> {
                    String t = Teams.teamOf(c.getSource().getPlayerOrException().getUUID());
                    return say(c.getSource(), t == null ? "No team." : "Team " + t + ": " + Teams.members(t).size() + " members.");
                })));
        d.register(claim);
        d.register(net.minecraft.commands.Commands.literal("ftbchunks").redirect(d.getRoot().getChild("claim")));
    }

    static int teamMsg(CommandSourceStack s, String err, String ok) {
        say(s, err == null ? ok : err);
        return err == null ? 1 : 0;
    }

    static int load(ServerPlayer p, boolean on) {
        LevelChunk ch = here(p);
        Optional<Claim> c = DiaithirClaims.at(ch);
        if (c.isEmpty() || !c.get().owner().equals(p.getUUID())) {
            p.sendSystemMessage(Component.literal("Stand in a chunk you hold."));
            return 0;
        }
        if (c.get().forced() == on) {
            p.sendSystemMessage(Component.literal(on ? "Already kept loaded." : "Not kept loaded."));
            return 0;
        }
        int n = p.getData(DiaithirClaims.LOADED);
        if (on && n >= DiaithirClaims.MAX_LOADED && !p.isCreative()) {
            p.sendSystemMessage(Component.literal("You keep " + n + " chunks loaded, the most allowed."));
            return 0;
        }
        ((net.minecraft.server.level.ServerLevel) p.level()).setChunkForced(ch.getPos().x(), ch.getPos().z(), on);
        ch.setData(DiaithirClaims.CLAIM, Optional.of(c.get().withForced(on)));
        p.setData(DiaithirClaims.LOADED, Math.max(0, n + (on ? 1 : -1)));
        p.sendSystemMessage(Component.literal(on ? "This chunk now stays loaded." : "This chunk unloads normally again."));
        return 1;
    }

    static int flag(ServerPlayer p, int flag, boolean on) {
        LevelChunk ch = here(p);
        Optional<Claim> c = DiaithirClaims.at(ch);
        if (c.isEmpty() || !c.get().owner().equals(p.getUUID())) {
            p.sendSystemMessage(Component.literal("Stand in a chunk you hold."));
            return 0;
        }
        int f = on ? c.get().flags() | flag : c.get().flags() & ~flag;
        ch.setData(DiaithirClaims.CLAIM, Optional.of(c.get().withFlags(f)));
        p.sendSystemMessage(Component.literal("Flag set."));
        return 1;
    }

    /** Claims the square of (2r+1) chunks around you that are free, up to your limit. */
    static int area(ServerPlayer p, int r) {
        ChunkPos me = p.chunkPosition();
        int got = 0;
        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                LevelChunk ch = p.level().getChunkSource().getChunkNow(me.x() + dx, me.z() + dz);
                int n = p.getData(DiaithirClaims.COUNT);
                if (ch == null || DiaithirClaims.at(ch).isPresent() || (n >= DiaithirClaims.MAX_CHUNKS && !p.isCreative())) {
                    continue;
                }
                ch.setData(DiaithirClaims.CLAIM, Optional.of(new Claim(p.getUUID(), p.getGameProfile().name(), List.of())));
                p.setData(DiaithirClaims.COUNT, n + 1);
                got++;
            }
        }
        p.sendSystemMessage(Component.literal("Claimed " + got + " chunks (" + p.getData(DiaithirClaims.COUNT) + "/" + DiaithirClaims.MAX_CHUNKS + ")."));
        return got > 0 ? 1 : 0;
    }

    /** Drops every loaded chunk you hold in the dimension you stand in. */
    static int dropAll(ServerPlayer p) {
        net.minecraft.server.level.ServerLevel lvl = (net.minecraft.server.level.ServerLevel) p.level();
        int dropped = 0;
        ChunkPos me = p.chunkPosition();
        int r = lvl.getServer().getPlayerList().getViewDistance();
        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                LevelChunk ch = lvl.getChunkSource().getChunkNow(me.x() + dx, me.z() + dz);
                Optional<Claim> c = ch == null ? Optional.empty() : DiaithirClaims.at(ch);
                if (c.isPresent() && c.get().owner().equals(p.getUUID())) {
                    if (c.get().forced()) {
                        lvl.setChunkForced(ch.getPos().x(), ch.getPos().z(), false);
                        p.setData(DiaithirClaims.LOADED, Math.max(0, p.getData(DiaithirClaims.LOADED) - 1));
                    }
                    ch.setData(DiaithirClaims.CLAIM, Optional.empty());
                    dropped++;
                }
            }
        }
        p.setData(DiaithirClaims.COUNT, Math.max(0, p.getData(DiaithirClaims.COUNT) - dropped));
        p.sendSystemMessage(Component.literal("Dropped " + dropped + " chunks near you."));
        return dropped > 0 ? 1 : 0;
    }

    static int claim(ServerPlayer p) {
        LevelChunk ch = here(p);
        if (DiaithirClaims.at(ch).isPresent()) {
            p.sendSystemMessage(Component.literal("Already claimed."));
            return 0;
        }
        int n = p.getData(DiaithirClaims.COUNT);
        if (n >= DiaithirClaims.MAX_CHUNKS && !p.isCreative()) {
            p.sendSystemMessage(Component.literal("You hold " + n + " chunks, the most allowed."));
            return 0;
        }
        ch.setData(DiaithirClaims.CLAIM, Optional.of(new Claim(p.getUUID(), p.getGameProfile().name(), List.of())));
        p.setData(DiaithirClaims.COUNT, n + 1);
        p.sendSystemMessage(Component.literal("Claimed chunk " + ch.getPos().x() + ", " + ch.getPos().z() + " (" + (n + 1) + "/" + DiaithirClaims.MAX_CHUNKS + ")."));
        return 1;
    }

    static int drop(ServerPlayer p) {
        LevelChunk ch = here(p);
        Optional<Claim> c = DiaithirClaims.at(ch);
        if (c.isEmpty() || !(c.get().owner().equals(p.getUUID()) || p.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))) {
            p.sendSystemMessage(Component.literal("Not yours."));
            return 0;
        }
        if (c.get().forced()) {
            ((net.minecraft.server.level.ServerLevel) p.level()).setChunkForced(ch.getPos().x(), ch.getPos().z(), false);
        }
        ch.setData(DiaithirClaims.CLAIM, Optional.empty());
        if (c.get().owner().equals(p.getUUID())) {
            if (c.get().forced()) {
                p.setData(DiaithirClaims.LOADED, Math.max(0, p.getData(DiaithirClaims.LOADED) - 1));
            }
            p.setData(DiaithirClaims.COUNT, Math.max(0, p.getData(DiaithirClaims.COUNT) - 1));
        }
        p.sendSystemMessage(Component.literal("Dropped."));
        return 1;
    }

    static int trust(ServerPlayer p, ServerPlayer other, boolean add) {
        LevelChunk ch = here(p);
        Optional<Claim> c = DiaithirClaims.at(ch);
        if (c.isEmpty() || !c.get().owner().equals(p.getUUID())) {
            p.sendSystemMessage(Component.literal("Stand in a chunk you hold."));
            return 0;
        }
        List<java.util.UUID> f = new ArrayList<>(c.get().friends());
        f.remove(other.getUUID());
        if (add) {
            f.add(other.getUUID());
        }
        ch.setData(DiaithirClaims.CLAIM, Optional.of(c.get().withFriends(List.copyOf(f))));
        p.sendSystemMessage(Component.literal((add ? "Trusted " : "No longer trusted: ") + other.getGameProfile().name()));
        return 1;
    }

    /** /claim map opens the map screen (21 by 21 chunks); /claim map chat prints 11 by 11 chunks around you. # yours, X someone else's, . free, @ you. */
    static int map(ServerPlayer p) {
        if (!p.connection.hasChannel(MapPayload.TYPE)) {
            return chatMap(p);
        }
        ChunkPos me = p.chunkPosition();
        int n = MapPayload.SIZE, r = n / 2;
        byte[] cells = new byte[n * n];
        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                LevelChunk ch = p.level().getChunkSource().getChunkNow(me.x() + dx, me.z() + dz);
                Optional<Claim> c = ch == null ? Optional.empty() : DiaithirClaims.at(ch);
                cells[(dz + r) * n + dx + r] = (byte) (c.isEmpty() ? 0 : c.get().allows(p.getUUID()) ? 1 : 2);
            }
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p, new MapPayload(me.x(), me.z(), cells));
        return 1;
    }

    static int chatMap(ServerPlayer p) {
        ChunkPos me = p.chunkPosition();
        for (int dz = -5; dz <= 5; dz++) {
            StringBuilder sb = new StringBuilder();
            for (int dx = -5; dx <= 5; dx++) {
                if (dx == 0 && dz == 0) {
                    sb.append('@');
                    continue;
                }
                LevelChunk ch = p.level().getChunkSource().getChunkNow(me.x() + dx, me.z() + dz);
                Optional<Claim> c = ch == null ? Optional.empty() : DiaithirClaims.at(ch);
                sb.append(c.isEmpty() ? '.' : c.get().allows(p.getUUID()) ? '#' : 'X');
            }
            p.sendSystemMessage(Component.literal(sb.toString()));
        }
        return 1;
    }
}
