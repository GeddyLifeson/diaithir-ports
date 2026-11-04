package net.diaithir.storagenet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * The loaded wireless transmitters of every dimension. Two networks are linked when each has a transmitter with the same frequency;
 * a transmitter in an unloaded chunk is not registered, so its network is simply not reachable until it loads again.
 * Folded from the idea of the Interdimensional Wireless Transmitter (MIT, starforcraft): one frequency, any distance, any dimension.
 */
public final class Links {
    private Links() {}

    private static final Set<WirelessBlockEntity> ALL = new LinkedHashSet<>();

    static synchronized void register(WirelessBlockEntity t) {
        ALL.add(t);
    }

    static synchronized void unregister(WirelessBlockEntity t) {
        ALL.remove(t);
    }

    /** The other networks reachable from this one through transmitters of a matching frequency (one hop, no chains). */
    static synchronized List<Network> peersOf(Network home) {
        List<Network> out = new ArrayList<>();
        Set<Integer> freqs = new LinkedHashSet<>();
        for (WirelessBlockEntity t : ALL) if (!t.isRemoved() && inside(home, t.getLevel(), t.getBlockPos())) freqs.add(t.frequency());
        if (freqs.isEmpty()) return out;
        for (WirelessBlockEntity t : ALL) {
            if (t.isRemoved() || !freqs.contains(t.frequency()) || inside(home, t.getLevel(), t.getBlockPos())) continue;
            Network n = Networks.find(t.getLevel(), t.getBlockPos());
            if (n != null && n != home && !out.contains(n)) out.add(n);
        }
        return out;
    }

    private static boolean inside(Network n, Level level, BlockPos pos) {
        return level == n.level() && n.members().contains(pos.asLong());
    }

    /** Number of registered transmitters on a frequency (for the status line). */
    static synchronized int count(int frequency) {
        int c = 0;
        for (WirelessBlockEntity t : ALL) if (!t.isRemoved() && t.frequency() == frequency) c++;
        return c;
    }
}
