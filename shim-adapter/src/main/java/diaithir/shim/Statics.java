package diaithir.shim;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.world.level.block.Block;

/** Static helpers that upstream Create declares on interfaces/classes the port restructured; remap redirects (tools/shim/redirects.json) point here. */
public class Statics {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("diaithir-shim");
    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> mountedItemStorage(RegistryEntry<MountedItemStorageType<?>, ? extends MountedItemStorageType<?>> type) {
        return b -> b.onRegisterAfter(com.zurrtum.create.api.registry.CreateRegistryKeys.MOUNTED_ITEM_STORAGE_TYPE, block -> { var t = type.get(); if (t != null) MountedItemStorageType.REGISTRY.register(block, t); else LOGGER.warn("mounted item storage type {} not registered; vault association skipped for {}", type.getId(), block); });
    }
    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> mountedFluidStorage(RegistryEntry<MountedFluidStorageType<?>, ? extends MountedFluidStorageType<?>> type) {
        return b -> b.onRegisterAfter(com.zurrtum.create.api.registry.CreateRegistryKeys.MOUNTED_FLUID_STORAGE_TYPE, block -> { var t = type.get(); if (t != null) MountedFluidStorageType.REGISTRY.register(block, t); else LOGGER.warn("mounted fluid storage type {} not registered; association skipped for {}", type.getId(), block); });
    }
}
