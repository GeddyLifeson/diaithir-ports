package me.desht.pneumaticcraft.client;

import me.desht.pneumaticcraft.api.lib.Names;
import me.desht.pneumaticcraft.common.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client-side registration for the 26.1 port.
 */
@EventBusSubscriber(modid = Names.MOD_ID, value = Dist.CLIENT)
public class PncClientSetup {
    public static void registerScreensOld(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.ADVANCED_AIR_COMPRESSOR.get(), me.desht.pneumaticcraft.client.gui.AdvancedAirCompressorScreen::new);
        event.register(ModMenuTypes.ADVANCED_LIQUID_COMPRESSOR.get(), me.desht.pneumaticcraft.client.gui.AdvancedLiquidCompressorScreen::new);
        event.register(ModMenuTypes.AERIAL_INTERFACE.get(), me.desht.pneumaticcraft.client.gui.AerialInterfaceScreen::new);
        event.register(ModMenuTypes.AIR_CANNON.get(), me.desht.pneumaticcraft.client.gui.AirCannonScreen::new);
        event.register(ModMenuTypes.AIR_COMPRESSOR.get(), me.desht.pneumaticcraft.client.gui.AirCompressorScreen::new);
        event.register(ModMenuTypes.ASSEMBLY_CONTROLLER.get(), me.desht.pneumaticcraft.client.gui.AssemblyControllerScreen::new);
        event.register(ModMenuTypes.CHARGING_STATION.get(), me.desht.pneumaticcraft.client.gui.ChargingStationScreen::new);
        event.register(ModMenuTypes.CREATIVE_COMPRESSOR.get(), me.desht.pneumaticcraft.client.gui.CreativeCompressorScreen::new);
        event.register(ModMenuTypes.CREATIVE_COMPRESSED_IRON_BLOCK.get(), me.desht.pneumaticcraft.client.gui.CreativeCompressedIronBlockScreen::new);
        event.register(ModMenuTypes.ELECTROSTATIC_COMPRESSOR.get(), me.desht.pneumaticcraft.client.gui.ElectrostaticCompressorScreen::new);
        event.register(ModMenuTypes.ELEVATOR.get(), me.desht.pneumaticcraft.client.gui.ElevatorScreen::new);
        event.register(ModMenuTypes.ETCHING_TANK.get(), me.desht.pneumaticcraft.client.gui.EtchingTankScreen::new);
        event.register(ModMenuTypes.FLUID_TANK.get(), me.desht.pneumaticcraft.client.gui.FluidTankScreen::new);
        event.register(ModMenuTypes.FLUID_MIXER.get(), me.desht.pneumaticcraft.client.gui.FluidMixerScreen::new);
        event.register(ModMenuTypes.FLUX_COMPRESSOR.get(), me.desht.pneumaticcraft.client.gui.FluxCompressorScreen::new);
        event.register(ModMenuTypes.GAS_LIFT.get(), me.desht.pneumaticcraft.client.gui.GasLiftScreen::new);
        event.register(ModMenuTypes.INVENTORY_SEARCHER.get(), me.desht.pneumaticcraft.client.gui.InventorySearcherScreen::new);
        event.register(ModMenuTypes.JACKHAMMER_SETUP.get(), me.desht.pneumaticcraft.client.gui.JackHammerSetupScreen::new);
        event.register(ModMenuTypes.KEROSENE_LAMP.get(), me.desht.pneumaticcraft.client.gui.KeroseneLampScreen::new);
        event.register(ModMenuTypes.LIQUID_COMPRESSOR.get(), me.desht.pneumaticcraft.client.gui.LiquidCompressorScreen::new);
        event.register(ModMenuTypes.LIQUID_HOPPER.get(), me.desht.pneumaticcraft.client.gui.LiquidHopperScreen::new);
        event.register(ModMenuTypes.MINIGUN_MAGAZINE.get(), me.desht.pneumaticcraft.client.gui.MinigunMagazineScreen::new);
        event.register(ModMenuTypes.OMNIDIRECTIONAL_HOPPER.get(), me.desht.pneumaticcraft.client.gui.OmnidirectionalHopperScreen::new);
        event.register(ModMenuTypes.PNEUMATIC_DOOR_BASE.get(), me.desht.pneumaticcraft.client.gui.PneumaticDoorBaseScreen::new);
        event.register(ModMenuTypes.PNEUMATIC_DYNAMO.get(), me.desht.pneumaticcraft.client.gui.PneumaticDynamoScreen::new);
        event.register(ModMenuTypes.PRESSURE_CHAMBER_VALVE.get(), me.desht.pneumaticcraft.client.gui.PressureChamberScreen::new);
        event.register(ModMenuTypes.PRESSURE_CHAMBER_INTERFACE.get(), me.desht.pneumaticcraft.client.gui.PressureChamberInterfaceScreen::new);
        event.register(ModMenuTypes.PRESSURIZED_SPAWNER.get(), me.desht.pneumaticcraft.client.gui.PressurizedSpawnerScreen::new);
        event.register(ModMenuTypes.PROGRAMMABLE_CONTROLLER.get(), me.desht.pneumaticcraft.client.gui.ProgrammableControllerScreen::new);
        event.register(ModMenuTypes.REFINERY.get(), me.desht.pneumaticcraft.client.gui.RefineryControllerScreen::new);
        event.register(ModMenuTypes.REINFORCED_CHEST.get(), me.desht.pneumaticcraft.client.gui.ReinforcedChestScreen::new);
        event.register(ModMenuTypes.ITEM_SEARCHER.get(), me.desht.pneumaticcraft.client.gui.ItemSearcherScreen::new);
        event.register(ModMenuTypes.SECURITY_STATION_MAIN.get(), me.desht.pneumaticcraft.client.gui.SecurityStationInventoryScreen::new);
        event.register(ModMenuTypes.SENTRY_TURRET.get(), me.desht.pneumaticcraft.client.gui.SentryTurretScreen::new);
        event.register(ModMenuTypes.SMART_CHEST.get(), me.desht.pneumaticcraft.client.gui.SmartChestScreen::new);
        event.register(ModMenuTypes.SOLAR_COMPRESSOR.get(), me.desht.pneumaticcraft.client.gui.SolarCompressorScreen::new);
        event.register(ModMenuTypes.SPAWNER_EXTRACTOR.get(), me.desht.pneumaticcraft.client.gui.SpawnerExtractorScreen::new);
        event.register(ModMenuTypes.TAG_MATCHER.get(), me.desht.pneumaticcraft.client.gui.TagWorkbenchScreen::new);
        event.register(ModMenuTypes.THERMAL_COMPRESSOR.get(), me.desht.pneumaticcraft.client.gui.ThermalCompressorScreen::new);
        event.register(ModMenuTypes.THERMOPNEUMATIC_PROCESSING_PLANT.get(), me.desht.pneumaticcraft.client.gui.ThermoPlantScreen::new);
        event.register(ModMenuTypes.UNIVERSAL_SENSOR.get(), me.desht.pneumaticcraft.client.gui.UniversalSensorScreen::new);
        event.register(ModMenuTypes.UV_LIGHT_BOX.get(), me.desht.pneumaticcraft.client.gui.UVLightBoxScreen::new);
        event.register(ModMenuTypes.VACUUM_PUMP.get(), me.desht.pneumaticcraft.client.gui.VacuumPumpScreen::new);
        event.register(ModMenuTypes.VACUUM_TRAP.get(), me.desht.pneumaticcraft.client.gui.VacuumTrapScreen::new);
    }
}
