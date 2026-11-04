package com.diaithir.end.echoes;

import net.neoforged.bus.api.Event;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;

/** Global Events hook point: posted when a player first enters an echoes structure. */
public class EchoesEvents extends Event {
    private final Identifier structure;
    private final BlockPos pos;
    private final int energyJoules;

    public EchoesEvents(Identifier structure, BlockPos pos, int energyJoules) {
        this.structure = structure;
        this.pos = pos;
        this.energyJoules = energyJoules;
    }

    public Identifier structure() { return structure; }
    public BlockPos pos() { return pos; }
    /** Magic cost in J, multiples of the 100 J quantum. */
    public int energyJoules() { return energyJoules; }
}
