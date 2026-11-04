package net.diaithir.claims.capitals;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Optional;
import java.util.UUID;
import net.diaithir.claims.Claim;
import net.diaithir.claims.DiaithirClaims;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Capitals: a monarchy layer over chunk claims. Clean-room, from the behaviour of "capital / monarchy" add-ons for villager mods
 * (found a capital, a ruler, an heir, subjects, succession when the ruler dies); no code taken from MCA Capitals (GPL-3.0).
 * Stage 1 does not need MCA Reborn; villager (MCA) subjects and nobles come in a later stage.
 * /capital found NAME | info [NAME] | list | swear NAME | leave | heir PLAYER | abdicate
 */
public final class Capitals {
    private Capitals() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> register(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(Capitals::onDeath);
    }

    private static int say(CommandSourceStack s, String t) {
        s.sendSystemMessage(Component.literal(t));
        return 1;
    }

    static int found(ServerPlayer p, String name) {
        CapitalData d = CapitalData.get(p.level().getServer());
        if (d.ruledBy(p.getUUID()) != null) return say(p.createCommandSourceStack(), "You already rule a capital.");
        if (d.byName(name) != null) return say(p.createCommandSourceStack(), "That name is taken.");
        LevelChunk ch = p.level().getChunkAt(p.blockPosition());
        Optional<Claim> cl = DiaithirClaims.at(ch);
        if (cl.isEmpty() || !cl.get().owner().equals(p.getUUID())) return say(p.createCommandSourceStack(), "Claim this chunk first (/claim); a capital stands on your own claim.");
        for (Capital c : d.all()) {
            if (c.dimension().equals(p.level().dimension().identifier().toString()) && c.cx() == ch.getPos().x() && c.cz() == ch.getPos().z()) {
                return say(p.createCommandSourceStack(), "A capital already stands here.");
            }
        }
        Capital sub = d.subjectOf(p.getUUID());
        if (sub != null) d.put(sub.withSubjects(sub.subjects().stream().filter(u -> !u.equals(p.getUUID())).toList()));
        d.put(new Capital(name, p.level().dimension().identifier().toString(), ch.getPos().x(), ch.getPos().z(), Optional.of(p.getUUID()),
            p.getGameProfile().name(), Optional.empty(), java.util.List.of(), p.level().getGameTime()));
        p.level().getServer().getPlayerList().broadcastSystemMessage(Component.literal(p.getGameProfile().name() + " has founded the capital of " + name + "."), false);
        return 1;
    }

    static String title(Capital c) {
        return c.rulerName().isEmpty() ? "no ruler" : "ruled by " + c.rulerName();
    }

    private static void register(CommandDispatcher<CommandSourceStack> dp) {
        dp.register(Commands.literal("capital")
            .then(Commands.literal("found").then(Commands.argument("name", StringArgumentType.word()).executes(c -> found(c.getSource().getPlayerOrException(), StringArgumentType.getString(c, "name")))))
            .then(Commands.literal("list").executes(c -> {
                for (Capital k : CapitalData.get(c.getSource().getServer()).all()) say(c.getSource(), k.name() + ", " + title(k) + ", " + k.subjects().size() + " subjects");
                return 1;
            }))
            .then(Commands.literal("info").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                CapitalData d = CapitalData.get(c.getSource().getServer());
                Capital k = d.ruledBy(p.getUUID());
                if (k == null) k = d.subjectOf(p.getUUID());
                return k == null ? say(c.getSource(), "You belong to no capital.") : say(c.getSource(), k.name() + ", " + title(k) + ", " + k.subjects().size() + " subjects, heir " + k.heir().map(h -> nameOf(c.getSource().getServer(), h)).orElse("none"));
            }))
            .then(Commands.literal("swear").then(Commands.argument("name", StringArgumentType.word()).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                CapitalData d = CapitalData.get(c.getSource().getServer());
                Capital k = d.byName(StringArgumentType.getString(c, "name"));
                if (k == null) return say(c.getSource(), "No such capital.");
                if (d.ruledBy(p.getUUID()) != null) return say(c.getSource(), "A ruler cannot swear to another.");
                Capital old = d.subjectOf(p.getUUID());
                if (old != null) d.put(old.withSubjects(old.subjects().stream().filter(u -> !u.equals(p.getUUID())).toList()));
                d.put(k.withSubjects(k.plus(p.getUUID())));
                return say(c.getSource(), "You are now a subject of " + k.name() + ".");
            })))
            .then(Commands.literal("leave").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                CapitalData d = CapitalData.get(c.getSource().getServer());
                Capital k = d.subjectOf(p.getUUID());
                if (k == null) return say(c.getSource(), "You are nobody's subject.");
                d.put(k.withSubjects(k.subjects().stream().filter(u -> !u.equals(p.getUUID())).toList()));
                return say(c.getSource(), "You left " + k.name() + ".");
            }))
            .then(Commands.literal("heir").then(Commands.argument("who", EntityArgument.player()).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                ServerPlayer h = EntityArgument.getPlayer(c, "who");
                CapitalData d = CapitalData.get(c.getSource().getServer());
                Capital k = d.ruledBy(p.getUUID());
                if (k == null) return say(c.getSource(), "You rule no capital.");
                if (h.getUUID().equals(p.getUUID())) return say(c.getSource(), "Name someone else.");
                d.put(k.withHeir(h.getUUID()));
                return say(c.getSource(), h.getGameProfile().name() + " is your heir.");
            })))
            .then(Commands.literal("abdicate").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                CapitalData d = CapitalData.get(c.getSource().getServer());
                Capital k = d.ruledBy(p.getUUID());
                if (k == null) return say(c.getSource(), "You rule no capital.");
                succeed(c.getSource().getServer(), d, k);
                return 1;
            })));
    }

    static String nameOf(MinecraftServer s, UUID id) {
        ServerPlayer p = s.getPlayerList().getPlayer(id);
        if (p != null) return p.getGameProfile().name();
        return s.services().nameToIdCache().get(id).map(x -> x.name()).orElse(id.toString().substring(0, 8));
    }

    /** The heir takes the throne; with no heir, the subject who swore first; with neither, the throne stays empty. */
    static void succeed(MinecraftServer s, CapitalData d, Capital k) {
        UUID next = k.heir().orElse(k.subjects().isEmpty() ? null : k.subjects().get(0));
        if (next == null) {
            d.put(k.withRuler(null, ""));
            s.getPlayerList().broadcastSystemMessage(Component.literal("The throne of " + k.name() + " stands empty."), false);
            return;
        }
        String n = nameOf(s, next);
        d.put(k.withRuler(next, n));
        s.getPlayerList().broadcastSystemMessage(Component.literal(n + " now rules " + k.name() + "."), false);
    }

    private static void onDeath(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        CapitalData d = CapitalData.get(p.level().getServer());
        Capital k = d.ruledBy(p.getUUID());
        if (k != null) succeed(p.level().getServer(), d, k);
    }
}
