package immersive_melodies.network;

import immersive_melodies.resources.Melody;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public interface FragmentedMessage extends ImmersivePayload {
    Map<String, Queue<byte[]>> buffer = new ConcurrentHashMap<>();

    String name();

    byte[] fragment();

    int length();

    @Override
    default void handle(Player e) {
        String identifier = this.getClass().getSimpleName() + ":" + (e == null ? "local" : e.getStringUUID()) + ":" + name();
        Queue<byte[]> byteBuffer = buffer.computeIfAbsent(identifier, k -> new ConcurrentLinkedQueue<>());
        byteBuffer.add(fragment());

        if (byteBuffer.stream().mapToInt(f -> f.length).sum() >= length()) {
            // Assemble
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            for (byte[] b : byteBuffer) {
                buffer.writeBytes(b);
            }

            finish(e, name(), Melody.STREAM_CODEC.decode(buffer));

            FragmentedMessage.buffer.remove(identifier);
        }
    }

    void finish(Player e, String name, Melody melody);
}