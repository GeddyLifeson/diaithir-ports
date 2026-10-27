package com.diaithir.addons.newage;

import com.zurrtum.create.AllBlocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import com.zurrtum.create.content.decoration.encasing.CasingBlock;
import com.zurrtum.create.content.decoration.encasing.EncasingRegistry;
import com.zurrtum.create.content.processing.AssemblyOperatorBlockItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import com.diaithir.addons.newage.content.electricity.connector.ElectricalConnectorBlock;
import com.diaithir.addons.newage.content.electricity.generation.brushes.CarbonBrushesBlock;
import com.diaithir.addons.newage.content.electricity.generation.brushes.CarbonBrushesItem;
import com.diaithir.addons.newage.content.electricity.generation.coil.GeneratorCoilBlock;
import com.diaithir.addons.newage.content.electricity.generation.magnet.ImplementedMagnetBlock;
import com.diaithir.addons.newage.content.electricity.light.LampPostBlock;
import com.diaithir.addons.newage.content.electricity.light.StreetLightBlock;
import com.diaithir.addons.newage.content.energising.EnergiserBlock;
import com.diaithir.addons.newage.content.energising.EnergisingBlockItem;
import com.diaithir.addons.newage.content.heat.heater.HeaterBlock;
import com.diaithir.addons.newage.content.heat.pipe.HeatPipeBlock;
import com.diaithir.addons.newage.content.heat.pipe.EncasedHeatPipeBlock;
import com.diaithir.addons.newage.content.heat.pipe.ReactorEncasedHeatPipeBlock;
import com.diaithir.addons.newage.content.heat.plate.SolarHeatingPlateBlock;
import com.diaithir.addons.newage.content.heat.pump.HeatPumpBlock;
import com.diaithir.addons.newage.content.heat.stirling.StirlingEngineBlock;
import com.diaithir.addons.newage.content.heat.stirling.StirlingEngineItem;
import com.diaithir.addons.newage.content.motor.MotorBlock;
import com.diaithir.addons.newage.content.motor.MotorBlockItem;
import com.diaithir.addons.newage.content.motor.extension.MotorExtensionBlock;
import com.diaithir.addons.newage.content.motor.extension.variants.AdvancedMotorExtensionVariant;
import com.diaithir.addons.newage.content.motor.extension.variants.BasicMotorExtensionVariant;
import com.diaithir.addons.newage.content.motor.variants.AdvancedMotorVariant;
import com.diaithir.addons.newage.content.motor.variants.BasicMotorVariant;
import com.diaithir.addons.newage.content.motor.variants.ReinforcedMotorVariant;
import com.diaithir.addons.newage.content.nuclear.CoriumBlock;
import com.diaithir.addons.newage.content.nuclear.SolidCoriumBlock;
import com.diaithir.addons.newage.content.nuclear.reactor.ReactorBlock;
import com.diaithir.addons.newage.content.nuclear.reactor.ReactorCasingBlock;
import com.diaithir.addons.newage.content.nuclear.reactor.ReactorTransparentBlock;
import com.diaithir.addons.newage.content.nuclear.reactor.fuelacceptor.ReactorFuelAcceptorBlock;
import com.diaithir.addons.newage.content.nuclear.reactor.rod.ReactorRodBlock;
import com.diaithir.addons.newage.content.nuclear.reactor.vent.ReactorHeatVentBlock;

import java.util.function.UnaryOperator;


public class CNABlocks {
    public static <B extends Block> UnaryOperator<Reg.BlockB<B>> setImpact(double value) {
        return b -> b.setImpact(value);
    }

    public static <B extends Block> UnaryOperator<Reg.BlockB<B>> setCapacity(double value) {
        return b -> b.setCapacity(value);
    }

    public static final DeferredBlock<EnergiserBlock> BASIC_ENERGISER =
            Reg
            .block("basic_energiser", EnergiserBlock::newBasic)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(setImpact(4.0))
            .item(EnergisingBlockItem::new)
            .build()
            .register();

    public static final DeferredBlock<EnergiserBlock> ADVANCED_ENERGISER =
            Reg
            .block("advanced_energiser", EnergiserBlock::newAdvanced)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(setImpact(8.0))
            .item(EnergisingBlockItem::new)
            .build()
            .register();

    public static final DeferredBlock<EnergiserBlock> REINFORCED_ENERGISER =
            Reg
            .block("reinforced_energiser", EnergiserBlock::newReinforced)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(setImpact(32.0))
            .item(EnergisingBlockItem::new)
            .build()
            .register();

    public static final DeferredBlock<ElectricalConnectorBlock> ELECTRICAL_CONNECTOR =
            Reg
            .block("electrical_connector", ElectricalConnectorBlock::new)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(properties -> properties.strength(0.4f))
            .item()
            .build()
            .register();

