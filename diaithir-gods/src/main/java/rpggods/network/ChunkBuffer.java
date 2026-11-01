package rpggods.network;

import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Splits large data maps into several packets (a single packet with all 1520 perks
 * exceeds the 2 MiB NBT limit) and reassembles them on the client.
 */
public final class ChunkBuffer {

    public static final int CHUNK_SIZE = 100;

    private static final Map<CustomPacketPayload.Type<?>, Map<Identifier, Object>> PENDING = new HashMap<>();

    private ChunkBuffer() {}

    /** Splits the map in chunks of at most {@link #CHUNK_SIZE} entries; always returns at least one chunk. */
    public static <V> List<Map<Identifier, V>> split(Map<Identifier, V> data) {
        List<Map<Identifier, V>> list = new ArrayList<>();
        Map<Identifier, V> current = new LinkedHashMap<>();
        for (Map.Entry<Identifier, V> e : data.entrySet()) {
            current.put(e.getKey(), e.getValue());
            if (current.size() >= CHUNK_SIZE) {
                list.add(current);
                current = new LinkedHashMap<>();
            }
        }
        if (!current.isEmpty() || list.isEmpty()) {
            list.add(current);
        }
        return list;
    }

    /** Called on the client; invokes the consumer with the full map once the last chunk arrives. */
    @SuppressWarnings("unchecked")
    public static <V> void accept(CustomPacketPayload.Type<?> type, int index, int total, Map<Identifier, V> chunk, Consumer<Map<Identifier, V>> finisher) {
        Map<Identifier, Object> pending = PENDING.computeIfAbsent(type, t -> new LinkedHashMap<>());
        if (index == 0) {
            pending.clear();
        }
        pending.putAll((Map<Identifier, Object>) chunk);
        if (index >= total - 1) {
            Map<Identifier, V> all = new LinkedHashMap<>((Map<Identifier, V>) (Map<?, ?>) pending);
            pending.clear();
            finisher.accept(all);
        }
    }
}
