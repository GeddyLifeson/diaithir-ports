package net.diaithir.waypoints;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Lighting, listing and walking waypoints. Cost is experience points, by distance in metres (1 block = 1 m). */
public final class Travel {
    static final double NEAR = 4.0;

    private Travel() {}

    public static void light(ServerPlayer p, BlockPos pos) {
        Identifier dim = p.level().dimension().identifier();
        List<Known> list = new ArrayList<>(p.getData(DiaithirWaypoints.KNOWN));
        list.removeIf(k -> k.dimension().equals(dim) && k.pos().equals(pos));
        list.add(new Known(dim, pos, Names.of(pos)));
        p.setData(DiaithirWaypoints.KNOWN, List.copyOf(list));
        p.sendSystemMessage(Component.literal("Lit: " + Names.of(pos)));
    }

    static boolean nearStone(ServerPlayer p) {
        BlockPos c = p.blockPosition();
        for (BlockPos q : BlockPos.betweenClosed(c.offset(-4, -2, -4), c.offset(4, 2, 4))) {
            if (p.level().getBlockState(q).is(DiaithirWaypoints.STONE.get())) {
                return true;
            }
        }
        return false;
    }

    public static void show(ServerPlayer p) {
        List<Known> list = p.getData(DiaithirWaypoints.KNOWN);
        p.sendSystemMessage(Component.literal("Waypoints: " + list.size()));
        for (int i = 0; i < list.size(); i++) {
            Known k = list.get(i);
            p.sendSystemMessage(Component.literal(" " + (i + 1) + ". " + k.name() + " (" + k.pos().toShortString() + ", " + k.dimension() + ")")
                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/route go " + (i + 1)))));
        }
    }

    /** Experience points the player holds now (level and bar), not the lifetime total. */
    static int points(ServerPlayer p) {
        int l = p.experienceLevel;
        int base = l <= 16 ? l * l + 6 * l : l <= 31 ? (int) (2.5 * l * l - 40.5 * l + 360) : (int) (4.5 * l * l - 162.5 * l + 2220);
        return base + (int) (p.experienceProgress * p.getXpNeededForNextLevel());
    }

    static int cost(ServerPlayer p, Known k) {
        double d = k.dimension().equals(p.level().dimension().identifier()) ? Math.sqrt(p.blockPosition().distSqr(k.pos())) : 2000;
        return (int) Math.min(200, Math.ceil(d / 100.0));
    }

    /** Walks to waypoint number index (0 based). Scrolls skip the near-stone rule. */
    public static boolean go(ServerPlayer p, int index, boolean scroll) {
        List<Known> list = p.getData(DiaithirWaypoints.KNOWN);
        if (index < 0 || index >= list.size()) {
            p.sendSystemMessage(Component.literal("No such waypoint."));
            return false;
        }
        if (!scroll && !nearStone(p)) {
            p.sendSystemMessage(Component.literal("Stand beside a waypoint stone first."));
            return false;
        }
        Known k = list.get(index);
        int xp = cost(p, k);
        if (!p.isCreative() && points(p) < xp) {
            p.sendSystemMessage(Component.literal("Needs " + xp + " experience points."));
            return false;
        }
        ServerLevel dest = p.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, k.dimension()));
        if (dest == null || !dest.getBlockState(k.pos()).is(DiaithirWaypoints.STONE.get())) {
            p.sendSystemMessage(Component.literal(k.name() + " is gone."));
            List<Known> keep = new ArrayList<>(list);
            keep.remove(index);
            p.setData(DiaithirWaypoints.KNOWN, List.copyOf(keep));
            return false;
        }
        if (!p.isCreative()) {
            p.giveExperiencePoints(-xp);
        }
        p.teleportTo(dest, k.pos().getX() + 0.5, k.pos().getY() + 1, k.pos().getZ() + 0.5, Set.of(), p.getYRot(), p.getXRot(), true);
        p.sendSystemMessage(Component.literal("Arrived at " + k.name() + "."));
        return true;
    }

    public static void commands(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("route")
            .then(Commands.literal("list").executes(c -> {
                show(c.getSource().getPlayerOrException());
                return 1;
            }))
            .then(Commands.literal("light").executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                BlockPos o = p.blockPosition();
                for (BlockPos q : BlockPos.betweenClosed(o.offset(-4, -2, -4), o.offset(4, 2, 4))) {
                    if (p.level().getBlockState(q).is(DiaithirWaypoints.STONE.get())) {
                        light(p, q.immutable());
                        return 1;
                    }
                }
                p.sendSystemMessage(Component.literal("No stone within 4 m."));
                return 0;
            }))
            .then(Commands.literal("go").then(Commands.argument("n", IntegerArgumentType.integer(1)).executes(c ->
                go(c.getSource().getPlayerOrException(), IntegerArgumentType.getInteger(c, "n") - 1, false) ? 1 : 0)))
            .then(Commands.literal("forget").then(Commands.argument("n", IntegerArgumentType.integer(1)).executes(c -> {
                ServerPlayer p = c.getSource().getPlayerOrException();
                List<Known> l = new ArrayList<>(p.getData(DiaithirWaypoints.KNOWN));
                int i = IntegerArgumentType.getInteger(c, "n") - 1;
                if (i >= l.size()) {
                    return 0;
                }
                l.remove(i);
                p.setData(DiaithirWaypoints.KNOWN, List.copyOf(l));
                return 1;
            }))));
    }
}
