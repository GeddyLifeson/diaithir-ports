package com.misterd.agritechevolved.compat.jade;

import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.AdvancedPlanterBlock;
import com.misterd.agritechevolved.block.custom.PlanterBlock;
import com.misterd.agritechevolved.blockentity.custom.AdvancedPlanterBlockEntity;
import com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.datamap.FertilizerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum PlanterProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    static final Identifier UID =
            Identifier.fromNamespaceAndPath("agritechevolved", "planter_info");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be instanceof AdvancedPlanterBlockEntity advanced) {
            appendAdvancedPlanterData(data, advanced, accessor.getBlockState());
        } else if (be instanceof PlanterBlockEntity basic) {
            appendBasicPlanterData(data, basic, accessor.getBlockState());
        }
    }

    private void appendBasicPlanterData(CompoundTag data, PlanterBlockEntity planter, BlockState state) {
        data.putBoolean("isAdvanced", false);

        ItemStack seedStack = planter.getStack(0);
        ItemStack soilStack = planter.getStack(1);
        if (seedStack.isEmpty() || soilStack.isEmpty()) {
            data.putBoolean("hasCrop", false);
            return;
        }

        appendCommonCropData(data, seedStack, soilStack,
                planter.getGrowthStage(), planter.getGrowthProgress(),
                planter.getSoilGrowthModifier(soilStack), planter.isTree());
        appendFertilizerData(data, planter.getStack(2));
        appendClocheData(data, state.getValue(PlanterBlock.CLOCHED));
    }

    private void appendAdvancedPlanterData(CompoundTag data, AdvancedPlanterBlockEntity planter, BlockState state) {
        data.putBoolean("isAdvanced", true);

        ItemStack seedStack = planter.getStack(0);
        ItemStack soilStack = planter.getStack(1);
        if (seedStack.isEmpty() || soilStack.isEmpty()) {
            data.putBoolean("hasCrop", false);
            return;
        }

        appendCommonCropData(data, seedStack, soilStack,
                planter.getGrowthStage(), planter.getGrowthProgress(),
                planter.getSoilGrowthModifier(soilStack), planter.isTree());
        appendFertilizerData(data, planter.getStack(4));
        appendClocheData(data, state.getValue(AdvancedPlanterBlock.CLOCHED));

        data.putInt("energyStored", planter.getEnergyStored());
        data.putInt("maxEnergy", planter.getMaxEnergyStored());

        boolean hasModules = !planter.getStack(2).isEmpty() || !planter.getStack(3).isEmpty();
        data.putBoolean("hasModules", hasModules);
        if (hasModules) {
            data.putFloat("moduleSpeedModifier", planter.getModuleSpeedModifier());
            data.putFloat("moduleYieldModifier", planter.getModuleYieldModifier());
        }
    }

    private void appendCommonCropData(CompoundTag data, ItemStack seedStack, ItemStack soilStack, int growthStage, float growthProgress, float soilModifier, boolean isTree) {
        data.putBoolean("hasCrop", true);
        data.putString("cropName", seedStack.getDisplayName().getString());
        data.putInt("currentStage", growthStage);
        data.putInt("maxStage", isTree ? 1 : 8);
        data.putFloat("progressPercent", growthProgress * 100.0F);
        data.putString("soilName", soilStack.getDisplayName().getString());
        data.putFloat("growthModifier", soilModifier);
    }

    private void appendFertilizerData(CompoundTag data, ItemStack fertStack) {
        if (fertStack.isEmpty()) {
            data.putBoolean("hasFertilizer", false);
            return;
        }
        FertilizerData fertData = fertStack.getItem().builtInRegistryHolder().getData(ATEDataMaps.FERTILIZERS);
        data.putBoolean("hasFertilizer", fertData != null);
        if (fertData != null) {
            data.putString("fertilizerName", fertStack.getDisplayName().getString());
            data.putFloat("fertilizerSpeedModifier", fertData.speedMultiplier());
            data.putFloat("fertilizerYieldModifier", fertData.yieldMultiplier());
        }
    }

    private void appendClocheData(CompoundTag data, boolean cloched) {
        data.putBoolean("isCloched", cloched);
        if (cloched) {
            data.putFloat("clocheSpeedModifier", (float) Config.getClocheSpeedMultiplier());
            data.putFloat("clocheYieldModifier", (float) Config.getClocheYieldMultiplier());
        }
    }
}