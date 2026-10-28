package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.common.registry.ModBlockEntityTypes;
import me.desht.pneumaticcraft.common.registry.ModEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Block entity and entity renderer registration for the 26.1 port. */
@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
public class PncRendererSetup {
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PNCModelLayers.AIR_CANNON, me.desht.pneumaticcraft.client.render.blockentity.AirCannonRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.PNEUMATIC_DOOR, me.desht.pneumaticcraft.client.render.blockentity.PneumaticDoorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.PNEUMATIC_DOOR_BASE, me.desht.pneumaticcraft.client.render.blockentity.PneumaticDoorBaseRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_CONTROLLER, me.desht.pneumaticcraft.client.render.blockentity.AssemblyControllerRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_DRILL, me.desht.pneumaticcraft.client.render.blockentity.AssemblyDrillRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_LASER, me.desht.pneumaticcraft.client.render.blockentity.AssemblyLaserRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_IO_UNIT, me.desht.pneumaticcraft.client.render.blockentity.AssemblyIOUnitRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ASSEMBLY_PLATFORM, me.desht.pneumaticcraft.client.render.blockentity.AssemblyPlatformRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.ELEVATOR_BASE, me.desht.pneumaticcraft.client.render.blockentity.ElevatorBaseRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.MANUAL_COMPRESSOR, me.desht.pneumaticcraft.client.render.blockentity.ManualCompressorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.PRESSURE_CHAMBER_INTERFACE, me.desht.pneumaticcraft.client.render.blockentity.PressureChamberInterfaceRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SPAWNER_EXTRACTOR, me.desht.pneumaticcraft.client.render.blockentity.SpawnerExtractorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.UNIVERSAL_SENSOR, me.desht.pneumaticcraft.client.render.blockentity.UniversalSensorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.VACUUM_PUMP, me.desht.pneumaticcraft.client.render.blockentity.VacuumPumpRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SOLAR_COMPRESSOR, me.desht.pneumaticcraft.client.render.blockentity.SolarCompressorRenderer::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerEntityLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PNCModelLayers.HEAT_FRAME, me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelHeatFrame::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.LOGISTICS_FRAME, me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelLogisticsFrame::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SPAWNER_AGITATOR, me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelSpawnerAgitator::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.TRANSFER_GADGET, me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelTransferGadget::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.CROP_SUPPORT, me.desht.pneumaticcraft.client.model.entity.semiblocks.ModelCropSupport::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.DRONE, me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.DRONE_CORE, me.desht.pneumaticcraft.client.model.entity.drone.ModelDroneCore::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.MINIGUN, me.desht.pneumaticcraft.client.model.ModelMinigun::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.DRONE.get(), me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone::standard);
        event.registerEntityRenderer(ModEntityTypes.AMADRONE.get(), me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone::amadrone);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_DRONE.get(), me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone::logistics);
        event.registerEntityRenderer(ModEntityTypes.HARVESTING_DRONE.get(), me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone::harvesting);
        event.registerEntityRenderer(ModEntityTypes.GUARD_DRONE.get(), me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone::guard);
        event.registerEntityRenderer(ModEntityTypes.COLLECTOR_DRONE.get(), me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone::collector);
        event.registerEntityRenderer(ModEntityTypes.PROGRAMMABLE_CONTROLLER.get(), me.desht.pneumaticcraft.client.render.entity.drone.RenderDrone::programmableController);
        event.registerEntityRenderer(ModEntityTypes.CROP_SUPPORT.get(), me.desht.pneumaticcraft.client.render.entity.semiblock.RenderCropSupport::new);
        event.registerEntityRenderer(ModEntityTypes.SPAWNER_AGITATOR.get(), me.desht.pneumaticcraft.client.render.entity.semiblock.RenderSpawnerAgitator::new);
        event.registerEntityRenderer(ModEntityTypes.HEAT_FRAME.get(), me.desht.pneumaticcraft.client.render.entity.semiblock.RenderHeatFrame::new);
        event.registerEntityRenderer(ModEntityTypes.TRANSFER_GADGET.get(), me.desht.pneumaticcraft.client.render.entity.semiblock.RenderTransferGadget::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_ACTIVE_PROVIDER.get(), me.desht.pneumaticcraft.client.render.entity.semiblock.RenderLogisticsFrame::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_PASSIVE_PROVIDER.get(), me.desht.pneumaticcraft.client.render.entity.semiblock.RenderLogisticsFrame::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_STORAGE.get(), me.desht.pneumaticcraft.client.render.entity.semiblock.RenderLogisticsFrame::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_DEFAULT_STORAGE.get(), me.desht.pneumaticcraft.client.render.entity.semiblock.RenderLogisticsFrame::new);
        event.registerEntityRenderer(ModEntityTypes.LOGISTICS_FRAME_REQUESTER.get(), me.desht.pneumaticcraft.client.render.entity.semiblock.RenderLogisticsFrame::new);
        event.registerEntityRenderer(ModEntityTypes.VORTEX.get(), me.desht.pneumaticcraft.client.render.entity.RenderEntityVortex::new);
        event.registerEntityRenderer(ModEntityTypes.RING.get(), me.desht.pneumaticcraft.client.render.entity.RenderEntityRing::new);
        event.registerEntityRenderer(ModEntityTypes.MICROMISSILE.get(), me.desht.pneumaticcraft.client.render.entity.RenderMicromissile::new);
        event.registerEntityRenderer(ModEntityTypes.TUMBLING_BLOCK.get(), me.desht.pneumaticcraft.client.render.entity.RenderTumblingBlock::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ADVANCED_LIQUID_COMPRESSOR.get(), me.desht.pneumaticcraft.client.render.fluid.RenderAdvancedLiquidCompressor::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.AIR_CANNON.get(), me.desht.pneumaticcraft.client.render.blockentity.AirCannonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.APHORISM_TILE.get(), me.desht.pneumaticcraft.client.render.blockentity.AphorismTileRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_CONTROLLER.get(), me.desht.pneumaticcraft.client.render.blockentity.AssemblyControllerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_DRILL.get(), me.desht.pneumaticcraft.client.render.blockentity.AssemblyDrillRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_IO_UNIT.get(), me.desht.pneumaticcraft.client.render.blockentity.AssemblyIOUnitRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_LASER.get(), me.desht.pneumaticcraft.client.render.blockentity.AssemblyLaserRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ASSEMBLY_PLATFORM.get(), me.desht.pneumaticcraft.client.render.blockentity.AssemblyPlatformRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.CHARGING_STATION.get(), me.desht.pneumaticcraft.client.render.blockentity.ChargingStationRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.DISPLAY_TABLE.get(), me.desht.pneumaticcraft.client.render.blockentity.DisplayTableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ELEVATOR_BASE.get(), me.desht.pneumaticcraft.client.render.blockentity.ElevatorBaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ELEVATOR_CALLER.get(), me.desht.pneumaticcraft.client.render.blockentity.ElevatorCallerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ETCHING_TANK.get(), me.desht.pneumaticcraft.client.render.fluid.RenderEtchingTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.FLUID_MIXER.get(), me.desht.pneumaticcraft.client.render.fluid.RenderFluidMixer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.KEROSENE_LAMP.get(), me.desht.pneumaticcraft.client.render.fluid.RenderKeroseneLamp::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.GAS_LIFT.get(), me.desht.pneumaticcraft.client.render.fluid.RenderGasLift::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.LIQUID_HOPPER.get(), me.desht.pneumaticcraft.client.render.fluid.RenderLiquidHopper::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.LIQUID_COMPRESSOR.get(), me.desht.pneumaticcraft.client.render.fluid.RenderLiquidCompressor::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.MANUAL_COMPRESSOR.get(), me.desht.pneumaticcraft.client.render.blockentity.ManualCompressorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PNEUMATIC_DOOR.get(), me.desht.pneumaticcraft.client.render.blockentity.PneumaticDoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PNEUMATIC_DOOR_BASE.get(), me.desht.pneumaticcraft.client.render.blockentity.PneumaticDoorBaseRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PRESSURE_CHAMBER_VALVE.get(), me.desht.pneumaticcraft.client.render.blockentity.PressureChamberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PRESSURE_CHAMBER_INTERFACE.get(), me.desht.pneumaticcraft.client.render.blockentity.PressureChamberInterfaceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PROGRAMMER.get(), me.desht.pneumaticcraft.client.render.blockentity.ProgrammerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.REFINERY.get(), me.desht.pneumaticcraft.client.render.fluid.RenderRefineryController::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.REFINERY_OUTPUT.get(), me.desht.pneumaticcraft.client.render.fluid.RenderRefineryOutput::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.REINFORCED_PRESSURE_CHAMBER_VALVE.get(), me.desht.pneumaticcraft.client.render.blockentity.PressureChamberRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SPAWNER_EXTRACTOR.get(), me.desht.pneumaticcraft.client.render.blockentity.SpawnerExtractorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TANK_SMALL.get(), me.desht.pneumaticcraft.client.render.fluid.RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TANK_MEDIUM.get(), me.desht.pneumaticcraft.client.render.fluid.RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TANK_LARGE.get(), me.desht.pneumaticcraft.client.render.fluid.RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TANK_HUGE.get(), me.desht.pneumaticcraft.client.render.fluid.RenderFluidTank::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.TAG_WORKBENCH.get(), me.desht.pneumaticcraft.client.render.blockentity.TagWorkbenchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.THERMOPNEUMATIC_PROCESSING_PLANT.get(), me.desht.pneumaticcraft.client.render.fluid.RenderThermopneumaticProcessingPlant::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.UNIVERSAL_SENSOR.get(), me.desht.pneumaticcraft.client.render.blockentity.UniversalSensorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.VACUUM_PUMP.get(), me.desht.pneumaticcraft.client.render.blockentity.VacuumPumpRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SOLAR_COMPRESSOR.get(), me.desht.pneumaticcraft.client.render.blockentity.SolarCompressorRenderer::new);
    }
}
