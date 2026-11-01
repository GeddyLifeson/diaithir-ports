package net.diaithir.conduits;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * /diaithirconduits: act on the node you are looking at (channel, join, leave, mode, kinds), manage who may use your channels (trust, untrust, list),
 * and an owner-only selftest.
 */
final class ConduitCommands {
    private ConduitCommands() {}

    private static LiteralArgumentBuilder<CommandSourceStack> lit(String s) {
        return Commands.literal(s);
    }

    static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(lit("diaithirconduits")
                .then(lit("channel").then(Commands.argument("number", IntegerArgumentType.integer(1)).executes(c -> onNode(c, (sl, be, p) -> be.join(sl, p.getStringUUID(), IntegerArgumentType.getInteger(c, "number"), p)))))
                .then(lit("join").then(Commands.argument("owner", StringArgumentType.word()).then(Commands.argument("number", IntegerArgumentType.integer(1)).executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    String owner = resolve(c.getSource(), StringArgumentType.getString(c, "owner"));
                    if (owner == null) return fail(c, "Nobody called that is known here.");
                    return onNode(c, (sl, be, pl) -> be.join(sl, owner, IntegerArgumentType.getInteger(c, "number"), pl));
                }))))
                .then(lit("leave").executes(c -> onNode(c, (sl, be, p) -> be.leave(sl, p))))
                .then(lit("mode").then(Commands.argument("face", StringArgumentType.word()).then(Commands.argument("mode", StringArgumentType.word()).executes(c -> {
                    Direction face = Direction.byName(StringArgumentType.getString(c, "face"));
                    String m = StringArgumentType.getString(c, "mode");
                    int mode = m.equals("off") ? 0 : m.equals("send") ? 1 : m.equals("receive") ? 2 : -1;
                    if (face == null || mode < 0) return fail(c, "Use: mode <north|east|south|west|up|down> <off|send|receive>");
                    return onNode(c, (sl, be, p) -> {
                        if (!be.canManage(p)) return "This node belongs to " + be.ownerName + ".";
                        be.setMode(face, mode);
                        return null;
                    });
                }))))
                .then(lit("kinds").then(Commands.argument("kind", StringArgumentType.word()).then(Commands.argument("on", BoolArgumentType.bool()).executes(c -> {
                    String k = StringArgumentType.getString(c, "kind");
                    int kind = List.of(ConduitBlockEntity.KIND_NAMES).indexOf(k);
                    if (kind < 0) return fail(c, "Use: kinds <items|fluids|energy> <true|false>");
                    boolean on = BoolArgumentType.getBool(c, "on");
                    return onNode(c, (sl, be, p) -> {
                        if (!be.canManage(p)) return "This node belongs to " + be.ownerName + ".";
                        be.setKind(kind, on);
                        return null;
                    });
                }))))
                .then(lit("info").executes(c -> onNode(c, (sl, be, p) -> null)))
                .then(lit("trust").then(Commands.argument("player", StringArgumentType.word()).executes(c -> trust(c, true))))
                .then(lit("untrust").then(Commands.argument("player", StringArgumentType.word()).executes(c -> trust(c, false))))
                .then(lit("list").executes(ConduitCommands::list))
                .then(lit("selftest").requires(Commands.hasPermission(Commands.LEVEL_OWNERS)).executes(c -> {
                    List<String> fails = ConduitSelfTest.run(c.getSource().getServer());
                    String line = fails.isEmpty() ? "Conduits self test PASS" : "Conduits self test FAIL: " + String.join("; ", fails);
                    c.getSource().sendSuccess(() -> Component.literal(line), false);
                    return fails.isEmpty() ? 1 : 0;
                })));
    }

    private static int fail(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendFailure(Component.literal(msg));
        return 0;
    }

    private interface NodeAction {
        /** Returns an error, or null when it worked. */
        String run(ServerLevel level, ConduitBlockEntity be, ServerPlayer player);
    }

    /** Runs an action on the node the player is looking at (within 6 blocks), then reports the node. */
    private static int onNode(CommandContext<CommandSourceStack> c, NodeAction action) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer p = c.getSource().getPlayerOrException();
        HitResult hit = p.pick(6.0, 0.0F, false);
        if (!(hit instanceof BlockHitResult bhr) || !(p.level().getBlockEntity(bhr.getBlockPos()) instanceof ConduitBlockEntity be)) return fail(c, "Look at a conduit node first.");
        ServerLevel sl = p.level();
        Channels ch = Channels.get(sl.getServer());
        ch.rememberName(p.getStringUUID(), p.getGameProfile().name());
        String err = action.run(sl, be, p);
        if (err != null) return fail(c, err);
        c.getSource().sendSuccess(() -> Component.literal(be.describe(ch)), false);
        return 1;
    }

    private static String resolve(CommandSourceStack src, String name) {
        ServerPlayer online = src.getServer().getPlayerList().getPlayerByName(name);
        if (online != null) return online.getStringUUID();
        return Channels.get(src.getServer()).uuidOfName(name);
    }

    private static int trust(CommandContext<CommandSourceStack> c, boolean add) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer me = c.getSource().getPlayerOrException();
        String who = resolve(c.getSource(), StringArgumentType.getString(c, "player"));
        if (who == null) return fail(c, "Nobody called that is known here.");
        Channels ch = Channels.get(c.getSource().getServer());
        ch.rememberName(me.getStringUUID(), me.getGameProfile().name());
        boolean changed = add ? ch.trust(me.getStringUUID(), who) : ch.untrust(me.getStringUUID(), who);
        c.getSource().sendSuccess(() -> Component.literal((changed ? (add ? "Now trusted: " : "No longer trusted: ") : (add ? "Already trusted: " : "Was not trusted: ")) + ch.nameOf(who)), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> c) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer me = c.getSource().getPlayerOrException();
        Channels ch = Channels.get(c.getSource().getServer());
        String id = me.getStringUUID();
        int n = ch.channelsOwnedBy(id);
        c.getSource().sendSuccess(() -> Component.literal("You own " + n + " of " + Config.MAX_CHANNELS_PER_PLAYER.get() + " channels. Trusted: "
                + (ch.trusted(id).isEmpty() ? "nobody" : String.join(", ", ch.trusted(id).stream().map(ch::nameOf).toList()))), false);
        for (int i = 1; i <= Config.MAX_CHANNEL_NUMBER.get() && i <= 9999; i++) {
            List<Channels.Entry> es = ch.on(id, i);
            if (es.isEmpty()) continue;
            final int num = i;
            c.getSource().sendSuccess(() -> Component.literal("Channel " + num + ": " + es.size() + " nodes"), false);
        }
        return n;
    }
}
