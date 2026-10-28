package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.MaterialPipeBlock;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.pipenet.IPipeNode;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;
import com.gregtechceu.gtceu.client.model.pipe.PipeModel;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;
import com.gregtechceu.gtceu.common.pipelike.item.ItemPipeType;
import com.gregtechceu.gtceu.common.pipelike.item.LevelItemPipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.List;

public class ItemPipeBlock extends MaterialPipeBlock<ItemPipeType, ItemPipeProperties, LevelItemPipeNet> {

    public ItemPipeBlock(Properties properties, ItemPipeType itemPipeType, Material material) {
        super(properties, itemPipeType, material);
    }

    @Override
    protected ItemPipeProperties createProperties(ItemPipeType itemPipeType, Material material) {
        return itemPipeType.modifyProperties(material.getProperty(PropertyKey.ITEM_PIPE));
    }

    public void attachCapabilities(RegisterCapabilitiesEvent event) {  /* ported stub */ }

    @Override
    protected ItemPipeProperties createMaterialData() {
        return material.getProperty(PropertyKey.ITEM_PIPE);
    }

    @Override
    public PipeModel createPipeModel(GTBlockstateProvider provider) {
        return pipeType.createPipeModel(this, material, provider);
    }

    @Override
    public LevelItemPipeNet getWorldPipeNet(ServerLevel level) {
        return LevelItemPipeNet.getOrCreate(level);
    }

    @Override
    public BlockEntityType<? extends PipeBlockEntity<ItemPipeType, ItemPipeProperties>> getBlockEntityType() {
        return GTBlockEntities.ITEM_PIPE.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {  /* ported stub */ }

    @Override
    public boolean canPipesConnect(IPipeNode<ItemPipeType, ItemPipeProperties> selfTile, Direction side,
                                   IPipeNode<ItemPipeType, ItemPipeProperties> sideTile) {
        return selfTile instanceof ItemPipeBlockEntity && sideTile instanceof ItemPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeNode<ItemPipeType, ItemPipeProperties> selfTile, Direction side,
                                         Level level, BlockPos pos) { return false; /* ported stub */ }
}
