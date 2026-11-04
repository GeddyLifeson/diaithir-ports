package org.diaithir.mega;

/**
 * Tick-sliced digging. {@link #tick} turns available power into a whole number of 1 m3 blocks per call, carrying the fraction,
 * and draws the volume from a {@link Deposit} so its magnitude falls as it is dug.
 */
public final class Dig {
    private final MegaSpec spec;
    private double carry;

    public Dig(MegaSpec spec) { this.spec = spec; }

    /** Deposit magnitude: volume left in m3. The assay roll reads {@link #fraction} to scale yield. */
    public static final class Deposit {
        private final double initial;
        private double left;

        public Deposit(double m3) { this(m3, m3); }

        /** A partly dug deposit: {@code left} of {@code initial} m3 remain. */
        public Deposit(double initial, double left) { this.initial = initial; this.left = Math.max(0, Math.min(initial, left)); }

        public double initial() { return initial; }

        double take(double m3) {
            double t = Math.min(m3, left);
            left -= t;
            return t;
        }

        void give(double m3) { left = Math.min(initial, left + m3); }

        public double left() { return left; }
        public double fraction() { return initial == 0 ? 0 : left / initial; }
    }

    /**
     * Advance one tick. {@code watts} is the power actually delivered (0 to full), {@code hopperFreeBlocks} limits output to free
     * container space so nothing spills.
     * @return whole blocks (m3) dug this tick
     */
    public int tick(double watts, int hopperFreeBlocks, Deposit d) {
        if (spec.eta() <= 0) return 0;
        double rate = Math.min(watts, spec.powerMW() * 1e6) * spec.eta() / (MegaSpec.specificEnergy(spec.medium()) * 1e6);
        carry += rate / 20.0;                          // 20 ticks per second
        int blocks = (int) Math.min(Math.floor(carry), hopperFreeBlocks);
        blocks = (int) d.take(blocks);
        carry -= blocks;
        if (hopperFreeBlocks <= 0 || d.left() <= 0) carry = Math.min(carry, 1.0);  // stalled: do not bank unlimited work
        return blocks;
    }

    /** Undo blocks that could not be delivered: the work is kept and the deposit gets its volume back. */
    public void refund(int blocks, Deposit d) {
        carry += blocks;
        d.give(blocks);
    }
}
