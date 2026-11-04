package net.diaithir.foundry;

import net.minecraft.world.item.Item;

/** A castable shape: how many molten units it costs and which cast item selects it. */
public enum Shape {
    INGOT(90, "ingot"),
    NUGGET(10, "nugget"),
    PLATE(90, "plate"),
    ROD(45, "rod"),
    GEAR(360, "gear"),
    BLOCK(810, "block");

    private final int cost;
    private final String id;

    Shape(int cost, String id) {
        this.cost = cost;
        this.id = id;
    }

    public int cost() {
        return cost;
    }

    public String id() {
        return id;
    }

    public Item cast() {
        return switch (this) {
            case INGOT -> DiaithirFoundry.CAST_INGOT.get();
            case NUGGET -> DiaithirFoundry.CAST_NUGGET.get();
            case PLATE -> DiaithirFoundry.CAST_PLATE.get();
            case ROD -> DiaithirFoundry.CAST_ROD.get();
            case GEAR -> DiaithirFoundry.CAST_GEAR.get();
            case BLOCK -> DiaithirFoundry.CAST_BLOCK.get();
        };
    }

    public static Shape forCast(Item item) {
        for (Shape s : values()) if (s.cast() == item) return s;
        return null;
    }
}
