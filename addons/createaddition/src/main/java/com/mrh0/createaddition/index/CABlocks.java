package com.mrh0.createaddition.index;

import com.mrh0.createaddition.blocks.alternator.AlternatorBlock;
import com.mrh0.createaddition.blocks.barbed_wire.BarbedWireBlock;
import com.mrh0.createaddition.blocks.cake.CACakeBlock;
import com.mrh0.createaddition.blocks.connector.LargeConnectorBlock;
import com.mrh0.createaddition.blocks.connector.SmallConnectorBlock;
import com.mrh0.createaddition.blocks.connector.SmallLightConnectorBlock;
import com.mrh0.createaddition.blocks.creative_energy.CreativeEnergyBlock;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorBlock;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlock;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockItem;
import com.mrh0.createaddition.blocks.redstone_relay.RedstoneRelayBlock;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillBlock;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlock;
import com.mrh0.createaddition.config.CommonConfig;
import com.mrh0.createaddition.energy.NodeMovementBehaviour;
import com.mrh0.createaddition.item.BiomassPelletBlockItem;
import com.mrh0.createaddition.reg.BlockEntry;
import com.mrh0.createaddition.reg.R;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.stress.BlockStressValues;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public class CABlocks {
    public static final BlockEntry<ElectricMotorBlock> ELECTRIC_MOTOR = R.block("electric_motor", ElectricMotorBlock::new, R.softMetal());
    public static final BlockEntry<AlternatorBlock> ALTERNATOR = R.block("alternator", AlternatorBlock::new, R.softMetal());
    public static final BlockEntry<RollingMillBlock> ROLLING_MILL = R.block("rolling_mill", RollingMillBlock::new, R.stone());
    public static final BlockEntry<CreativeEnergyBlock> CREATIVE_ENERGY = R.block("creative_energy", CreativeEnergyBlock::new, R.softMetal(),
            net.minecraft.world.item.BlockItem::new, new Item.Properties().rarity(Rarity.EPIC));
    public static final BlockEntry<SmallConnectorBlock> SMALL_CONNECTOR = R.block("connector", SmallConnectorBlock::new, R.stone());
    public static final BlockEntry<SmallLightConnectorBlock> SMALL_LIGHT_CONNECTOR = R.block("small_light_connector", SmallLightConnectorBlock::new, R.stone());
    public static final BlockEntry<LargeConnectorBlock> LARGE_CONNECTOR = R.block("large_connector", LargeConnectorBlock::new, R.stone());
    public static final BlockEntry<RedstoneRelayBlock> REDSTONE_RELAY = R.block("redstone_relay", RedstoneRelayBlock::new, R.stone());
    public static final BlockEntry<CACakeBlock> CHOCOLATE_CAKE = R.block("chocolate_cake", CACakeBlock::new, Properties.ofFullCopy(Blocks.CAKE).sound(SoundType.WOOL).strength(0.5f));
    public static final BlockEntry<CACakeBlock> HONEY_CAKE = R.block("honey_cake", CACakeBlock::new, Properties.ofFullCopy(Blocks.CAKE).sound(SoundType.WOOL).strength(0.5f));
    public static final BlockEntry<BarbedWireBlock> BARBED_WIRE = R.block("barbed_wire", BarbedWireBlock::new,
            Properties.ofFullCopy(Blocks.COBWEB).noCollision().requiresCorrectToolForDrops().strength(4.0F));
    public static final BlockEntry<TeslaCoilBlock> TESLA_COIL = R.block("tesla_coil", TeslaCoilBlock::new,
            R.softMetal().lightLevel(state -> state.getValue(TeslaCoilBlock.POWERED) ? 10 : 0));
    public static final BlockEntry<ModularAccumulatorBlock> MODULAR_ACCUMULATOR = R.block("modular_accumulator", ModularAccumulatorBlock::regular,
            R.softMetal().noOcclusion(), ModularAccumulatorBlockItem::new, new Item.Properties());
    public static final BlockEntry<Block> BIOMASS_PALLET = R.block("biomass_pellet_block", Block::new,
            Properties.ofFullCopy(Blocks.DRIED_KELP_BLOCK).mapColor(MapColor.COLOR_GREEN), BiomassPelletBlockItem::new, new Item.Properties());
    public static final BlockEntry<Block> ELECTRUM_BLOCK = R.block("electrum_block", Block::new,
            Properties.ofFullCopy(Blocks.GOLD_BLOCK).mapColor(MapColor.TERRACOTTA_YELLOW));

    public static void register() {
        NodeMovementBehaviour node = new NodeMovementBehaviour();
        for (Block b : new Block[]{SMALL_CONNECTOR.get(), SMALL_LIGHT_CONNECTOR.get(), LARGE_CONNECTOR.get(), REDSTONE_RELAY.get()})
            MovementBehaviour.REGISTRY.register(b, node);
        BlockStressValues.setGeneratorSpeed(ELECTRIC_MOTOR.get(), 256, true);
        BlockStressValues.CAPACITIES.register(ELECTRIC_MOTOR.get(), () -> CommonConfig.MAX_STRESS.get() / 256f);
        BlockStressValues.IMPACTS.register(ALTERNATOR.get(), () -> CommonConfig.MAX_STRESS.get() / 256f);
        BlockStressValues.IMPACTS.register(ROLLING_MILL.get(), () -> CommonConfig.ROLLING_MILL_STRESS.get());
    }
}
