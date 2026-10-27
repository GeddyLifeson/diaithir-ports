package com.diaithir.addons.newage;

import com.diaithir.addons.newage.content.electricity.connector.ElectricalConnectorBlockEntity;
import com.diaithir.addons.newage.content.electricity.generation.brushes.CarbonBrushesBlockEntity;
import com.diaithir.addons.newage.content.electricity.generation.coil.GeneratorCoilBlockEntity;
import com.diaithir.addons.newage.content.electricity.light.StreetLightBlockEntity;
import com.diaithir.addons.newage.content.energising.EnergiserBlockEntity;
import com.diaithir.addons.newage.content.heat.heater.HeaterBlockEntity;
import com.diaithir.addons.newage.content.heat.pipe.EncasedHeatPipeBlockEntity;
import com.diaithir.addons.newage.content.heat.pipe.HeatPipeBlockEntity;
import com.diaithir.addons.newage.content.heat.plate.SolarHeatingPlateBlockEntity;
import com.diaithir.addons.newage.content.heat.pump.HeatPumpBlockEntity;
import com.diaithir.addons.newage.content.heat.stirling.StirlingEngineBlockEntity;
import com.diaithir.addons.newage.content.motor.MotorBlockEntity;
import com.diaithir.addons.newage.content.motor.extension.MotorExtensionBlockEntity;
import com.diaithir.addons.newage.content.motor.extension.variants.AdvancedMotorExtensionVariant;
import com.diaithir.addons.newage.content.motor.extension.variants.BasicMotorExtensionVariant;
import com.diaithir.addons.newage.content.motor.variants.AdvancedMotorVariant;
import com.diaithir.addons.newage.content.motor.variants.BasicMotorVariant;
import com.diaithir.addons.newage.content.motor.variants.ReinforcedMotorVariant;
import com.diaithir.addons.newage.content.nuclear.reactor.fuelacceptor.ReactorFuelAcceptorBlockEntity;
import com.diaithir.addons.newage.content.nuclear.reactor.rod.ReactorRodBlockEntity;
import com.diaithir.addons.newage.content.nuclear.reactor.vent.ReactorHeatVentBlockEntity;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CNABlockEntityTypes {
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergiserBlockEntity>> ENERGISER = Reg
            .blockEntity("energiser", EnergiserBlockEntity::new)
            .validBlocks(CNABlocks.BASIC_ENERGISER, CNABlocks.ADVANCED_ENERGISER, CNABlocks.REINFORCED_ENERGISER)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricalConnectorBlockEntity>> ELECTRICAL_CONNECTOR = Reg
            .blockEntity("electrical_connector", ElectricalConnectorBlockEntity::new)
            .validBlocks(CNABlocks.ELECTRICAL_CONNECTOR)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneratorCoilBlockEntity>> GENERATOR_COIL = Reg
            .blockEntity("generator_coil", GeneratorCoilBlockEntity::new)
            .validBlocks(CNABlocks.GENERATOR_COIL)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CarbonBrushesBlockEntity>> CARBON_BRUSHES = Reg
            .blockEntity("carbon_brushes", CarbonBrushesBlockEntity::new)
            .validBlocks(CNABlocks.CARBON_BRUSHES)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatPipeBlockEntity>> HEAT_PIPE = Reg
            .blockEntity("heat_pipe", HeatPipeBlockEntity::new)
            .validBlocks(CNABlocks.HEAT_PIPE)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EncasedHeatPipeBlockEntity>> ENCASED_HEAT_PIPE = Reg
            .blockEntity("encased_heat_pipe", EncasedHeatPipeBlockEntity::new)
            .validBlocks(CNABlocks.ENCASED_HEAT_PIPE, CNABlocks.REACTOR_ENCASED_HEAT_PIPE)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatPumpBlockEntity>> HEAT_PUMP = Reg
            .blockEntity("heat_pump", HeatPumpBlockEntity::new)
            .validBlocks(CNABlocks.HEAT_PUMP)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeaterBlockEntity>> HEATER = Reg
            .blockEntity("heater", HeaterBlockEntity::new)
            .validBlocks(CNABlocks.HEATER)
            .register();


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarHeatingPlateBlockEntity>> BASIC_SOLAR_HEATING_PLATE = Reg
            .blockEntity("basic_solar_heating_plate", SolarHeatingPlateBlockEntity::createBasic)
            .validBlocks(CNABlocks.BASIC_SOLAR_HEATING_PLATE)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarHeatingPlateBlockEntity>> ADVANCED_SOLAR_HEATING_PLATE = Reg
            .blockEntity("advanced_solar_heating_plate", SolarHeatingPlateBlockEntity::createAdvanced)
            .validBlocks(CNABlocks.ADVANCED_SOLAR_HEATING_PLATE)
            .register();


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorRodBlockEntity>> REACTOR_ROD = Reg
            .blockEntity("reactor_rod", ReactorRodBlockEntity::new)
            .validBlocks(CNABlocks.REACTOR_ROD)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorFuelAcceptorBlockEntity>> REACTOR_FUEL_ACCEPTOR = Reg
            .blockEntity("reactor_fuel_acceptor", ReactorFuelAcceptorBlockEntity::new)
            .validBlocks(CNABlocks.REACTOR_FUEL_ACCEPTOR)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorHeatVentBlockEntity>> REACTOR_HEAT_VENT = Reg
            .blockEntity("reactor_heat_vent", ReactorHeatVentBlockEntity::new)
            .validBlocks(CNABlocks.REACTOR_HEAT_VENT)
            .register();


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StirlingEngineBlockEntity>> STIRLING_ENGINE = Reg
            .blockEntity("stirling_engine", StirlingEngineBlockEntity::new)
            .validBlocks(CNABlocks.STIRLING_ENGINE)
            .register();


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MotorBlockEntity>> BASIC_MOTOR = Reg
            .blockEntity("basic_motor", MotorBlockEntity.create(new BasicMotorVariant()))
            .validBlocks(CNABlocks.BASIC_MOTOR)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MotorBlockEntity>> ADVANCED_MOTOR = Reg
            .blockEntity("advanced_motor", MotorBlockEntity.create(new AdvancedMotorVariant()))
            .validBlocks(CNABlocks.ADVANCED_MOTOR)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MotorBlockEntity>> REINFORCED_MOTOR = Reg
            .blockEntity("reinforced_motor", MotorBlockEntity.create(new ReinforcedMotorVariant()))
            .validBlocks(CNABlocks.REINFORCED_MOTOR)
            .register();


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MotorExtensionBlockEntity>> BASIC_MOTOR_EXTENSION = Reg
            .blockEntity("basic_motor_extension", MotorExtensionBlockEntity.create(new BasicMotorExtensionVariant()))
            .validBlocks(CNABlocks.BASIC_MOTOR_EXTENSION)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MotorExtensionBlockEntity>> ADVANCED_MOTOR_EXTENSION = Reg
            .blockEntity("advanced_motor_extension", MotorExtensionBlockEntity.create(new AdvancedMotorExtensionVariant()))
            .validBlocks(CNABlocks.ADVANCED_MOTOR_EXTENSION)
            .register();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StreetLightBlockEntity>> STREET_LIGHT = Reg
            .blockEntity("street_light", StreetLightBlockEntity::new)
            .validBlocks(CNABlocks.STREET_LIGHT)
            .register();

    public static void load() {  }
}
