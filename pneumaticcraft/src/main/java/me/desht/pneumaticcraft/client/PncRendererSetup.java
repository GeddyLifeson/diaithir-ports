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
    public static void registerBlockStateModels(net.neoforged.neoforge.client.event.RegisterBlockStateModels event) {
        event.registerModel(me.desht.pneumaticcraft.api.PneumaticRegistry.RL("pressure_tube"),
                me.desht.pneumaticcraft.client.model.custom.PressureTubeBlockModel.Unbaked.MAP_CODEC);
    }

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
        event.registerLayerDefinition(PNCModelLayers.AIR_GRATE_MODULE, me.desht.pneumaticcraft.client.render.tube_module.AirGrateRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.CHARGING_MODULE, me.desht.pneumaticcraft.client.render.tube_module.ChargingRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.FLOW_DETECTOR_MODULE, me.desht.pneumaticcraft.client.render.tube_module.FlowDetectorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.LOGISTICS_MODULE, me.desht.pneumaticcraft.client.render.tube_module.LogisticsRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.PRESSURE_GAUGE_MODULE, me.desht.pneumaticcraft.client.render.tube_module.PressureGaugeRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.REDSTONE_MODULE, me.desht.pneumaticcraft.client.render.tube_module.RedstoneRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.REGULATOR_MODULE, me.desht.pneumaticcraft.client.render.tube_module.RegulatorRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.SAFETY_VALVE_MODULE, me.desht.pneumaticcraft.client.render.tube_module.SafetyValveRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.VACUUM_MODULE, me.desht.pneumaticcraft.client.render.tube_module.VacuumRenderer::createBodyLayer);
        event.registerLayerDefinition(PNCModelLayers.THERMOSTAT_MODULE, me.desht.pneumaticcraft.client.render.tube_module.ThermostatRenderer::createBodyLayer);
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
        event.registerBlockEntityRenderer(ModBlockEntityTypes.PRESSURE_TUBE.get(), me.desht.pneumaticcraft.client.render.blockentity.PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.ADVANCED_PRESSURE_TUBE.get(), me.desht.pneumaticcraft.client.render.blockentity.PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.REINFORCED_PRESSURE_TUBE.get(), me.desht.pneumaticcraft.client.render.blockentity.PressureTubeModuleRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.SENTRY_TURRET.get(), me.desht.pneumaticcraft.client.render.blockentity.SentryTurretRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntityTypes.AERIAL_INTERFACE.get(), me.desht.pneumaticcraft.client.render.blockentity.AerialInterfaceRenderer::new);
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

    @SubscribeEvent
    public static void wrapCamouflageModels(net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult event) {
        var map = event.getBakingResult().blockStateModels();
        for (var e : new java.util.ArrayList<>(map.entrySet())) {
            if (e.getKey().getBlock() instanceof me.desht.pneumaticcraft.common.block.AbstractCamouflageBlock) {
                map.put(e.getKey(), new me.desht.pneumaticcraft.client.model.custom.CamoBlockStateModel(e.getValue()));
            }
        }
    }

    @SubscribeEvent
    public static void registerSpecialRenderers(net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent event) {
        event.register(me.desht.pneumaticcraft.api.PneumaticRegistry.RL("minigun"), me.desht.pneumaticcraft.client.render.MinigunSpecialRenderer.Unbaked.MAP_CODEC);
    }

    public record DrillBitProperty() implements net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty {
        public static final com.mojang.serialization.MapCodec<DrillBitProperty> MAP_CODEC = com.mojang.serialization.MapCodec.unit(new DrillBitProperty());

        @Override
        public float get(net.minecraft.world.item.ItemStack stack, net.minecraft.client.multiplayer.ClientLevel level, net.minecraft.world.entity.ItemOwner owner, int seed) {
            if (me.desht.pneumaticcraft.common.item.JackHammerItem.getDrillBit(stack) == me.desht.pneumaticcraft.common.item.DrillBitItem.DrillBitType.NONE) return 0f;
            if (level == null || !(owner != null && owner.asLivingEntity() instanceof net.minecraft.world.entity.player.Player player)) return 0.99f;
            long l = me.desht.pneumaticcraft.client.sound.MovingSoundJackhammer.lastJackHammerTime(player);
            if (l <= 20) return net.minecraft.util.Mth.sin((level.getGameTime() % 4 / 4f) * 3.141529f);
            return 0.99f;
        }

        @Override
        public com.mojang.serialization.MapCodec<DrillBitProperty> type() {
            return MAP_CODEC;
        }
    }

    @SubscribeEvent
    public static void registerItemProperties(net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(me.desht.pneumaticcraft.api.PneumaticRegistry.RL("drill_bit"), DrillBitProperty.MAP_CODEC);
    }

    public static final net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions PARTICLE_HANDLER = new net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions() {
        @Override
        public boolean addDestroyEffects(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.client.particle.ParticleEngine manager) {
            // like vanilla, but without forcing at least two particles per voxel axis (avoids huge clouds for thin shapes)
            if (!(level instanceof net.minecraft.client.multiplayer.ClientLevel cl)) return false;
            state.getShape(level, pos).forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double xDif = Math.min(1, maxX - minX);
                double yDif = Math.min(1, maxY - minY);
                double zDif = Math.min(1, maxZ - minZ);
                int xCount = net.minecraft.util.Mth.ceil(xDif / 0.25);
                int yCount = net.minecraft.util.Mth.ceil(yDif / 0.25);
                int zCount = net.minecraft.util.Mth.ceil(zDif / 0.25);
                if (xCount > 0 && yCount > 0 && zCount > 0) {
                    for (int x = 0; x < xCount; x++) {
                        for (int y = 0; y < yCount; y++) {
                            for (int z = 0; z < zCount; z++) {
                                double d4 = (x + 0.5) / xCount;
                                double d5 = (y + 0.5) / yCount;
                                double d6 = (z + 0.5) / zCount;
                                manager.add(new net.minecraft.client.particle.TerrainParticle(cl, pos.getX() + d4 * xDif + minX,
                                        pos.getY() + d5 * yDif + minY, pos.getZ() + d6 * zDif + minZ, d4 - 0.5, d5 - 0.5, d6 - 0.5, state, pos));
                            }
                        }
                    }
                }
            });
            return true;
        }
    };

    @SubscribeEvent
    public static void registerClientExtensions(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        net.minecraft.world.level.block.Block[] toAdd = me.desht.pneumaticcraft.common.registry.ModBlocks.BLOCKS.getEntries().stream()
                .filter(h -> h.get() instanceof me.desht.pneumaticcraft.common.block.AbstractPneumaticCraftBlock)
                .map(h -> (net.minecraft.world.level.block.Block) h.get())
                .toArray(net.minecraft.world.level.block.Block[]::new);
        event.registerBlock(PARTICLE_HANDLER, toAdd);
    }

    @SubscribeEvent
    public static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            net.minecraft.client.renderer.entity.player.AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer> r = event.getPlayerRenderer(skin);
            if (r != null) {
                r.addLayer(new me.desht.pneumaticcraft.client.render.pneumatic_armor.PneumaticElytraLayer(r, event.getEntityModels(), event.getContext().getEquipmentRenderer()));
            }
        }
    }
}
