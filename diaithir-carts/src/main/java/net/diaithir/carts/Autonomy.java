package net.diaithir.carts;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Crewed or autonomous. Autonomy is never automatic: a vehicle runs without crew only when its type is capable (it is in the entity type tag
 * diaithircarts:autonomy_capable, which other vehicle mods add their types to) and it has an autonomy module fitted (the item, Age VII, used on the vehicle).
 * Without both, a scheduled vehicle runs only while a living rider (player, villager, NPC) is aboard. The wages thread reads {@link #autonomousCrew}
 * and retires the crew of a vehicle for which it is true.
 */
public final class Autonomy {
    private Autonomy() {}

    public enum Status { UNCREWED, CREWED, AUTONOMOUS }

    public static final TagKey<EntityType<?>> CAPABLE = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DiaithirCarts.ID, "autonomy_capable"));

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DiaithirCarts.ID);
    public static final Supplier<AttachmentType<Boolean>> MODULE = ATTACHMENTS.register("autonomy_module", () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL.fieldOf("fitted")).build());

    /** Crew jobs that an autonomy module replaces. */
    public static final java.util.Set<String> AUTOMATED_JOBS = java.util.Set.of("driver", "train_driver", "cart_driver", "crane_operator", "machine_operator");

    public static boolean capable(Entity vehicle) {
        return vehicle.getType().builtInRegistryHolder().is(CAPABLE);
    }

    public static boolean hasModule(Entity vehicle) {
        return vehicle.hasData(MODULE) && vehicle.getData(MODULE);
    }

    /** True when the vehicle is capable and has a module fitted. */
    public static boolean autonomous(Entity vehicle) {
        return capable(vehicle) && hasModule(vehicle);
    }

    /** Fits a module. False when the type is not capable or one is already fitted. */
    public static boolean fit(Entity vehicle) {
        if (!capable(vehicle) || hasModule(vehicle)) return false;
        vehicle.setData(MODULE, true);
        return true;
    }

    /** The predicate behind global.AUTONOMOUS_CREW(job, vehicle): true when this job on this vehicle is automated, so the crew can be retired. */
    public static boolean autonomousCrew(String job, Entity vehicle) {
        return AUTOMATED_JOBS.contains(job) && autonomous(vehicle);
    }

    public static Status status(Entity vehicle) {
        if (autonomous(vehicle)) return Status.AUTONOMOUS;
        return crewOnBoard(vehicle) >= crewRequired(vehicle) ? Status.CREWED : Status.UNCREWED;
    }

    /** Crew the vehicle needs: its rolling stock's crew count (steam locomotive 2, driver and fireman), at least 1. */
    public static int crewRequired(Entity vehicle) {
        return vehicle instanceof net.minecraft.world.entity.vehicle.minecart.AbstractMinecart c ? Math.max(1, c.getData(GaugeData.CREW)) : 1;
    }

    /** Living riders aboard (spectators excluded). The wages thread pays each of them while the vehicle is CREWED, up to {@link #crewRequired}. */
    public static int crewOnBoard(Entity vehicle) {
        int n = 0;
        for (Entity rider : vehicle.getPassengers()) if (rider instanceof LivingEntity && !(rider instanceof Player p && p.isSpectator())) n++;
        return n;
    }

    /** True when the vehicle may run its schedule now. */
    public static boolean mayRun(Entity vehicle) {
        return status(vehicle) != Status.UNCREWED;
    }
}
