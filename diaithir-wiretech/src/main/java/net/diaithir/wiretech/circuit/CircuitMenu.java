package net.diaithir.wiretech.circuit;

import net.diaithir.wiretech.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** A slotless menu for one circuit block. The server refreshes the client every 5 ticks, so the screen shows live signals. */
public final class CircuitMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public CircuitKind kind = CircuitKind.CONSTANT;
    public Tier tier = Tier.MECHANICAL;
    private final Player player;
    private int age;
    /** Latest view on the client. */
    public CircuitPayloads.View view;
    /** Last error or note from the server, kept until the next Apply. */
    public String msg = "";

    /** Client side. */
    public CircuitMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos());
        this.kind = CircuitKind.values()[buf.readVarInt()];
        this.tier = Tier.values()[buf.readVarInt()];
        this.view = CircuitPayloads.View.CODEC.decode(buf);
    }

    public CircuitMenu(int id, Inventory inv, BlockPos pos) {
        super(Machines.CIRCUIT_MENU.get(), id);
        this.pos = pos;
        this.player = inv.player;
    }

    /** Writes what the client constructor reads. */
    public static void write(RegistryFriendlyByteBuf buf, CircuitNodeBE be) {
        buf.writeBlockPos(be.getBlockPos());
        buf.writeVarInt(be.kind.ordinal());
        buf.writeVarInt(be.tier.ordinal());
        CircuitPayloads.View.CODEC.encode(buf, be.view("", true));
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (player instanceof ServerPlayer sp && ++age % 5 == 0 && player.level().getBlockEntity(pos) instanceof CircuitNodeBE be) {
            PacketDistributor.sendToPlayer(sp, be.view("", false));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player p, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player p) {
        return p.level().getBlockEntity(pos) instanceof CircuitNodeBE && p.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
