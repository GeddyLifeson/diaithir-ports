package sonar.fluxnetworks.register;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import sonar.fluxnetworks.common.util.LegacyEnergyHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import sonar.fluxnetworks.api.FluxCapabilities;
import sonar.fluxnetworks.common.device.*;

import java.util.Set;

public class RegistryBlockEntityTypes {
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFluxPlug>> FLUX_PLUG = holder(RegistryBlocks.FLUX_PLUG_KEY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFluxPoint>> FLUX_POINT = holder(RegistryBlocks.FLUX_POINT_KEY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFluxController>> FLUX_CONTROLLER = holder(RegistryBlocks.FLUX_CONTROLLER_KEY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFluxStorage.Basic>> BASIC_FLUX_STORAGE = holder(RegistryBlocks.BASIC_FLUX_STORAGE_KEY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFluxStorage.Herculean>> HERCULEAN_FLUX_STORAGE = holder(RegistryBlocks.HERCULEAN_FLUX_STORAGE_KEY);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileFluxStorage.Gargantuan>> GARGANTUAN_FLUX_STORAGE = holder(RegistryBlocks.GARGANTUAN_FLUX_STORAGE_KEY);

    static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder(Identifier location) {
        return DeferredHolder.create(BuiltInRegistries.BLOCK_ENTITY_TYPE.key(), location);
    }

    static void register(RegisterEvent.RegisterHelper<BlockEntityType<?>> helper) {
        helper.register(RegistryBlocks.FLUX_PLUG_KEY, new BlockEntityType<>(TileFluxPlug::new, Set.of(RegistryBlocks.FLUX_PLUG.get())));
        helper.register(RegistryBlocks.FLUX_POINT_KEY, new BlockEntityType<>(TileFluxPoint::new, Set.of(RegistryBlocks.FLUX_POINT.get())));
        helper.register(RegistryBlocks.FLUX_CONTROLLER_KEY, new BlockEntityType<>(TileFluxController::new, Set.of(RegistryBlocks.FLUX_CONTROLLER.get())));
        helper.register(RegistryBlocks.BASIC_FLUX_STORAGE_KEY, new BlockEntityType<>(TileFluxStorage.Basic::new, Set.of(RegistryBlocks.BASIC_FLUX_STORAGE.get())));
        helper.register(RegistryBlocks.HERCULEAN_FLUX_STORAGE_KEY, new BlockEntityType<>(TileFluxStorage.Herculean::new, Set.of(RegistryBlocks.HERCULEAN_FLUX_STORAGE.get())));
        helper.register(RegistryBlocks.GARGANTUAN_FLUX_STORAGE_KEY, new BlockEntityType<>(TileFluxStorage.Gargantuan::new, Set.of(RegistryBlocks.GARGANTUAN_FLUX_STORAGE.get())));
    }

    static void registerBlockCapabilities(RegisterCapabilitiesEvent event) {
        registerEnergyCapabilities(event, FLUX_PLUG.get());
        registerEnergyCapabilities(event, FLUX_POINT.get());
    }

    static void registerEnergyCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<? extends TileFluxConnector> blockEntityType) {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, blockEntityType, (blockEntity, side) -> {
            IEnergyStorage storage = (IEnergyStorage) blockEntity.getEnergyCapability(FluxCapabilities.BLOCK, side);
            return storage == null ? null : new LegacyEnergyHandler(storage);
        });
        event.registerBlockEntity(FluxCapabilities.BLOCK, blockEntityType, (blockEntity, side) -> blockEntity.getEnergyCapability(FluxCapabilities.BLOCK, side));
    }

    private RegistryBlockEntityTypes() {}
}
