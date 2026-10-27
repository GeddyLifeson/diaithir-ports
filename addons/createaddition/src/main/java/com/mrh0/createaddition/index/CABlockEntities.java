package com.mrh0.createaddition.index;

import com.mrh0.createaddition.blocks.alternator.AlternatorBlockEntity;
import com.mrh0.createaddition.blocks.connector.LargeConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.SmallConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.SmallLightConnectorBlockEntity;
import com.mrh0.createaddition.blocks.creative_energy.CreativeEnergyBlockEntity;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorBlockEntity;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockEntity;
import com.mrh0.createaddition.blocks.redstone_relay.RedstoneRelayBlockEntity;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillBlockEntity;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlockEntity;
import com.mrh0.createaddition.reg.BlockEntityEntry;
import com.mrh0.createaddition.reg.R;

public class CABlockEntities {
    public static final BlockEntityEntry<ElectricMotorBlockEntity> ELECTRIC_MOTOR = R.blockEntity("electric_motor",
            (pos, state) -> new ElectricMotorBlockEntity(CABlockEntities.ELECTRIC_MOTOR.get(), pos, state), CABlocks.ELECTRIC_MOTOR.get());
    public static final BlockEntityEntry<AlternatorBlockEntity> ALTERNATOR = R.blockEntity("alternator",
            (pos, state) -> new AlternatorBlockEntity(CABlockEntities.ALTERNATOR.get(), pos, state), CABlocks.ALTERNATOR.get());
    public static final BlockEntityEntry<RollingMillBlockEntity> ROLLING_MILL = R.blockEntity("rolling_mill",
            (pos, state) -> new RollingMillBlockEntity(CABlockEntities.ROLLING_MILL.get(), pos, state), CABlocks.ROLLING_MILL.get());
    public static final BlockEntityEntry<CreativeEnergyBlockEntity> CREATIVE_ENERGY = R.blockEntity("creative_energy",
            (pos, state) -> new CreativeEnergyBlockEntity(CABlockEntities.CREATIVE_ENERGY.get(), pos, state), CABlocks.CREATIVE_ENERGY.get());
    public static final BlockEntityEntry<SmallConnectorBlockEntity> SMALL_CONNECTOR = R.blockEntity("connector",
            (pos, state) -> new SmallConnectorBlockEntity(CABlockEntities.SMALL_CONNECTOR.get(), pos, state), CABlocks.SMALL_CONNECTOR.get());
    public static final BlockEntityEntry<SmallLightConnectorBlockEntity> SMALL_LIGHT_CONNECTOR = R.blockEntity("small_light_connector",
            (pos, state) -> new SmallLightConnectorBlockEntity(CABlockEntities.SMALL_LIGHT_CONNECTOR.get(), pos, state), CABlocks.SMALL_LIGHT_CONNECTOR.get());
    public static final BlockEntityEntry<LargeConnectorBlockEntity> LARGE_CONNECTOR = R.blockEntity("large_connector",
            (pos, state) -> new LargeConnectorBlockEntity(CABlockEntities.LARGE_CONNECTOR.get(), pos, state), CABlocks.LARGE_CONNECTOR.get());
    public static final BlockEntityEntry<RedstoneRelayBlockEntity> REDSTONE_RELAY = R.blockEntity("redstone_relay",
            (pos, state) -> new RedstoneRelayBlockEntity(CABlockEntities.REDSTONE_RELAY.get(), pos, state), CABlocks.REDSTONE_RELAY.get());
    public static final BlockEntityEntry<TeslaCoilBlockEntity> TESLA_COIL = R.blockEntity("tesla_coil",
            (pos, state) -> new TeslaCoilBlockEntity(CABlockEntities.TESLA_COIL.get(), pos, state), CABlocks.TESLA_COIL.get());
    public static final BlockEntityEntry<ModularAccumulatorBlockEntity> MODULAR_ACCUMULATOR = R.blockEntity("modular_accumulator",
            (pos, state) -> new ModularAccumulatorBlockEntity(CABlockEntities.MODULAR_ACCUMULATOR.get(), pos, state), CABlocks.MODULAR_ACCUMULATOR.get());

    public static void register() {}
}
