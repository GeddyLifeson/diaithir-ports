package net.diaithir.freight;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** The inventories of every load in one container, deployed or carried, presented as one Container; slot numbers run load by load. */
final class ContainerView implements Container {
    private final List<? extends Container> loads;
    private final int size;

    ContainerView(List<? extends Container> loads) {
        this.loads = loads;
        int n = 0;
        for (Container l : loads) n += l.getContainerSize();
        this.size = n;
    }

    private Container part(int slot) {
        for (Container l : loads) {
            if (slot < l.getContainerSize()) return l;
            slot -= l.getContainerSize();
        }
        return null;
    }

    private int local(int slot) {
        for (Container l : loads) {
            if (slot < l.getContainerSize()) return slot;
            slot -= l.getContainerSize();
        }
        return 0;
    }

    @Override public int getContainerSize() { return size; }

    @Override
    public boolean isEmpty() {
        for (Container l : loads) if (!l.isEmpty()) return false;
        return true;
    }

    @Override public ItemStack getItem(int slot) { Container l = part(slot); return l == null ? ItemStack.EMPTY : l.getItem(local(slot)); }
    @Override public ItemStack removeItem(int slot, int n) { Container l = part(slot); return l == null ? ItemStack.EMPTY : l.removeItem(local(slot), n); }
    @Override public ItemStack removeItemNoUpdate(int slot) { Container l = part(slot); return l == null ? ItemStack.EMPTY : l.removeItemNoUpdate(local(slot)); }
    @Override public void setItem(int slot, ItemStack stack) { Container l = part(slot); if (l != null) l.setItem(local(slot), stack); }
    @Override public void setChanged() { for (Container l : loads) l.setChanged(); }
    @Override public boolean stillValid(Player p) { return true; }
    @Override public void clearContent() { for (Container l : loads) l.clearContent(); }
}
