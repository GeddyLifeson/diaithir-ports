package org.valkyrienskies.eureka.forge.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

// 26.2: ForgeHooks.getBurnTime is gone - burn times are datapack-driven FuelValues, reachable
// through a MinecraftServer or Level. The engine burns fuel server-side, but the fuel-slot
// filters also run in client GUIs, so fall back to the client level off the server.
public class FuelRegistryImpl extends org.valkyrienskies.eureka.registry.FuelRegistry {

    public FuelRegistryImpl() {
        INSTANCE = this;
    }

    @Override
    public int get(ItemStack stack) {
        final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.fuelValues().burnDuration(stack);
        }
        if (FMLEnvironment.getDist().isClient()) {
            return ClientFuelValues.get(stack);
        }
        return 0;
    }

    // Separate holder class so the dedicated server never classloads net.minecraft.client.
    private static final class ClientFuelValues {
        static int get(final ItemStack stack) {
            final Level level = Minecraft.getInstance().level;
            return level == null ? 0 : level.fuelValues().burnDuration(stack);
        }
    }
}
