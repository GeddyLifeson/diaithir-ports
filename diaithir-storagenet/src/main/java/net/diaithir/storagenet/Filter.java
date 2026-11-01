package net.diaithir.storagenet;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.List;

/** A short list of items with a whitelist or blacklist mode. An empty list lets everything through. */
public final class Filter {
    private final List<ItemResource> items = new ArrayList<>();
    private boolean blacklist;

    public boolean allows(ItemResource r) {
        if (items.isEmpty()) return true;
        return items.contains(r) != blacklist;
    }

    /** True if this filter names the item and is a whitelist: the owner of the filter wants that item. */
    public boolean wants(ItemResource r) {
        return !blacklist && items.contains(r);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<ItemResource> items() {
        return items;
    }

    public boolean blacklist() {
        return blacklist;
    }

    public void toggleMode() {
        blacklist = !blacklist;
    }

    public void setBlacklist(boolean b) {
        blacklist = b;
    }

    /** Adds the item if absent and there is room; removes it if already listed. Returns a short message. */
    public String toggle(ItemStack stack) {
        ItemResource r = ItemResource.of(stack);
        if (items.remove(r)) return "Removed " + stack.getHoverName().getString();
        if (items.size() >= Config.FILTER_SLOTS.get()) return "Filter is full";
        items.add(r);
        return "Added " + stack.getHoverName().getString();
    }

    public void clear() {
        items.clear();
    }

    public String describe() {
        StringBuilder sb = new StringBuilder(blacklist ? "blacklist" : "whitelist").append(' ');
        if (items.isEmpty()) return sb.append("(empty, lets everything through)").toString();
        for (int i = 0; i < items.size(); i++) sb.append(i == 0 ? "" : ", ").append(items.get(i).getHoverName().getString());
        return sb.toString();
    }
}
