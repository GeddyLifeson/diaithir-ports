package org.diaithir.mega.gantry;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

/**
 * Gantry connectivity for assembling a moving gantry (mega crane, dragline boom) into a Valkyrien Skies 2 ship.
 *
 * Rewritten from the connectivity rules in Create Aeronautics / Simulated (Creators-of-Aeronautics/Simulated-Project, MIT code:
 * SimAssemblyContraption.moveGantryPinion / moveGantryShaft and SimBlockMovementChecks): a carriage pulls in the block it faces and
 * every shaft on its rotation axis; a shaft pulls in the next shaft with the same facing along its axis and any carriage facing it.
 * Copyright (c) The Simulated Team / The Creators of Aeronautics, MIT; notice in LICENSES.md. No assets copied (those are All Rights
 * Reserved). Changes: Create/Minecraft block types replaced by a {@link Grid} so the walk is pure Java; the result is the set of
 * positions to hand to the VS2 ship assembler, with a size cap so a mega gantry cannot swallow a world.
 */
public final class GantryAssembly {
    private GantryAssembly() {}

    public enum Kind { AIR, SHAFT, CARRIAGE, OTHER }

    /** Axis index: 0 x, 1 y, 2 z. Direction index 0..5 = -x,+x,-y,+y,-z,+z. */
    public record Pos(int x, int y, int z) {
        Pos rel(int dir) {
            int a = dir / 2, s = (dir % 2 == 0) ? -1 : 1;
            return new Pos(x + (a == 0 ? s : 0), y + (a == 1 ? s : 0), z + (a == 2 ? s : 0));
        }
    }

    public interface Grid {
        Kind kind(Pos p);
        /** Facing direction (0..5) of a shaft or carriage. */
        int facing(Pos p);
        /** Rotation axis (0..2) of a carriage. */
        int axis(Pos p);
    }

    /** Positions pulled into the assembly starting from {@code start}, at most {@code limit}; null when over the cap. */
    public static Set<Pos> collect(Grid g, Pos start, int limit) {
        Set<Pos> visited = new LinkedHashSet<>();
        Queue<Pos> frontier = new ArrayDeque<>();
        frontier.add(start);
        while (!frontier.isEmpty()) {
            Pos p = frontier.poll();
            if (!visited.add(p)) continue;
            if (visited.size() > limit) return null;
            switch (g.kind(p)) {
                case CARRIAGE -> carriage(g, p, frontier, visited);
                case SHAFT -> shaft(g, p, frontier, visited);
                default -> {}
            }
        }
        visited.removeIf(p -> g.kind(p) == Kind.AIR);
        return visited;
    }

    private static void carriage(Grid g, Pos p, Queue<Pos> frontier, Set<Pos> visited) {
        Pos ahead = p.rel(g.facing(p));
        if (!visited.contains(ahead)) frontier.add(ahead);
        int axis = g.axis(p);
        for (int d = axis * 2; d < axis * 2 + 2; d++) {
            Pos o = p.rel(d);
            if (g.kind(o) == Kind.SHAFT && g.facing(o) / 2 == d / 2 && !visited.contains(o)) frontier.add(o);
        }
    }

    private static void shaft(Grid g, Pos p, Queue<Pos> frontier, Set<Pos> visited) {
        int facing = g.facing(p);
        for (int d = 0; d < 6; d++) {
            Pos o = p.rel(d);
            if (visited.contains(o)) continue;
            Kind k = g.kind(o);
            if (d / 2 == facing / 2 && k == Kind.SHAFT && g.facing(o) == facing) frontier.add(o);
            else if (k == Kind.CARRIAGE && g.facing(o) == d) frontier.add(o);
        }
    }

    /** Convenience for tests and data-driven grids. */
    public static Grid of(Function<Pos, Kind> kind, Function<Pos, Integer> facing, Function<Pos, Integer> axis) {
        return new Grid() {
            public Kind kind(Pos p) { return kind.apply(p); }
            public int facing(Pos p) { return facing.apply(p); }
            public int axis(Pos p) { return axis.apply(p); }
        };
    }
}
