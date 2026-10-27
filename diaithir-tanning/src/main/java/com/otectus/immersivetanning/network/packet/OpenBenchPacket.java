package com.otectus.immersivetanning.network.packet;

import com.otectus.immersivetanning.client.ClientPacketHandler;
import com.otectus.immersivetanning.leather.Finish;
import com.otectus.immersivetanning.trait.TraitRef;
import net.minecraft.core.BlockPos;
import com.otectus.immersivetanning.ImmersiveTanning;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Every piece the player can cut and stitch right now, one entry per recipe and leather group. */
public record OpenBenchPacket(int sessionId, BlockPos pos, List<Entry> entries) implements CustomPacketPayload {
    public static final Type<OpenBenchPacket> TYPE = new Type<>(ImmersiveTanning.id("open_bench"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBenchPacket> STREAM_CODEC = StreamCodec.ofMember(OpenBenchPacket::encode, OpenBenchPacket::decode);

    @Override
    public Type<OpenBenchPacket> type() {
        return TYPE;
    }

    public static final int MAX_ENTRIES = 512;

    public record Entry(Identifier recipe, ItemStack result, int leather, Finish finish, @Nullable TraitRef trait,
                        int integrity, List<ItemStack> auxiliary) {}

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(sessionId);
        buf.writeBlockPos(pos);
        int n = Math.min(entries.size(), MAX_ENTRIES);
        buf.writeVarInt(n);
        for (int i = 0; i < n; i++) {
            Entry e = entries.get(i);
            buf.writeIdentifier(e.recipe());
            ItemStack.STREAM_CODEC.encode(buf, e.result());
            buf.writeVarInt(e.leather());
            buf.writeEnum(e.finish());
            TraitRef.writeNullable(buf, e.trait());
            buf.writeVarInt(e.integrity());
            buf.writeVarInt(e.auxiliary().size());
            e.auxiliary().forEach(a -> ItemStack.STREAM_CODEC.encode(buf, a));
        }
    }

    public static OpenBenchPacket decode(RegistryFriendlyByteBuf buf) {
        int session = buf.readVarInt();
        BlockPos pos = buf.readBlockPos();
        int n = Math.min(buf.readVarInt(), MAX_ENTRIES);
        List<Entry> entries = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            Identifier recipe = buf.readIdentifier();
            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            int leather = buf.readVarInt();
            Finish finish = buf.readEnum(Finish.class);
            TraitRef trait = TraitRef.readNullable(buf);
            int integrity = buf.readVarInt();
            int auxCount = Math.min(buf.readVarInt(), 16);
            List<ItemStack> aux = new ArrayList<>(auxCount);
            for (int j = 0; j < auxCount; j++) aux.add(ItemStack.STREAM_CODEC.decode(buf));
            entries.add(new Entry(recipe, result, leather, finish, trait, integrity, List.copyOf(aux)));
        }
        return new OpenBenchPacket(session, pos, List.copyOf(entries));
    }

    public void handle(IPayloadContext ctx) {
        ClientPacketHandler.openBench(this);
    }
}
