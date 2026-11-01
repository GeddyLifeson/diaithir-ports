package com.github.ysbbbbbb.kaleidoscopetavern.game.tap;

import com.github.ysbbbbbb.kaleidoscopetavern.api.blockentity.ITapBehavior;
import com.google.common.collect.Maps;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class TapBehaviorManager {
    private static final Map<Block, ITapBehavior> BEHAVIOR_MAP = Maps.newHashMap();

    public static void register(Block block, ITapBehavior behavior) {
        BEHAVIOR_MAP.put(block, behavior);
    }

    public static boolean contains(Block block) {
        return BEHAVIOR_MAP.containsKey(block);
    }

    public static ITapBehavior get(Block block) {
        return BEHAVIOR_MAP.get(block);
    }
}
