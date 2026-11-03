package net.diaithir.lookat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.fml.InterModComms;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * Extra lines per block. Other modules register one with
 * {@code InterModComms.sendTo("diaithirlookat", "block/<namespace:path>", () -> (BiFunction<Level, BlockPos, List<String>>) ...)};
 * nothing to compile against. Every block also shows stored energy when it exposes the energy capability to the client.
 */
public final class Providers {
    private static final Map<Identifier, BiFunction<Level, BlockPos, List<String>>> BY_BLOCK = new HashMap<>();

    private Providers() {}

    @SuppressWarnings("unchecked")
    static void collect() {
        InterModComms.getMessages(DiaithirLookAt.ID, m -> m.startsWith("block/")).forEach(msg -> {
            Object o = msg.messageSupplier().get();
            if (o instanceof BiFunction<?, ?, ?> f) {
                BY_BLOCK.put(Identifier.parse(msg.method().substring(6)), (BiFunction<Level, BlockPos, List<String>>) f);
            }
        });
    }

    static List<String> lines(Level level, BlockPos pos, Identifier block) {
        List<String> out = new ArrayList<>();
        var energy = level.getCapability(Capabilities.Energy.BLOCK, pos, null);
        if (energy != null && energy.getCapacityAsLong() > 0) {
            out.add(energy.getAmountAsLong() + " / " + energy.getCapacityAsLong() + " J");
        }
        BiFunction<Level, BlockPos, List<String>> f = BY_BLOCK.get(block);
        if (f != null) {
            out.addAll(f.apply(level, pos));
        }
        return out;
    }
}
