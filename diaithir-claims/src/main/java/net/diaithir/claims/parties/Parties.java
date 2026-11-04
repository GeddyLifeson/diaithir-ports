package net.diaithir.claims.parties;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Parties: groups whose members trust each other on every member's claims, so one /party invite replaces trusting each chunk.
 * Clean-room, from the behaviour of party-and-claims mods (Open Parties and Claims is LGPL-3.0; no code taken).
 * /party create NAME | invite P | accept NAME | leave | kick P | disband | info | list
 */
public final class Parties {
    private static volatile MinecraftServer server;

    private Parties() {}

    public static void init() {
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent e) -> server = e.getServer());
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> server = null);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent e) -> register(e.getDispatcher()));
    }

    /** True when both are in the same party. Used by claims to let party members in. */
    public static boolean sameParty(UUID a, UUID b) {
        MinecraftServer s = server;
        if (s == null || a.equals(b)) return a.equals(b);
        PartyData.Party p = PartyData.get(s).of(a);
        return p != null && p.members().contains(b);
    }

    private static int say(CommandSourceStack s, String t) {
        s.sendSystemMessage(Component.literal(t));
        return 1;
    }

    private static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("party")
            .then(Commands.literal("create").then(Commands.argument("name", StringArgumentType.word()).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                PartyData data = PartyData.get(c.getSource().getServer());
                String n = StringArgumentType.getString(c, "name");
                if (data.of(p.getUUID()) != null) return say(c.getSource(), "Leave your party first.");
                if (data.byName(n) != null) return say(c.getSource(), "That name is taken.");
                data.create(n, p.getUUID());
                return say(c.getSource(), "Party " + n + " created.");
            })))
            .then(Commands.literal("invite").then(Commands.argument("who", EntityArgument.player()).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                ServerPlayer t = EntityArgument.getPlayer(c, "who");
                PartyData data = PartyData.get(c.getSource().getServer());
                PartyData.Party party = data.of(p.getUUID());
                if (party == null || !party.owner().equals(p.getUUID())) return say(c.getSource(), "Only a party owner can invite.");
                if (!party.invited().contains(t.getUUID())) party.invited().add(t.getUUID());
                data.setDirty();
                t.sendSystemMessage(Component.literal(p.getGameProfile().name() + " invites you to " + party.name() + ". /party accept " + party.name()));
                return say(c.getSource(), "Invited " + t.getGameProfile().name() + ".");
            })))
            .then(Commands.literal("accept").then(Commands.argument("name", StringArgumentType.word()).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                PartyData data = PartyData.get(c.getSource().getServer());
                PartyData.Party party = data.byName(StringArgumentType.getString(c, "name"));
                if (party == null || !party.invited().contains(p.getUUID())) return say(c.getSource(), "No such invite.");
                if (data.of(p.getUUID()) != null) return say(c.getSource(), "Leave your party first.");
                party.invited().remove(p.getUUID());
                party.members().add(p.getUUID());
                data.setDirty();
                return say(c.getSource(), "You joined " + party.name() + ".");
            })))
            .then(Commands.literal("leave").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                PartyData data = PartyData.get(c.getSource().getServer());
                PartyData.Party party = data.of(p.getUUID());
                if (party == null) return say(c.getSource(), "You are in no party.");
                if (party.owner().equals(p.getUUID())) return say(c.getSource(), "Owners disband (/party disband) instead of leaving.");
                party.members().remove(p.getUUID());
                data.setDirty();
                return say(c.getSource(), "You left " + party.name() + ".");
            }))
            .then(Commands.literal("kick").then(Commands.argument("who", EntityArgument.player()).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                ServerPlayer t = EntityArgument.getPlayer(c, "who");
                PartyData data = PartyData.get(c.getSource().getServer());
                PartyData.Party party = data.of(p.getUUID());
                if (party == null || !party.owner().equals(p.getUUID())) return say(c.getSource(), "Only a party owner can kick.");
                if (t.getUUID().equals(p.getUUID()) || !party.members().remove(t.getUUID())) return say(c.getSource(), "Not a member.");
                data.setDirty();
                return say(c.getSource(), "Removed " + t.getGameProfile().name() + ".");
            })))
            .then(Commands.literal("disband").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                PartyData data = PartyData.get(c.getSource().getServer());
                PartyData.Party party = data.of(p.getUUID());
                if (party == null || !party.owner().equals(p.getUUID())) return say(c.getSource(), "Only a party owner can disband.");
                data.remove(party);
                return say(c.getSource(), "Party disbanded.");
            }))
            .then(Commands.literal("info").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                PartyData.Party party = PartyData.get(c.getSource().getServer()).of(p.getUUID());
                return party == null ? say(c.getSource(), "You are in no party.") : say(c.getSource(), party.name() + ", " + party.members().size() + " members.");
            }))
            .then(Commands.literal("list").executes(c -> {
                for (PartyData.Party q : PartyData.get(c.getSource().getServer()).all()) say(c.getSource(), q.name() + " (" + q.members().size() + ")");
                return 1;
            })));
    }
}