    public static final DeferredBlock<GeneratorCoilBlock> GENERATOR_COIL =
            Reg
            .block("generator_coil", GeneratorCoilBlock::new)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(setImpact(24.0f))
            .item()
            .build()
            .register();

    
    public static final DeferredBlock<ImplementedMagnetBlock> MAGNETITE_BLOCK =
            Reg
            .block("magnetite_block", ImplementedMagnetBlock.simple(1))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .simpleItem()
            .register();

    public static final DeferredBlock<ImplementedMagnetBlock> REDSTONE_MAGNET =
            Reg
            .block("redstone_magnet", ImplementedMagnetBlock.simple(2))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .simpleItem()
            .register();

    public static final DeferredBlock<ImplementedMagnetBlock> LAYERED_MAGNET =
            Reg
            .block("layered_magnet", ImplementedMagnetBlock.simple(4))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .simpleItem()
            .register();

    public static final DeferredBlock<ImplementedMagnetBlock> FLUXUATED_MAGNETITE =
            Reg
            .block("fluxuated_magnetite", ImplementedMagnetBlock.simple(8))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .simpleItem()
            .register();

    public static final DeferredBlock<ImplementedMagnetBlock> NETHERITE_MAGNET =
            Reg
            .block("netherite_magnet", ImplementedMagnetBlock.simple(24))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .simpleItem()
            .register();
    
    
    public static final DeferredBlock<CarbonBrushesBlock> CARBON_BRUSHES =
            Reg
            .block("carbon_brushes", CarbonBrushesBlock::new)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .item(CarbonBrushesItem::new)
            .build()
            .register();
    
    public static final DeferredBlock<MotorBlock> BASIC_MOTOR =
            Reg
            .block("basic_motor", (p) -> new MotorBlock(p, CNABlockEntityTypes.BASIC_MOTOR, new BasicMotorVariant()))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(properties -> properties.strength(3.0f))
            .item(MotorBlockItem::new)
            .build()
            .register();

    public static final DeferredBlock<MotorBlock> ADVANCED_MOTOR =
            Reg
            .block("advanced_motor", (p) -> new MotorBlock(p, CNABlockEntityTypes.ADVANCED_MOTOR, new AdvancedMotorVariant()))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(properties -> properties.strength(3.5f))
            .item(MotorBlockItem::new)
            .build()
            .register();


    public static final DeferredBlock<MotorBlock> REINFORCED_MOTOR =
            Reg
            .block("reinforced_motor", (p) -> new MotorBlock(p, CNABlockEntityTypes.REINFORCED_MOTOR, new ReinforcedMotorVariant()))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(properties -> properties.strength(4.0f))
            .item(MotorBlockItem::new)
            .build()
            .register();

    public static final DeferredBlock<MotorExtensionBlock> BASIC_MOTOR_EXTENSION =
            Reg
            .block("basic_motor_extension", (p) -> new MotorExtensionBlock(p, CNABlockEntityTypes.BASIC_MOTOR_EXTENSION, new BasicMotorExtensionVariant()))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(properties -> properties.strength(4.0f))
            .item()
            .build()
            .register();

    public static final DeferredBlock<MotorExtensionBlock> ADVANCED_MOTOR_EXTENSION =
            Reg
            .block("advanced_motor_extension", (p) -> new MotorExtensionBlock(p, CNABlockEntityTypes.ADVANCED_MOTOR_EXTENSION, new AdvancedMotorExtensionVariant()))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(properties -> properties.strength(4.0f))
            .item()
            .build()
            .register();


