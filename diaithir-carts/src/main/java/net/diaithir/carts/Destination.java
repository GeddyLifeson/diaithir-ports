package net.diaithir.carts;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Cart labels. A cart carries a destination label: 0 for none, or 1 to 16 for the dye colour it was labelled with (colour id + 1). A destination rail
 * has a channel of the same kind, plus 0 for "any cart". Clean-room: the label is a plain attachment on the minecart.
 */
public final class Destination {
    private Destination() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirCarts.ID);
    private static final MapCodec<Integer> LABEL_CODEC = Codec.INT.fieldOf("label");
    public static final Supplier<AttachmentType<Integer>> LABEL = ATTACHMENTS.register("label", () -> AttachmentType.builder(() -> 0).serialize(LABEL_CODEC).build());

    public static int labelOf(AbstractMinecart cart) {
        return cart.getData(LABEL);
    }

    public static void setLabel(AbstractMinecart cart, int label) {
        if (label <= 0) cart.removeData(LABEL);
        else cart.setData(LABEL, label);
    }

    /** True if a cart with this label trips a rail with this channel. Channel 0 trips for every cart. */
    public static boolean matches(int channel, int label) {
        return channel == 0 || channel == label;
    }

    public static String describe(int code) {
        return code <= 0 ? "none" : DyeColor.byId(code - 1).getName().replace('_', ' ');
    }
}
