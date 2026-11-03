package net.diaithir.carts;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/** The gauge stored on a vehicle cart (kept apart from {@link Gauge} so the gauge rules load without the game). */
public final class GaugeData {
    private GaugeData() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirCarts.ID);
    /** The gauge of a vehicle (a cart spawned from rolling stock). Plain carts have none and run anywhere. */
    public static final Supplier<AttachmentType<String>> TYPE = ATTACHMENTS.register("gauge", () -> AttachmentType.builder(() -> "any").serialize(Codec.STRING.fieldOf("id")).build());

    /** Crew a vehicle needs to run (steam locomotive 2: driver and fireman). 0 means the default of 1. */
    public static final Supplier<AttachmentType<Integer>> CREW = ATTACHMENTS.register("crew", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("n")).build());

    public static Gauge of(AbstractMinecart cart) {
        return Gauge.of(cart.getData(TYPE));
    }

    public static void set(AbstractMinecart cart, Gauge g) {
        cart.setData(TYPE, g.id);
    }
}
