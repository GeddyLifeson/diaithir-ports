package rearth.belts;

import rearth.belts.blocks.ChuteBlockEntity;
import rearth.belts.foundry.FoundryLoaderBlockEntity;
import rearth.belts.foundry.FoundrySplitterBlockEntity;
import rearth.belts.foundry.FoundryTunnelBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntitiesContent {
    
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Belts.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    
    
    public static final RegistrySupplier<BlockEntityType<ChuteBlockEntity>> CHUTE_BLOCK = TYPES.register(
            "chute",
            () -> PlatformBlockEntityTypes.create(ChuteBlockEntity::new, BlockContent.CHUTE_BLOCK.get())
    );
    
    public static final RegistrySupplier<BlockEntityType<FoundrySplitterBlockEntity>> FOUNDRY_SPLITTER = TYPES.register(
            "foundry_splitter",
            () -> PlatformBlockEntityTypes.create(FoundrySplitterBlockEntity::new, BlockContent.FOUNDRY_SPLITTER_BLOCK.get())
    );
    
    public static final RegistrySupplier<BlockEntityType<FoundryTunnelBlockEntity>> FOUNDRY_TUNNEL = TYPES.register(
            "foundry_tunnel",
            () -> PlatformBlockEntityTypes.create(FoundryTunnelBlockEntity::new, BlockContent.FOUNDRY_TUNNEL_BLOCK.get())
    );
    
    public static final RegistrySupplier<BlockEntityType<FoundryLoaderBlockEntity>> FOUNDRY_LOADER = TYPES.register(
            "foundry_loader",
            () -> PlatformBlockEntityTypes.create(FoundryLoaderBlockEntity::new, BlockContent.FOUNDRY_LOADER_BLOCK.get())
    );
    
}
