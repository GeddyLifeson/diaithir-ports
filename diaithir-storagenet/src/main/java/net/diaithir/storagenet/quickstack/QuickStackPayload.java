package net.diaithir.storagenet.quickstack;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.util.List;

/** Client to server: one storage action on the menu with the given container id (ignored for the actions that need no menu). */
public record QuickStackPayload(int containerId, Action action) implements CustomPacketPayload {
    public enum Action { LOOT_ALL, DEPOSIT_ALL, QUICK_STACK, RESTOCK, SORT_STORAGE, SORT_INVENTORY, QUICK_STACK_NEARBY }

    public static final Type<QuickStackPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("diaithirstoragenet", "quick_stack"));
    public static final StreamCodec<FriendlyByteBuf, QuickStackPayload> CODEC = StreamCodec.of(
            (buf, p) -> { buf.writeVarInt(p.containerId); buf.writeEnum(p.action); },
            buf -> new QuickStackPayload(buf.readVarInt(), buf.readEnum(Action.class)));

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToServer(TYPE, CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player) p.handle(player);
        }));
    }

    private void handle(ServerPlayer player) {
        if (player.isSpectator()) return;
        boolean hotbar = QuickStackConfig.HOTBAR_PROTECTION;
        switch (action) {
            case QUICK_STACK_NEARBY -> QuickStackCore.quickStackToNearbyStorages(player, hotbar, QuickStackConfig.SMART_DEPOSIT);
            case SORT_INVENTORY -> QuickStackCore.sortPlayerItems(player, QuickStackConfig.SORT, hotbar);
            default -> {
                if (player.containerMenu == null || player.containerMenu.containerId != containerId) return;
                List<Slot> slots = InventoryUtils.getStorageSlots(player.containerMenu, player);
                if (slots.isEmpty() || !InventoryUtils.hasUsableSlot(slots, player)) {
                    player.sendSystemMessage(Component.literal("This storage can't be used with quick stack."));
                    return;
                }
                StorageAccess storage = new SlotStorageAccess(slots);
                switch (action) {
                    case LOOT_ALL -> QuickStackCore.lootAll(player, storage, hotbar);
                    case DEPOSIT_ALL -> QuickStackCore.depositAll(player, storage, hotbar);
                    case QUICK_STACK -> QuickStackCore.quickStack(player, storage, hotbar, QuickStackConfig.SMART_DEPOSIT);
                    case RESTOCK -> QuickStackCore.restock(player, storage, hotbar);
                    case SORT_STORAGE -> QuickStackCore.sortStorageItems(player, storage, QuickStackConfig.SORT);
                    default -> { return; }
                }
                player.containerMenu.broadcastChanges();
            }
        }
    }
}
