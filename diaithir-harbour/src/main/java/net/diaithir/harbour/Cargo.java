package net.diaithir.harbour;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Cargo crates on boats. Each crate gives a boat nine more slots (a row of the cargo hold); how many a boat may carry follows the harbour office near it.
 * Crates and contents are attachments on the boat and are dropped if it is destroyed. Clean-room: written from the idea of a hold on a boat only.
 */
public final class Cargo {
    private Cargo() {}

    public static final int PER_CRATE = 9, MAX_CRATES = 6;
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirHarbour.ID);
    private static final MapCodec<Integer> CRATES_CODEC = Codec.INT.fieldOf("crates");
    private static final MapCodec<List<ItemStack>> ITEMS_CODEC = ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items");
    public static final Supplier<AttachmentType<Integer>> CRATES = ATTACHMENTS.register("crates", () -> AttachmentType.builder(() -> 0).serialize(CRATES_CODEC).build());
    public static final Supplier<AttachmentType<List<ItemStack>>> ITEMS = ATTACHMENTS.register("items", () -> AttachmentType.<List<ItemStack>>builder(() -> List.of()).serialize(ITEMS_CODEC).build());

    public static int crates(AbstractBoat boat) {
        return boat.getData(CRATES);
    }

    /** Adds a crate if the boat is under its limit. Returns false when full. */
    public static boolean addCrate(AbstractBoat boat, int limit) {
        int n = crates(boat);
        if (n >= Math.min(limit, MAX_CRATES)) return false;
        boat.setData(CRATES, n + 1);
        return true;
    }

    /** The hold as a container saved back to the boat on every change. */
    public static Container hold(AbstractBoat boat) {
        int size = crates(boat) * PER_CRATE;
        SimpleContainer c = new SimpleContainer(size) {
            @Override
            public void setChanged() {
                super.setChanged();
                List<ItemStack> out = new ArrayList<>();
                for (int i = 0; i < getContainerSize(); i++) out.add(getItem(i).copy());
                boat.setData(ITEMS, out);
            }
        };
        List<ItemStack> saved = boat.getData(ITEMS);
        for (int i = 0; i < Math.min(size, saved.size()); i++) c.setItem(i, saved.get(i).copy());
        return c;
    }

    public static void open(Player player, AbstractBoat boat) {
        int rows = crates(boat);
        if (rows <= 0) {
            player.sendOverlayMessage(Component.literal("No cargo crate on this boat."));
            return;
        }
        MenuType<ChestMenu> type = switch (rows) { case 1 -> MenuType.GENERIC_9x1; case 2 -> MenuType.GENERIC_9x2; case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4; case 5 -> MenuType.GENERIC_9x5; default -> MenuType.GENERIC_9x6; };
        Container hold = hold(boat);
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ChestMenu(type, id, inv, hold, rows), Component.literal("Cargo hold")));
    }

    /** Drops the crates and everything in the hold where the boat was. */
    public static void spill(ServerLevel level, AbstractBoat boat) {
        int n = crates(boat);
        if (n <= 0) return;
        for (ItemStack s : boat.getData(ITEMS)) if (!s.isEmpty()) level.addFreshEntity(new ItemEntity(level, boat.getX(), boat.getY() + 0.5, boat.getZ(), s.copy()));
        level.addFreshEntity(new ItemEntity(level, boat.getX(), boat.getY() + 0.5, boat.getZ(), new ItemStack(DiaithirHarbour.CARGO_CRATE.get(), n)));
        boat.removeData(CRATES);
        boat.removeData(ITEMS);
    }
}
