package com.mrh0.createaddition.index;

import com.mrh0.createaddition.blocks.alternator.AlternatorBlockEntity;
import com.mrh0.createaddition.blocks.connector.LargeConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.SmallConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.SmallLightConnectorBlockEntity;
import com.mrh0.createaddition.blocks.creative_energy.CreativeEnergyBlockEntity;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorBlockEntity;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockEntity;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillBlockEntity;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlockEntity;
import com.mrh0.createaddition.item.CapacitorItem;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CACapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        AlternatorBlockEntity.registerCapabilities(event);
        LargeConnectorBlockEntity.registerCapabilities(event);
        SmallConnectorBlockEntity.registerCapabilities(event);
        SmallLightConnectorBlockEntity.registerCapabilities(event);
        ElectricMotorBlockEntity.registerCapabilities(event);
        CreativeEnergyBlockEntity.registerCapabilities(event);
        ModularAccumulatorBlockEntity.registerCapabilities(event);
        TeslaCoilBlockEntity.registerCapabilities(event);
        CapacitorItem.registerCapabilities(event);
    }
}
