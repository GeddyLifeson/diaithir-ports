package net.diaithir.wiretech;

/** The three wire tiers. A wire only joins connectors of its own tier. Loss is the share of FE lost per block of wire. */
public enum WireType {
    COPPER("copper", "Kellivane", 16, 256, 0.015, 0xB87333),
    ELECTRUM("electrum", "Ostrimeth", 24, 1024, 0.008, 0xE6CF6E),
    STEEL("steel", "Varkhulon", 32, 4096, 0.004, 0x8D939C);

    public final String id;
    public final String display;
    public final int range;
    public final int throughput;
    public final double lossPerBlock;
    public final int color;

    WireType(String id, String display, int range, int throughput, double lossPerBlock, int color) {
        this.id = id;
        this.display = display;
        this.range = range;
        this.throughput = throughput;
        this.lossPerBlock = lossPerBlock;
        this.color = color;
    }

    /** Longest wire in blocks, after the server config scale. */
    public double maxRange() {
        return range * Config.WIRE_RANGE_SCALE.get();
    }

    /** Share of FE that survives a wire of this length, from 0.1 to 1. */
    public double efficiency(double length) {
        return Math.max(0.1, 1.0 - lossPerBlock * Config.WIRE_LOSS_SCALE.get() * length);
    }
}
