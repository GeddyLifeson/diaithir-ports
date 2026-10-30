package net.diaithir.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/** A ring or charm with a fixed number of rune sockets. Socketed runes live in the diaithir_classes:sockets component. */
public final class SocketItem extends Item {
    private final int sockets;

    public SocketItem(Properties p, int sockets) {
        super(p);
        this.sockets = sockets;
    }

    public int sockets() { return sockets; }

    /** Tries to press a rune into the ring. Returns an error key, or null on success (the rune stack is shrunk). */
    public static String socket(ItemStack ring, ItemStack rune) {
        if (!(ring.getItem() instanceof SocketItem si) || !(rune.getItem() instanceof RuneItem ri)) {
            return "not_applicable";
        }
        List<String> cur = new ArrayList<>(DiaithirClasses.socketsOf(ring));
        if (cur.size() >= si.sockets) {
            return "full";
        }
        if (cur.contains(ri.runeId())) {
            return "duplicate";
        }
        cur.add(ri.runeId());
        ring.set(DiaithirClasses.SOCKETS.get(), List.copyOf(cur));
        rune.shrink(1);
        return null;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess carried) {
        if (action != ClickAction.SECONDARY || !(other.getItem() instanceof RuneItem) || !slot.allowModification(player)) {
            return false;
        }
        String err = socket(self, other);
        if (err != null) {
            player.sendOverlayMessage(Component.translatable("message.diaithir_classes.socket_" + err));
            return true;
        }
        player.playSound(SoundEvents.SMITHING_TABLE_USE, 0.8f, 1.2f);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        List<String> cur = DiaithirClasses.socketsOf(stack);
        out.accept(Component.translatable("tooltip.diaithir_classes.sockets", cur.size(), sockets).withStyle(ChatFormatting.GRAY));
        for (String id : cur) {
            DiaithirClasses.Rune r = DiaithirClasses.RUNES.get(id);
            if (r != null) {
                out.accept(Component.translatable("item.diaithir_classes.rune_" + id).withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(" ")).append(Component.translatable("rune.diaithir_classes." + id + ".effect").withStyle(ChatFormatting.DARK_AQUA)));
            }
        }
        if (cur.size() < sockets) {
            out.accept(Component.translatable("tooltip.diaithir_classes.hint").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
