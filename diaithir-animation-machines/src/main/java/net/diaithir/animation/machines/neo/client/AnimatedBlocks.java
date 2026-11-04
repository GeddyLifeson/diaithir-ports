package net.diaithir.animation.machines.neo.client;

import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Blocks whose static chunk model is hidden because AnimatedBlockRenderer draws them instead. Filled on each renderer reload. */
public final class AnimatedBlocks {
    private static final Set<Block> HIDDEN = ConcurrentHashMap.newKeySet();

    private AnimatedBlocks() {}

    public static void clear() { HIDDEN.clear(); }
    public static void add(Block b) { HIDDEN.add(b); }
    public static boolean hidden(Block b) { return HIDDEN.contains(b); }
    public static int count() { return HIDDEN.size(); }
}
