package net.diaithir.worldsys.economy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A player's inventory and chat, for the economy's quests and purse. */
final class ServerPlayerHolder implements EconQuests.Player {
    private final ServerPlayer p;

    ServerPlayerHolder(ServerPlayer p) { this.p = p; }

    private static Item item(String id) { return BuiltInRegistries.ITEM.getValue(Identifier.parse(id)); }

    public String id() { return p.getUUID().toString(); }

    public int count(String id) {
        Item it = item(id);
        int n = 0;
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) { ItemStack st = inv.getItem(i); if (!st.isEmpty() && st.is(it)) n += st.getCount(); }
        return n;
    }

    public void take(String id, int n) {
        Item it = item(id);
        var inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize() && n > 0; i++) {
            ItemStack st = inv.getItem(i);
            if (st.isEmpty() || !st.is(it)) continue;
            int t = Math.min(n, st.getCount());
            st.shrink(t);
            n -= t;
        }
    }

    public void give(String id, int n) {
        Item it = item(id);
        while (n > 0) {
            ItemStack st = new ItemStack(it, Math.min(n, it.getDefaultMaxStackSize()));
            n -= st.getCount();
            if (!p.getInventory().add(st)) p.drop(st, false);
        }
    }

    public void tell(String text) { p.sendSystemMessage(Component.literal(text)); }

    public void command(String command) {
        try { p.level().getServer().getCommands().performPrefixedCommand(p.createCommandSourceStack(), command); } catch (RuntimeException ignored) { }
    }

    public String itemName(String id) { return new ItemStack(item(id)).getHoverName().getString(); }

    public double bottleFactor(String id, int n) {
        var styles = EconWorld.data().drinks;
        if (!styles.containsKey(id.substring(id.indexOf(':') + 1))) return 1;
        try {
            Item it = item(id);
            int left = n;
            double sum = 0;
            var inv = p.getInventory();
            for (int i = 0; i < inv.getContainerSize() && left > 0; i++) {
                ItemStack st = inv.getItem(i);
                if (st.isEmpty() || !st.is(it)) continue;
                var cd = st.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
                var b = EconDrinks.bottleData(cd == null ? "" : cd.copyTag().toString());
                int take = Math.min(left, st.getCount());
                sum += take * EconDrinks.factor(styles, id, b.abv(), b.aged());
                left -= take;
            }
            return n - left > 0 ? (sum + left) / n : 1;
        } catch (RuntimeException e) {
            return 1;
        }
    }
}
