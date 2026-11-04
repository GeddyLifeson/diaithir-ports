package net.diaithir.ships.cargo

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier

/** One carrier ship's deck and loads, server to client. A load is {type id, x, y, z, turned}. */
class ShipCargoPacket(val shipId: Long, val ox: Int, val oy: Int, val oz: Int, val w: Int, val h: Int, val l: Int, val loads: List<Load>) : CustomPacketPayload {
    class Load(val type: String, val x: Int, val y: Int, val z: Int, val turned: Boolean)

    override fun type() = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ShipCargoPacket>(Identifier.fromNamespaceAndPath("vs_yard", "ship_cargo"))
        val CODEC: StreamCodec<FriendlyByteBuf, ShipCargoPacket> = StreamCodec.of(
            { b, p ->
                b.writeVarLong(p.shipId); b.writeVarInt(p.ox); b.writeVarInt(p.oy); b.writeVarInt(p.oz); b.writeVarInt(p.w); b.writeVarInt(p.h); b.writeVarInt(p.l)
                b.writeVarInt(p.loads.size)
                for (d in p.loads) { b.writeUtf(d.type); b.writeVarInt(d.x); b.writeVarInt(d.y); b.writeVarInt(d.z); b.writeBoolean(d.turned) }
            },
            { b ->
                val id = b.readVarLong(); val ox = b.readVarInt(); val oy = b.readVarInt(); val oz = b.readVarInt()
                val w = b.readVarInt(); val h = b.readVarInt(); val l = b.readVarInt()
                val n = b.readVarInt().coerceIn(0, 4096)
                ShipCargoPacket(id, ox, oy, oz, w, h, l, List(n) { Load(b.readUtf(64), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readBoolean()) })
            })
    }
}
