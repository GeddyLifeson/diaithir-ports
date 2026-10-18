package me.desht.pneumaticcraft.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;

public class PNCStreamCodecs {
    // 26.1's VECTOR3F codec is for Vector3fc; our packets carry mutable Vector3f
    public static final StreamCodec<ByteBuf, Vector3f> VECTOR3F = ByteBufCodecs.VECTOR3F.map(Vector3f::new, v -> v);
}
