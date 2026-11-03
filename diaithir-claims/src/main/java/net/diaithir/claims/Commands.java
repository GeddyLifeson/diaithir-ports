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
        d.register(net.minecraft.commands.Commands.literal("claim")
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
                .executes(c -> SelfTest.run(c.getSource().getPlayerOrException())))
            .then(net.minecraft.commands.Commands.literal("map").executes(c -> map(c.getSource().getPlayerOrException()))
                .then(net.minecraft.commands.Commands.literal("chat").executes(c -> chatMap(c.getSource().getPlayerOrException())))));
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
        ch.setData(DiaithirClaims.CLAIM, Optional.empty());
        if (c.get().owner().equals(p.getUUID())) {
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
        ch.setData(DiaithirClaims.CLAIM, Optional.of(new Claim(c.get().owner(), c.get().ownerName(), List.copyOf(f))));
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
