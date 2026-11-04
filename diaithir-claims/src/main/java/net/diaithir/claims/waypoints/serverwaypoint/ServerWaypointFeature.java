package net.diaithir.claims.waypoints.serverwaypoint;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Server Waypoint fold (2676959, MIT): shared named waypoint lists kept on the server, listed, navigated and
 * teleported to by command. No client mod needed. Commands: /wp list [list], /wp nav <list> <name>, /wp nav off
 * (everyone); /wp addlist, add, remove, removelist, tp (game masters). Navigation shows distance and compass
 * direction on the action bar once a second.
 */
public final class ServerWaypointFeature {
    private static final Map<UUID, WaypointBook.Wp> NAV = new ConcurrentHashMap<>();

    private ServerWaypointFeature() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> commands(e.getDispatcher()));
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, e -> {
            if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 0) return;
            WaypointBook.Wp wp = NAV.get(p.getUUID());
            if (wp == null) return;
            if (!p.level().dimension().identifier().toString().equals(wp.dim())) {
                p.sendSystemMessage(Component.literal("Waypoint is in " + wp.dim()), true);
                return;
            }
            p.sendSystemMessage(Component.literal(describe(p.getX(), p.getZ(), wp)), true);
        });
    }

    /** Pure helper: "123 m NE". */
    public static String describe(double px, double pz, WaypointBook.Wp wp) {
        double dx = wp.x() + 0.5 - px, dz = wp.z() + 0.5 - pz;
        long m = Math.round(Math.sqrt(dx * dx + dz * dz));
        if (m < 3) return "arrived";
        String[] names = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        // atan2(-dx, dz) is minecraft yaw (0 = south, 90 = west); eight sectors of 45 degrees
        double yaw = Math.toDegrees(Math.atan2(-dx, dz));
        int idx = (int) Math.floorMod(Math.round(yaw / 45.0), 8L);
        return m + " m " + names[idx];
    }

    private static void commands(CommandDispatcher<CommandSourceStack> d) {
        SuggestionProvider<CommandSourceStack> lists = (c, b) ->
                SharedSuggestionProvider.suggest(WaypointBook.get(c.getSource().getServer()).lists().keySet(), b);
        SuggestionProvider<CommandSourceStack> names = (c, b) -> {
            var l = WaypointBook.get(c.getSource().getServer()).lists().get(StringArgumentType.getString(c, "list"));
            return SharedSuggestionProvider.suggest(l == null ? Set.<String>of() : l.keySet(), b);
        };
        java.util.function.Predicate<CommandSourceStack> gm = Commands.hasPermission(Commands.LEVEL_GAMEMASTERS);
        d.register(Commands.literal("wp")
                .then(Commands.literal("list").executes(c -> {
                    var m = WaypointBook.get(c.getSource().getServer()).lists();
                    c.getSource().sendSuccess(() -> Component.literal(m.isEmpty() ? "No waypoint lists" : m.entrySet().stream()
                            .map(e -> e.getKey() + " (" + e.getValue().size() + ")").collect(Collectors.joining(", "))), false);
                    return m.size();
                }).then(Commands.argument("list", StringArgumentType.string()).suggests(lists).executes(c -> {
                    var l = WaypointBook.get(c.getSource().getServer()).lists().get(StringArgumentType.getString(c, "list"));
                    if (l == null) { c.getSource().sendFailure(Component.literal("No such list")); return 0; }
                    c.getSource().sendSuccess(() -> Component.literal(l.isEmpty() ? "Empty list" : l.entrySet().stream()
                            .map(e -> e.getKey() + " " + e.getValue().dim() + " " + e.getValue().x() + " " + e.getValue().y() + " " + e.getValue().z())
                            .collect(Collectors.joining("\n"))), false);
                    return l.size();
                })))
                .then(Commands.literal("addlist").requires(gm).then(Commands.argument("list", StringArgumentType.string()).executes(c -> {
                    boolean ok = WaypointBook.get(c.getSource().getServer()).addList(StringArgumentType.getString(c, "list"));
                    if (!ok) c.getSource().sendFailure(Component.literal("List exists"));
                    return ok ? 1 : 0;
                })))
                .then(Commands.literal("removelist").requires(gm).then(Commands.argument("list", StringArgumentType.string()).suggests(lists).executes(c ->
                        WaypointBook.get(c.getSource().getServer()).removeList(StringArgumentType.getString(c, "list")) ? 1 : 0)))
                .then(Commands.literal("add").requires(gm).then(Commands.argument("list", StringArgumentType.string()).suggests(lists)
                        .then(Commands.argument("name", StringArgumentType.string()).executes(c -> {
                            BlockPos p = BlockPos.containing(c.getSource().getPosition());
                            return add(c, p);
                        }).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(c ->
                                add(c, BlockPosArgument.getBlockPos(c, "pos")))))))
                .then(Commands.literal("remove").requires(gm).then(Commands.argument("list", StringArgumentType.string()).suggests(lists)
                        .then(Commands.argument("name", StringArgumentType.string()).suggests(names).executes(c ->
                                WaypointBook.get(c.getSource().getServer()).remove(StringArgumentType.getString(c, "list"), StringArgumentType.getString(c, "name")) ? 1 : 0))))
                .then(Commands.literal("nav")
                        .then(Commands.literal("off").executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            return NAV.remove(p.getUUID()) != null ? 1 : 0;
                        }))
                        .then(Commands.argument("list", StringArgumentType.string()).suggests(lists)
                                .then(Commands.argument("name", StringArgumentType.string()).suggests(names).executes(c -> {
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    var wp = WaypointBook.get(c.getSource().getServer()).find(StringArgumentType.getString(c, "list"), StringArgumentType.getString(c, "name"));
                                    if (wp == null) { c.getSource().sendFailure(Component.literal("No such waypoint")); return 0; }
                                    NAV.put(p.getUUID(), wp);
                                    return 1;
                                }))))
                .then(Commands.literal("tp").requires(gm).then(Commands.argument("list", StringArgumentType.string()).suggests(lists)
                        .then(Commands.argument("name", StringArgumentType.string()).suggests(names).executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            var wp = WaypointBook.get(c.getSource().getServer()).find(StringArgumentType.getString(c, "list"), StringArgumentType.getString(c, "name"));
                            if (wp == null) { c.getSource().sendFailure(Component.literal("No such waypoint")); return 0; }
                            ServerLevel lvl = c.getSource().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(wp.dim())));
                            if (lvl == null) { c.getSource().sendFailure(Component.literal("Unknown dimension " + wp.dim())); return 0; }
                            p.teleportTo(lvl, wp.x() + 0.5, wp.y(), wp.z() + 0.5, Set.of(), p.getYRot(), p.getXRot(), true);
                            return 1;
                        })))));
    }

    private static int add(com.mojang.brigadier.context.CommandContext<CommandSourceStack> c, BlockPos pos) {
        String list = StringArgumentType.getString(c, "list"), name = StringArgumentType.getString(c, "name");
        String dim = c.getSource().getLevel().dimension().identifier().toString();
        boolean ok = WaypointBook.get(c.getSource().getServer()).put(list, name, new WaypointBook.Wp(dim, pos.getX(), pos.getY(), pos.getZ()));
        if (!ok) c.getSource().sendFailure(Component.literal("No such list"));
        return ok ? 1 : 0;
    }
}
