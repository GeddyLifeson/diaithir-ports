package com.diaithir.addons.newage.content.heat.plate;

import net.neoforged.neoforge.registries.DeferredHolder;
import com.diaithir.addons.newage.content.TooltipBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.diaithir.addons.newage.CNABlockEntityTypes;
import com.diaithir.addons.newage.config.CNAConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.diaithir.addons.newage.content.heat.pipe.HeatPipeBlock.massPipe;

public class SolarHeatingPlateBlock extends Block implements EntityBlock, IWrenchable , TooltipBlock{
    private final DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<?>> entry;
    private final int strength;

    public SolarHeatingPlateBlock(Properties properties, DeferredHolder<BlockEntityType<?>, ? extends BlockEntityType<?>> entry, int strength) {
        super(properties.strength(4.0f));
        this.entry = entry;
        this.strength = strength;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return entry.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        final int on = massPipe;
        massPipe++;
        if (massPipe >= 20) {
            massPipe = 0;
        }
        return (level1, blockPos, blockState, blockEntity) -> {
            if ((level1.getGameTime() + on) % 20 != 0 || !(blockEntity instanceof SolarHeatingPlateBlockEntity ent)) return;
            ent.tick(blockPos, level1, blockState);
        };
    }

    public static SolarHeatingPlateBlock createAdvanced(Properties properties) {
        return  new SolarHeatingPlateBlock(properties, CNABlockEntityTypes.ADVANCED_SOLAR_HEATING_PLATE, 60);
    }

    public static SolarHeatingPlateBlock createBasic(Properties properties) {
        return  new SolarHeatingPlateBlock(properties, CNABlockEntityTypes.BASIC_SOLAR_HEATING_PLATE, 20);
    }

    public void appendTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(CreateLang.translate("tooltip.create_new_age.generates").style(ChatFormatting.GRAY)
                .component());
        tooltipComponents.add(CreateLang.text(" ").translate("tooltip.create_new_age.temperature.ps", strength * CNAConfig.getServer().solarPanelHeatMultiplier.get()).style(ChatFormatting.AQUA).component());
    }
}
