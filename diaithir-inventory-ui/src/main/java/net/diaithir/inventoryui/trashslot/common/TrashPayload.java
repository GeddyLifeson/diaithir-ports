package net.diaithir.inventoryui.trashslot.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TrashPayload(int containerId, int stateId, int slot, int count, boolean restore, ItemStack restoreStack) implements CustomPacketPayload {
    public static final Type<TrashPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("diaithir_inventoryui", "trash_stack"));

    public static final StreamCodec<ByteBuf, TrashPayload> CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.VAR_INT.encode(buf, p.containerId);
                ByteBufCodecs.VAR_INT.encode(buf, p.stateId);
                ByteBufCodecs.VAR_INT.encode(buf, p.slot);
                ByteBufCodecs.VAR_INT.encode(buf, p.count);
                ByteBufCodecs.BOOL.encode(buf, p.restore);
            },
            buf -> new TrashPayload(
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ItemStack.EMPTY));

    /** slot -1 = carried stack; count 0 = whole stack. */
    public static TrashPayload trash(int containerId, int stateId, int slot, int count) {
        return new TrashPayload(containerId, stateId, slot, count, false, ItemStack.EMPTY);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToServer(TYPE, CODEC, TrashPayload::handle);
    }

    private static void handle(TrashPayload p, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            AbstractContainerMenu menu = player.containerMenu;
            if (!TrashServerConfig.ALLOW_TRASHING.get() || menu == null || menu.containerId != p.containerId) {
                return;
            }
            if (menu.getStateId() != p.stateId) {
                menu.sendAllDataToRemote();
                return;
            }
            if (p.slot < 0) {
                if (!menu.getCarried().isEmpty()) {
                    menu.setCarried(ItemStack.EMPTY);
                    menu.broadcastChanges();
                }
                return;
            }
            if (p.slot >= menu.slots.size()) {
                return;
            }
            var s = menu.getSlot(p.slot);
            if (!s.hasItem() || !s.mayPickup(player)) {
                menu.sendAllDataToRemote();
                return;
            }
            if (p.count <= 0 || p.count >= s.getItem().getCount()) {
                s.set(ItemStack.EMPTY);
            } else {
                s.remove(p.count);
            }
            s.setChanged();
            menu.broadcastChanges();
        });
    }
}
