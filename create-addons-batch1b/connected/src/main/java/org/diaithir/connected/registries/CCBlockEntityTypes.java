package org.diaithir.connected.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.diaithir.connected.CreateConnected;
import org.diaithir.connected.content.brake.BrakeBlockEntity;
import org.diaithir.connected.content.brassgearbox.BrassGearboxBlockEntity;
import org.diaithir.connected.content.invertedclutch.InvertedClutchBlockEntity;
import org.diaithir.connected.content.invertedgearshift.InvertedGearshiftBlockEntity;
import org.diaithir.connected.content.parallelgearbox.ParallelGearboxBlockEntity;
import org.diaithir.connected.content.sixwaygearbox.SixWayGearboxBlockEntity;

import org.diaithir.connected.content.centrifugalclutch.CentrifugalClutchBlockEntity;
import org.diaithir.connected.content.freewheelclutch.FreewheelClutchBlockEntity;
import org.diaithir.connected.content.overstressclutch.OverstressClutchBlockEntity;

import java.util.Set;

public class CCBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateConnected.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ParallelGearboxBlockEntity>> PARALLEL_GEARBOX =
        TYPES.register("parallel_gearbox", () -> new BlockEntityType<>((pos, state) -> new ParallelGearboxBlockEntity(CCBlockEntityTypes.PARALLEL_GEARBOX.get(), pos, state), Set.of(CCBlocks.PARALLEL_GEARBOX.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SixWayGearboxBlockEntity>> SIX_WAY_GEARBOX =
        TYPES.register("six_way_gearbox", () -> new BlockEntityType<>((pos, state) -> new SixWayGearboxBlockEntity(CCBlockEntityTypes.SIX_WAY_GEARBOX.get(), pos, state), Set.of(CCBlocks.SIX_WAY_GEARBOX.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrassGearboxBlockEntity>> BRASS_GEARBOX =
        TYPES.register("brass_gearbox", () -> new BlockEntityType<>((pos, state) -> new BrassGearboxBlockEntity(CCBlockEntityTypes.BRASS_GEARBOX.get(), pos, state), Set.of(CCBlocks.BRASS_GEARBOX.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InvertedClutchBlockEntity>> INVERTED_CLUTCH =
        TYPES.register("inverted_clutch", () -> new BlockEntityType<>((pos, state) -> new InvertedClutchBlockEntity(CCBlockEntityTypes.INVERTED_CLUTCH.get(), pos, state), Set.of(CCBlocks.INVERTED_CLUTCH.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InvertedGearshiftBlockEntity>> INVERTED_GEARSHIFT =
        TYPES.register("inverted_gearshift", () -> new BlockEntityType<>((pos, state) -> new InvertedGearshiftBlockEntity(CCBlockEntityTypes.INVERTED_GEARSHIFT.get(), pos, state), Set.of(CCBlocks.INVERTED_GEARSHIFT.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BrakeBlockEntity>> BRAKE =
        TYPES.register("brake", () -> new BlockEntityType<>((pos, state) -> new BrakeBlockEntity(CCBlockEntityTypes.BRAKE.get(), pos, state), Set.of(CCBlocks.BRAKE.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OverstressClutchBlockEntity>> OVERSTRESS_CLUTCH =
        TYPES.register("overstress_clutch", () -> new BlockEntityType<>((pos, state) -> new OverstressClutchBlockEntity(CCBlockEntityTypes.OVERSTRESS_CLUTCH.get(), pos, state), Set.of(CCBlocks.OVERSTRESS_CLUTCH.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FreewheelClutchBlockEntity>> FREEWHEEL_CLUTCH =
        TYPES.register("freewheel_clutch", () -> new BlockEntityType<>((pos, state) -> new FreewheelClutchBlockEntity(CCBlockEntityTypes.FREEWHEEL_CLUTCH.get(), pos, state), Set.of(CCBlocks.FREEWHEEL_CLUTCH.get())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CentrifugalClutchBlockEntity>> CENTRIFUGAL_CLUTCH =
        TYPES.register("centrifugal_clutch", () -> new BlockEntityType<>((pos, state) -> new CentrifugalClutchBlockEntity(CCBlockEntityTypes.CENTRIFUGAL_CLUTCH.get(), pos, state), Set.of(CCBlocks.CENTRIFUGAL_CLUTCH.get())));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<org.diaithir.connected.content.shearpin.ShearPinBlockEntity>> SHEAR_PIN =
        TYPES.register("shear_pin", () -> new BlockEntityType<>((pos, state) -> new org.diaithir.connected.content.shearpin.ShearPinBlockEntity(CCBlockEntityTypes.SHEAR_PIN.get(), pos, state), Set.of(CCBlocks.SHEAR_PIN.get())));
}
