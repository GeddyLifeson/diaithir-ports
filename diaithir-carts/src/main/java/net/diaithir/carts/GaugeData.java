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

    /** The vehicle's engine class, as the integration matrix names it in diaithir-units ({@code Fuels.engine(id).accepts(fuelId)}): steam_piston, diesel_engine, electric_motor, or arcane_motor once fitted with a mana firebox or cell. Plain ids, so no code dependency. */
    public static final Supplier<AttachmentType<String>> ENGINE = ATTACHMENTS.register("engine", () -> AttachmentType.builder(() -> "").serialize(Codec.STRING.fieldOf("id")).build());

    /** Joules banked on a magic-fitted vehicle (see MagicFuel). */
    public static final Supplier<AttachmentType<Long>> JOULES = ATTACHMENTS.register("joules", () -> AttachmentType.builder(() -> 0L).serialize(Codec.LONG.fieldOf("j")).build());
    /** Rated power of the vehicle in W (locomotives: the roster's power), used to price a fill of fire in joules. */
    public static final Supplier<AttachmentType<Integer>> RATED = ATTACHMENTS.register("rated", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT.fieldOf("w")).build());

    public static Gauge of(AbstractMinecart cart) {
        return Gauge.of(cart.getData(TYPE));
    }

    public static void set(AbstractMinecart cart, Gauge g) {
        cart.setData(TYPE, g.id);
    }
}
