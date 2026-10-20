package untamedwilds.util;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.ValueInput;

/**
 * Helpers for reading saved data through {@link ValueInput}, which has no {@code contains}.
 */
public class NbtUtil {

    /** True when {@code key} holds a number, string or compound in the input. */
    public static boolean has(ValueInput input, String key) {
        return input.read(key, Codec.INT).isPresent()
                || input.read(key, Codec.STRING).isPresent()
                || input.read(key, net.minecraft.core.UUIDUtil.CODEC).isPresent()
                || input.child(key).isPresent();
    }
}
