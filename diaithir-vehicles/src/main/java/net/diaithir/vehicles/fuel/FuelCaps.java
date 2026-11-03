package net.diaithir.vehicles.fuel;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jspecify.annotations.Nullable;

/** The shared fuel capability. Vehicles, ships and aircraft register {@link #ENTITY}; tanks and pumps {@link #BLOCK}; jerry cans {@link #ITEM}. */
public final class FuelCaps {
    private static final Identifier ID = Identifier.fromNamespaceAndPath("diaithir", "fuel_store");
    public static final EntityCapability<FuelStore, @Nullable Void> ENTITY = EntityCapability.createVoid(ID, FuelStore.class);
    public static final BlockCapability<FuelStore, @Nullable Direction> BLOCK = BlockCapability.createSided(ID, FuelStore.class);
    public static final ItemCapability<FuelStore, @Nullable Void> ITEM = ItemCapability.createVoid(ID, FuelStore.class);

    private FuelCaps() {}
}
