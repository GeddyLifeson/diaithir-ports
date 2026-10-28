package com.gregtechceu.gtceu.common.block;

import net.minecraft.client.renderer.block.BlockAndTintGetter;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.WireProperties;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.pipelike.cable.Insulation;
import com.gregtechceu.gtceu.common.pipelike.cable.LevelEnergyNet;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CableBlock extends MaterialPipeBlock<Insulation, WireProperties, LevelEnergyNet> {

    public CableBlock(Properties properties, Insulation insulation, Material material) {
        super(properties, insulation, material);
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(GTCapability.CAPABILITY_ENERGY_CONTAINER, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof CableBlockEntity cableBlockEntity) {
                return cableBlockEntity.getEnergyContainer(side);
            }
            return null;
        }, this);
        event.registerBlock(GTCapability.CAPABILITY_COVERABLE, (level, pos, state, blockEntity, side) -> {
            if (blockEntity instanceof PipeBlockEntity<?, ?> pipe) {
                return pipe.getCoverContainer();
            }
            return null;
        }, this);
    }

    @Override
    public int tinted(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int index) {
        if (pipeType.isCable && (index == 0 || index == 2)) {
            return 0x404040;
        }
        return super.tinted(state, level, pos, index);
    }

    @Override
    protected WireProperties createProperties(Insulation insulation, Material material) {
        return insulation.modifyProperties(material.getProperty(PropertyKey.WIRE));
    }

    @Override
    protected WireProperties createMaterialData() {
        return material.getProperty(PropertyKey.WIRE);
    }

    @Override
    public LevelEnergyNet getWorldPipeNet(ServerLevel level) {
        return LevelEnergyNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<Insulation, WireProperties>> getBlockEntityType() {
        return GTBlockEntities.CABLE.get();
    }

    @Override
    public boolean canPipesConnect(IPipeNode<Insulation, WireProperties> selfTile, Direction side,
                                   IPipeNode<Insulation, WireProperties> sideTile) {
        return selfTile instanceof CableBlockEntity && sideTile instanceof CableBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<Insulation, WireProperties> selfTile,
                                         Direction side, Level level, BlockPos pos) {
        return level.getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, pos, side.getOpposite()) != null;
    }

    @Override
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return pipeType.createPipeModel(this, material, provider);
    }

    
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {  /* ported stub */ }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {  /* ported stub */ }

    @Override
    public GTToolType getPipeTuneTool() {
        return GTToolType.WIRE_CUTTER;
    }
}
