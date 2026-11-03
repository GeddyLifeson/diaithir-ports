package net.diaithir.claims;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.fml.InterModComms;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

final class ClientHooks {
    private static final Map<Long, long[]> ASKED = new HashMap<>();
    private static final Map<Long, String> OWNERS = new HashMap<>();

    private ClientHooks() {}

    static void open(MapPayload p) {
        Minecraft.getInstance().setScreen(new MapScreen(p));
    }

    static void info(ClaimInfo i) {
        OWNERS.put(ChunkPos.pack(i.cx(), i.cz()), i.owner());
    }

    /** Hands Look-At a provider that shows the holder of the chunk under the crosshair; asks the server at most every 3 s per chunk. */
    static void register() {
        InterModComms.sendTo("diaithirlookat", "any", () -> (BiFunction<Level, BlockPos, List<String>>) (level, pos) -> {
            ChunkPos c = ChunkPos.containing(pos);
            long key = c.pack();
            long now = System.currentTimeMillis();
            long[] last = ASKED.get(key);
            if ((last == null || now - last[0] > 3000) && Minecraft.getInstance().getConnection() != null
                && Minecraft.getInstance().getConnection().hasChannel(ClaimQuery.TYPE)) {
                ASKED.put(key, new long[] {now});
                ClientPacketDistributor.sendToServer(new ClaimQuery(c.x(), c.z()));
            }
            String owner = OWNERS.get(key);
            return owner == null || owner.isEmpty() ? List.of() : List.of("Claimed by " + owner);
        });
    }
}