    public static final DeferredBlock<CasingBlock> HEAT_CASING =
            Reg
            .block("heat_casing", CasingBlock::new)
            .initialProperties(() -> AllBlocks.ANDESITE_CASING)
            .properties((p) -> p.requiresCorrectToolForDrops().mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .item()
            .build()
            .register();

    public static final DeferredBlock<HeatPipeBlock> HEAT_PIPE =
            Reg
            .block("heat_pipe", HeatPipeBlock::new)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(properties -> properties.strength(1.6f))
            .item()
            .build()
            .register();

    public static final DeferredBlock<EncasedHeatPipeBlock> ENCASED_HEAT_PIPE =
            Reg
            .block("encased_heat_pipe", (p) -> new EncasedHeatPipeBlock(p, HEAT_CASING::get))
            .initialProperties(HEAT_CASING::get)
            .register();

    public static final DeferredBlock<HeatPumpBlock> HEAT_PUMP =
            Reg
            .block("heat_pump", HeatPumpBlock::new)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(properties -> properties.strength(1.6f))
            .item()
            .build()
            .register();

    public static final DeferredBlock<HeaterBlock> HEATER =
            Reg
            .block("heater", HeaterBlock::new)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(properties -> properties.strength(2.0f))
            .item()
            .build()
            .register();


    public static final DeferredBlock<StirlingEngineBlock> STIRLING_ENGINE =
            Reg
            .block("stirling_engine", properties -> new StirlingEngineBlock(properties, CNABlockEntityTypes.STIRLING_ENGINE))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(properties -> properties.strength(2.0f))
            .transform(setCapacity(32.0))
            .item(StirlingEngineItem::new)
            .build()
            .register();

    public static final DeferredBlock<SolidCoriumBlock> SOLID_CORIUM =
            Reg
            .block("solid_corium", SolidCoriumBlock::new)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(properties -> properties.strength(50.0f))
            .simpleItem()
            .register();

    public static final DeferredBlock<CoriumBlock> CORIUM =
            Reg
            .block("corium", CoriumBlock::new)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(properties -> properties.strength(70.0f))
            .simpleItem()
            .register();

    public static final DeferredBlock<ReactorCasingBlock> REACTOR_CASING =
            Reg
            .block("reactor_casing", ReactorCasingBlock::new)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .item()
            .build()
            .register();

    public static final DeferredBlock<ReactorEncasedHeatPipeBlock> REACTOR_ENCASED_HEAT_PIPE =
            Reg
            .block("reactor_encased_heat_pipe", (p) -> new ReactorEncasedHeatPipeBlock(p, REACTOR_CASING::get))
            .initialProperties(REACTOR_CASING::get)
            .register();

    public static final DeferredBlock<ReactorRodBlock> REACTOR_ROD =
            Reg
            .block("reactor_rod", ReactorRodBlock::new)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .simpleItem()
            .register();

    public static final DeferredBlock<ReactorTransparentBlock> REACTOR_GLASS =
            Reg
            .block("reactor_glass", ReactorTransparentBlock::new)
            .initialProperties(() -> Blocks.GLASS)
            .properties(p -> p.isViewBlocking((blockState, blockGetter, blockPos) -> false))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(p -> p.isViewBlocking((a,b,c) -> false))
            .simpleItem()
            .register();

    public static final DeferredBlock<ReactorFuelAcceptorBlock> REACTOR_FUEL_ACCEPTOR =
            Reg
            .block("reactor_fuel_acceptor", ReactorFuelAcceptorBlock::new)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .simpleItem()
            .register();


    public static final DeferredBlock<ReactorHeatVentBlock> REACTOR_HEAT_VENT =
            Reg
            .block("reactor_heat_vent", ReactorHeatVentBlock::new)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .simpleItem()
            .register();


    public static final DeferredBlock<SolarHeatingPlateBlock> BASIC_SOLAR_HEATING_PLATE =
            Reg
            .block("basic_solar_heating_plate", SolarHeatingPlateBlock::createBasic)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .simpleItem()
            .register();

    public static final DeferredBlock<SolarHeatingPlateBlock> ADVANCED_SOLAR_HEATING_PLATE =
            Reg
            .block("advanced_solar_heating_plate", SolarHeatingPlateBlock::createAdvanced)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .simpleItem()
            .register();


    public static final DeferredBlock<Block> THORIUM_ORE =
            Reg
            .block("thorium_ore", Block::new)
            .properties((p) -> p.strength(3.5f).requiresCorrectToolForDrops())
            .item(AssemblyOperatorBlockItem::new)
                    .build()
                    .register();

    public static final DeferredBlock<RotatedPillarBlock> COPPER_WIRE_BLOCK =
            Reg
            .block("copper_wire_block", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.COPPER_BLOCK)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.sound(SoundType.COPPER))
            .simpleItem()
            .register();

    public static final DeferredBlock<RotatedPillarBlock> OVERCHARGED_IRON_WIRE_BLOCK =
            Reg
            .block("overcharged_iron_wire_block", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.sound(SoundType.COPPER))
            .simpleItem()
            .register();

    public static final DeferredBlock<RotatedPillarBlock> OVERCHARGED_GOLDEN_WIRE_BLOCK =
            Reg
            .block("overcharged_golden_wire_block", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.GOLD_BLOCK)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.sound(SoundType.COPPER))
            .simpleItem()
            .register();

    public static final DeferredBlock<RotatedPillarBlock> OVERCHARGED_DIAMOND_WIRE_BLOCK =
            Reg
            .block("overcharged_diamond_wire_block", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.DIAMOND_BLOCK)
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.sound(SoundType.COPPER))
            .simpleItem()
            .register();

    public static final DeferredBlock<StreetLightBlock> STREET_LIGHT =
            Reg
            .block("street_light", StreetLightBlock::new)
            .initialProperties(() -> Blocks.REDSTONE_LAMP)
            .properties(p -> p.lightLevel(s -> s.getValue(StreetLightBlock.LIGHT_LEVEL)))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.sound(SoundType.LANTERN))
            .item()
            .build()
            .register();

    public static final DeferredBlock<LampPostBlock> LAMP_POST =
            Reg
            .block("lamp_post", LampPostBlock::new)
            .properties(p -> p.strength(5.0f, 6.0f))
            .properties((p) -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
            .properties(BlockBehaviour.Properties::requiresCorrectToolForDrops)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .item()
            .build()
            .register();

    public static void load() {  }
}
