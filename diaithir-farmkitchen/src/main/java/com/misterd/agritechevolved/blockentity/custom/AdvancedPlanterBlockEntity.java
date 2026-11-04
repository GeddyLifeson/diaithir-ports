package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.AdvancedPlanterBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.datamap.FertilizerData;
import com.misterd.agritechevolved.datamap.SoilModifierData;
import com.misterd.agritechevolved.gui.custom.AdvancedPlanterMenu;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.integration.PlanterPostHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterPreHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterProcessingTimeEvent;
import com.misterd.agritechevolved.recipe.ATERecipeTypes;
import com.misterd.agritechevolved.recipe.CropRecipe;
import com.misterd.agritechevolved.recipe.DropEntry;
import com.misterd.agritechevolved.recipe.TreeRecipe;
import com.misterd.agritechevolved.util.ATETags;
import com.misterd.agritechevolved.util.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.*;

public class AdvancedPlanterBlockEntity extends BlockEntity implements MenuProvider {

    private static final int SLOT_PLANT = 0;
    private static final int SLOT_SOIL = 1;
    private static final int SLOT_MODULE_1 = 2;
    private static final int SLOT_MODULE_2 = 3;
    private static final int SLOT_FERTILIZER = 4;
    private static final int SLOT_OUTPUT_MIN = 5;
    private static final int SLOT_OUTPUT_MAX = 16;
    private static final int TOTAL_SLOTS = 17;

    private static final String SM_MK1 = "agritechevolved:sm_mk1";
    private static final String SM_MK2 = "agritechevolved:sm_mk2";
    private static final String SM_MK3 = "agritechevolved:sm_mk3";
    private static final String YM_MK1 = "agritechevolved:ym_mk1";
    private static final String YM_MK2 = "agritechevolved:ym_mk2";
    private static final String YM_MK3 = "agritechevolved:ym_mk3";

    private @Nullable CropRecipe cachedCropRecipe = null;
    private @Nullable TreeRecipe cachedTreeRecipe = null;
    private @Nullable Item cachedSeedItem = null;
    private Set<Item> cachedValidSoils = null;
    private int soilCacheRevision = -1;
    private int cachedRevision = -1;

    public final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(TOTAL_SLOTS) {
        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return (index == SLOT_PLANT || index == SLOT_SOIL || index == SLOT_MODULE_1 || index == SLOT_MODULE_2)
                    ? 1
                    : resource.toStack().getMaxStackSize();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (resource.isEmpty()) return false;
            ItemStack stack = resource.toStack();
            return switch (index) {
                case SLOT_PLANT -> {
                    if (!isValidPlant(stack)) yield false;
                    ItemStack soil = getStack(SLOT_SOIL);
                    if (soil.isEmpty()) yield true;
                    yield isValidPlantSoilCombination(stack, soil);
                }
                case SLOT_SOIL -> {
                    if (!isValidSoilForAnyRecipe(stack)) yield false;
                    ItemStack plant = getStack(SLOT_PLANT);
                    if (plant.isEmpty()) yield true;
                    yield isValidPlantSoilCombination(plant, stack);
                }
                case SLOT_MODULE_1, SLOT_MODULE_2 -> stack.is(ATETags.Items.ATE_MODULES);
                case SLOT_FERTILIZER -> isFertilizer(stack);
                default -> true;
            };
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            AdvancedPlanterBlockEntity.this.setChanged();
            Level lvl = AdvancedPlanterBlockEntity.this.level;
            if (lvl != null && !lvl.isClientSide()) {
                BlockPos p = AdvancedPlanterBlockEntity.this.getBlockPos();
                lvl.sendBlockUpdated(p, AdvancedPlanterBlockEntity.this.getBlockState(), AdvancedPlanterBlockEntity.this.getBlockState(), 3);
            }
        }
    };

    private int growthProgress = 0;
    private int growthTicks = 0;
    private boolean readyToHarvest = false;
    private int lastGrowthStage = -1;
    private int energyStored = 0;
    private float currentTotalModifier = 1.0F;

    public AdvancedPlanterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.ADVANCED_PLANTER_BLOCK_BE.get(), pos, blockState);
    }

    private void invalidateRecipeCache() {
        cachedCropRecipe = null;
        cachedTreeRecipe = null;
        cachedSeedItem = null;
        cachedRevision = -1;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.agritechevolved.advanced_planter");
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AdvancedPlanterMenu(id, inv, this);
    }

    @Nullable
    private RecipeManager getRecipes() {
        if (level instanceof ServerLevel serverLevel) return serverLevel.recipeAccess();
        return null;
    }

    private void refreshRecipeCacheIfNeeded(ItemStack seed) {
        if (seed.isEmpty()) {
            invalidateRecipeCache();
            return;
        }
        Item seedItem = seed.getItem();
        if (seedItem == cachedSeedItem && cachedRevision == AgritechEvolved.RECIPE_REVISION) return;

        invalidateRecipeCache();
        RecipeManager rm = getRecipes();
        if (rm == null) return;

        cachedSeedItem = seedItem;
        cachedRevision = AgritechEvolved.RECIPE_REVISION;
        SingleRecipeInput input = new SingleRecipeInput(seed);

        Optional<RecipeHolder<CropRecipe>> crop = rm.getRecipeFor(ATERecipeTypes.CROP_TYPE.get(), input, level);
        if (crop.isPresent()) { cachedCropRecipe = crop.get().value(); return; }

        Optional<RecipeHolder<TreeRecipe>> tree = rm.getRecipeFor(ATERecipeTypes.TREE_TYPE.get(), input, level);
        tree.ifPresent(h -> cachedTreeRecipe = h.value());
    }

    private Optional<CropRecipe> findCropRecipe(ItemStack seed) {
        if (seed.isEmpty()) return Optional.empty();
        refreshRecipeCacheIfNeeded(seed);
        return Optional.ofNullable(cachedCropRecipe);
    }

    private Optional<TreeRecipe> findTreeRecipe(ItemStack sapling) {
        if (sapling.isEmpty()) return Optional.empty();
        refreshRecipeCacheIfNeeded(sapling);
        return Optional.ofNullable(cachedTreeRecipe);
    }

    public boolean isValidPlant(ItemStack stack) {
        if (level == null) return false;
        return findCropRecipe(stack).isPresent() || findTreeRecipe(stack).isPresent();
    }

    private Set<Item> getValidSoils() {
        if (cachedValidSoils != null && soilCacheRevision == AgritechEvolved.RECIPE_REVISION) {
            return cachedValidSoils;
        }
        RecipeManager rm = getRecipes();
        if (rm == null) return Set.of();
        Set<Item> soils = new HashSet<>();
        for (RecipeHolder<?> holder : rm.getRecipes()) {
            if (holder.value().getType() == ATERecipeTypes.CROP_TYPE.get()) {
                for (Ingredient ing : ((CropRecipe) holder.value()).getSoils()) {
                    ing.items().map(Holder::value).forEach(soils::add);
                }
            } else if (holder.value().getType() == ATERecipeTypes.TREE_TYPE.get()) {
                for (Ingredient ing : ((TreeRecipe) holder.value()).getSoils()) {
                    ing.items().map(Holder::value).forEach(soils::add);
                }
            }
        }
        cachedValidSoils = soils;
        soilCacheRevision = AgritechEvolved.RECIPE_REVISION;
        return cachedValidSoils;
    }

    public boolean isValidSoilForAnyRecipe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return getValidSoils().contains(stack.getItem());
    }

    public boolean isValidPlantSoilCombination(ItemStack plant, ItemStack soil) {
        Optional<CropRecipe> crop = findCropRecipe(plant);
        if (crop.isPresent()) return crop.get().matchesSoil(soil);
        Optional<TreeRecipe> tree = findTreeRecipe(plant);
        if (tree.isPresent()) return tree.get().matchesSoil(soil);
        return false;
    }

    public boolean isTree() {
        return findTreeRecipe(getStack(SLOT_PLANT)).isPresent();
    }

    private static boolean isFertilizer(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem().builtInRegistryHolder().getData(ATEDataMaps.FERTILIZERS) != null;
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public int getMaxEnergyStored() {
        return Config.getPlanterEnergyBuffer();
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = Math.min(maxReceive, getMaxEnergyStored() - energyStored);
        if (!simulate) {
            energyStored += received;
            setChanged();
        }
        return received;
    }

    public EnergyHandler getEnergyStorage(@Nullable Direction side) {
        return new BEEnergyHandler(this);
    }

    private static class BEEnergyHandler extends SnapshotJournal<Integer> implements EnergyHandler {
        private final AdvancedPlanterBlockEntity be;

        BEEnergyHandler(AdvancedPlanterBlockEntity be) {
            this.be = be;
        }

        @Override
        protected Integer createSnapshot() {
            return be.energyStored;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            be.energyStored = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            be.setChanged();
        }

        @Override
        public long getAmountAsLong() {
            return be.energyStored;
        }

        @Override
        public long getCapacityAsLong() {
            return be.getMaxEnergyStored();
        }

        @Override
        public int insert(int amount, TransactionContext tx) {
            int received = Math.min(amount, be.getMaxEnergyStored() - be.energyStored);
            if (received <= 0) return 0;
            updateSnapshots(tx);
            be.energyStored += received;
            return received;
        }

        @Override
        public int extract(int amount, TransactionContext tx) {
            return 0;
        }
    }

    private boolean consumeEnergy() {
        int required = Math.round(Config.getPlanterBasePowerConsumption() * getModulePowerModifier());
        if (energyStored < required) return false;
        energyStored -= required;
        setChanged();
        return true;
    }

    public float getModuleSpeedModifier() {
        float speed = 1.0F, penalty = 1.0F;
        for (int slot = SLOT_MODULE_1; slot <= SLOT_MODULE_2; slot++) {
            String id = RegistryHelper.getItemId(getStack(slot));
            if (id.isEmpty()) continue;
            speed *= switch (id) {
                case SM_MK1 -> (float) Config.getSpeedModuleMk1Multiplier();
                case SM_MK2 -> (float) Config.getSpeedModuleMk2Multiplier();
                case SM_MK3 -> (float) Config.getSpeedModuleMk3Multiplier();
                default -> 1.0F;
            };
            penalty *= switch (id) {
                case YM_MK1 -> (float) Config.getYieldModuleMk1SpeedPenalty();
                case YM_MK2 -> (float) Config.getYieldModuleMk2SpeedPenalty();
                case YM_MK3 -> (float) Config.getYieldModuleMk3SpeedPenalty();
                default -> 1.0F;
            };
        }
        return speed * penalty;
    }

    public float getModuleYieldModifier() {
        float yield = 1.0F;
        for (int slot = SLOT_MODULE_1; slot <= SLOT_MODULE_2; slot++) {
            String id = RegistryHelper.getItemId(getStack(slot));
            yield *= switch (id) {
                case YM_MK1 -> (float) Config.getYieldModuleMk1Multiplier();
                case YM_MK2 -> (float) Config.getYieldModuleMk2Multiplier();
                case YM_MK3 -> (float) Config.getYieldModuleMk3Multiplier();
                default -> 1.0F;
            };
        }
        return yield;
    }

    public float getModulePowerModifier() {
        float power = 1.0F;
        for (int slot = SLOT_MODULE_1; slot <= SLOT_MODULE_2; slot++) {
            String id = RegistryHelper.getItemId(getStack(slot));
            power *= switch (id) {
                case SM_MK1 -> (float) Config.getSpeedModuleMk1PowerMultiplier();
                case SM_MK2 -> (float) Config.getSpeedModuleMk2PowerMultiplier();
                case SM_MK3 -> (float) Config.getSpeedModuleMk3PowerMultiplier();
                default -> 1.0F;
            };
        }
        return power;
    }

    private float getClocheGrowthModifier() {
        return getBlockState().getValue(AdvancedPlanterBlock.CLOCHED)
                ? (float) Config.getClocheSpeedMultiplier() : 1.0F;
    }

    private float getClocheYieldModifier() {
        return getBlockState().getValue(AdvancedPlanterBlock.CLOCHED)
                ? (float) Config.getClocheYieldMultiplier() : 1.0F;
    }

    private float getFertilizerGrowthModifier() {
        return getFertilizerModifier(true);
    }

    private float getFertilizerYieldModifier() {
        return getFertilizerModifier(false);
    }

    private float getFertilizerModifier(boolean forSpeed) {
        ItemStack stack = getStack(SLOT_FERTILIZER);
        if (stack.isEmpty()) return 1.0F;
        FertilizerData data = stack.getItem().builtInRegistryHolder().getData(ATEDataMaps.FERTILIZERS);
        return data != null ? (forSpeed ? data.speedMultiplier() : data.yieldMultiplier()) : 1.0F;
    }

    public float getSoilGrowthModifier(ItemStack soil) {
        if (soil.isEmpty()) return 1.0F;
        SoilModifierData data = soil.getItem().builtInRegistryHolder().getData(ATEDataMaps.SOIL_MODIFIERS);
        return data != null ? data.growthModifier() : 1.0F;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedPlanterBlockEntity be) {
        if (level.isClientSide()) return;

        boolean powered = be.energyStored > 0;
        if (state.getValue(AdvancedPlanterBlock.POWERED) != powered) {
            level.setBlock(pos, state.setValue(AdvancedPlanterBlock.POWERED, powered), 3);
        }

        ItemStack plant = be.getStack(SLOT_PLANT);
        ItemStack soil = be.getStack(SLOT_SOIL);

        if (plant.isEmpty() || soil.isEmpty()) {
            be.resetGrowth();
            return;
        }

        if (!be.isValidPlantSoilCombination(plant, soil)) {
            be.resetGrowth();
            return;
        }

        if (!be.readyToHarvest) {
            if (!be.consumeEnergy()) return;

            float totalModifier = be.getSoilGrowthModifier(soil)
                    * be.getModuleSpeedModifier()
                    * be.getFertilizerGrowthModifier()
                    * be.getClocheGrowthModifier();
            be.currentTotalModifier = totalModifier;

            int growthTime = Math.max(1, Math.round(Config.getAdvancedPlanterBaseProcessingTime() / totalModifier));

            PlanterProcessingTimeEvent timeEvent = new PlanterProcessingTimeEvent(be, plant, growthTime);
            NeoForge.EVENT_BUS.post(timeEvent);
            growthTime = timeEvent.getProcessingTime();

            be.growthTicks++;

            if (be.growthTicks >= growthTime) {
                be.readyToHarvest = true;
                be.growthProgress = 100;
                be.lastGrowthStage = be.getGrowthStage();
                level.sendBlockUpdated(pos, state, state, 3);
                be.setChanged();
            } else {
                be.growthProgress = (int) (be.growthTicks / (float) growthTime * 100);
                int stage = be.getGrowthStage();
                boolean stageChanged = stage != be.lastGrowthStage;
                if (stageChanged) {
                    be.lastGrowthStage = stage;
                }
                if (stageChanged || (be.isTree() && be.growthTicks % 10 == 0)) {
                    level.sendBlockUpdated(pos, state, state, 3);
                    be.setChanged();
                }
            }
        }

        if (be.readyToHarvest) {
            boolean hasSpace = be.hasOutputSpace();

            if (!hasSpace && level.getGameTime() % 10 == 0) {
                tryOutputItemsBelow(level, pos, be);
                hasSpace = be.hasOutputSpace();
            }

            if (hasSpace) {
                be.harvestPlant();
                tryOutputItemsBelow(level, pos, be);
            }
        }
    }

    private void resetGrowth() {
        growthProgress = 0;
        growthTicks = 0;
        readyToHarvest = false;
        lastGrowthStage = -1;
        setChanged();
    }

    public float getGrowthProgress() {
        return growthProgress / 100.0F;
    }

    public int getGrowthStage() {
        return isTree()
                ? (growthProgress > 50 ? 1 : 0)
                : Math.min(8, (int) (growthProgress / 12.5F));
    }

    private static void tryOutputItemsBelow(Level level, BlockPos pos, AdvancedPlanterBlockEntity be) {
        BlockPos below = pos.below();
        if (level.getBlockEntity(below) == null) return;

        ResourceHandler<ItemResource> target = level.getCapability(Capabilities.Item.BLOCK, below, Direction.UP);
        if (target == null) return;

        boolean changed = false;
        for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX; slot++) {
            ItemResource res = be.inventory.getResource(slot);
            if (res.isEmpty()) continue;
            int available = be.inventory.getAmountAsInt(slot);
            if (available <= 0) continue;

            try (Transaction tx = Transaction.openRoot()) {
                int insertable = target.insert(res, available, tx);
                if (insertable <= 0) continue;
                int extracted = be.inventory.extract(slot, res, insertable, tx);
                if (extracted != insertable) continue;
                tx.commit();
                changed = true;
            }
        }

        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (state.getValue(AdvancedPlanterBlock.CLOCHED)) {
            level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ATEItems.CLOCHE.get())));
        }
        drops();
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.size());
        for (int i = 0; i < inventory.size(); i++) {
            inv.setItem(i, getStack(i));
        }
        Containers.dropContents(level, worldPosition, inv);
    }

    public boolean hasOutputSpace() {
        List<ItemStack> drops = getHarvestDrops(getStack(SLOT_PLANT));
        Map<Integer, Integer> simAmounts = new HashMap<>();
        Map<Integer, Item> simItems = new HashMap<>();
        Map<Integer, Integer> simCap = new HashMap<>();

        for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX; slot++) {
            ItemStack s = getStack(slot);
            simAmounts.put(slot, s.getCount());
            simItems.put(slot, s.isEmpty() ? null : s.getItem());
            simCap.put(slot, s.isEmpty() ? 64 : s.getMaxStackSize());
        }

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();

            for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX && remaining > 0; slot++) {
                Item here = simItems.get(slot);
                if (here != null && here == drop.getItem()) {
                    int space = simCap.get(slot) - simAmounts.get(slot);
                    int toAdd = Math.min(space, remaining);
                    simAmounts.merge(slot, toAdd, Integer::sum);
                    remaining -= toAdd;
                }
            }

            for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX && remaining > 0; slot++) {
                if (simItems.get(slot) == null) {
                    simItems.put(slot, drop.getItem());
                    simAmounts.put(slot, remaining);
                    remaining = 0;
                }
            }

            if (remaining > 0) return false;
        }
        return true;
    }

    public void harvestPlant() {
        if (!readyToHarvest) return;

        PlanterPreHarvestEvent preEvent = new PlanterPreHarvestEvent(this, getStack(SLOT_PLANT));
        NeoForge.EVENT_BUS.post(preEvent);
        ItemStack seedForDrops = preEvent.getSeed();

        if (!ItemStack.matches(seedForDrops, getStack(SLOT_PLANT))) {
            try (Transaction tx = Transaction.openRoot()) {
                inventory.extract(SLOT_PLANT, ItemResource.of(getStack(SLOT_PLANT)), 1, tx);
                inventory.insert(SLOT_PLANT, ItemResource.of(seedForDrops), 1, tx);
                tx.commit();
            }
        }

        float yieldModifier = getFertilizerYieldModifier() * getModuleYieldModifier() * getClocheYieldModifier();
        List<ItemStack> drops = new ArrayList<>(applyYieldModifier(getHarvestDrops(seedForDrops), yieldModifier));

        PlanterPostHarvestEvent postEvent = new PlanterPostHarvestEvent(this, getStack(SLOT_PLANT), drops);
        NeoForge.EVENT_BUS.post(postEvent);
        drops = postEvent.getDrops();

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();
            ItemResource res = ItemResource.of(drop);

            for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX && remaining > 0; slot++) {
                ItemStack existing = getStack(slot);
                if (!existing.isEmpty() && existing.is(drop.getItem())) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    if (space <= 0) continue;
                    try (Transaction tx = Transaction.openRoot()) {
                        int inserted = inventory.insert(slot, res, Math.min(space, remaining), tx);
                        tx.commit();
                        remaining -= inserted;
                    }
                }
            }

            for (int slot = SLOT_OUTPUT_MIN; slot <= SLOT_OUTPUT_MAX && remaining > 0; slot++) {
                if (getStack(slot).isEmpty()) {
                    int toPlace = Math.min(remaining, drop.getMaxStackSize());
                    try (Transaction tx = Transaction.openRoot()) {
                        int inserted = inventory.insert(slot, res, toPlace, tx);
                        tx.commit();
                        remaining -= inserted;
                    }
                }
            }

            if (remaining > 0) break;
        }

        consumeFertilizer();
        resetGrowth();
    }

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    public void applyManualFertilizer(float speedMultiplier) {
        if (readyToHarvest) return;
        ItemStack soil = getStack(SLOT_SOIL);
        float totalMod = getSoilGrowthModifier(soil)
                * getModuleSpeedModifier()
                * getClocheGrowthModifier()
                * speedMultiplier;
        int growthTime = Math.max(1, Math.round(Config.getAdvancedPlanterBaseProcessingTime() / totalMod));
        int boost = Math.max(1, Math.round(growthTime * 0.1f * speedMultiplier));
        growthTicks = Math.min(growthTicks + boost, growthTime);
        growthProgress = (int) (growthTicks / (float) growthTime * 100);
        if (growthTicks >= growthTime) {
            readyToHarvest = true;
            growthProgress = 100;
        }
        setChanged();
    }

    private void consumeFertilizer() {
        ItemStack stack = getStack(SLOT_FERTILIZER);
        if (stack.isEmpty()) return;
        try (Transaction tx = Transaction.openRoot()) {
            inventory.extract(SLOT_FERTILIZER, ItemResource.of(stack), 1, tx);
            tx.commit();
        }
        setChanged();
    }

    private List<ItemStack> applyYieldModifier(List<ItemStack> drops, float modifier) {
        if (modifier == 1.0F) return drops;
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack drop : drops) {
            result.add(drop.copyWithCount(Math.max(1, Math.round(drop.getCount() * modifier))));
        }
        return result;
    }

    private List<ItemStack> getHarvestDrops(ItemStack plant) {
        if (plant.isEmpty()) return List.of();

        Optional<CropRecipe> crop = findCropRecipe(plant);
        List<DropEntry> entries = crop.map(CropRecipe::getDrops)
                .orElseGet(() -> findTreeRecipe(plant).map(TreeRecipe::getDrops).orElse(List.of()));

        List<ItemStack> drops = new ArrayList<>();
        Random rng = new Random();
        for (DropEntry entry : entries) {
            if (rng.nextFloat() <= entry.chance()) {
                int count = entry.max() > entry.min()
                        ? entry.min() + rng.nextInt(entry.max() - entry.min() + 1)
                        : entry.min();
                drops.add(new ItemStack(entry.item(), count));
            }
        }
        return drops;
    }

    public ResourceHandler<ItemResource> getInsertHandler() {
        return new ResourceHandler<>() {
            @Override
            public int size() {
                return inventory.size();
            }

            @Override
            public ItemResource getResource(int index) {
                return inventory.getResource(index);
            }

            @Override
            public long getAmountAsLong(int index) {
                return inventory.getAmountAsLong(index);
            }

            @Override
            public long getCapacityAsLong(int index, ItemResource resource) {
                return inventory.getCapacityAsLong(index, resource);
            }

            @Override
            public boolean isValid(int index, ItemResource resource) {
                if (resource.isEmpty()) return false;
                ItemStack stack = resource.toStack();
                return switch (index) {
                    case SLOT_PLANT -> isValidPlant(stack);
                    case SLOT_SOIL -> isValidSoilForAnyRecipe(stack);
                    case SLOT_FERTILIZER -> isFertilizer(stack);
                    default -> false;
                };
            }

            @Override
            public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
                if (index != SLOT_FERTILIZER) return 0;
                if (!isFertilizer(resource.toStack())) return 0;
                return inventory.insert(index, resource, amount, tx);
            }

            @Override
            public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
                return 0;
            }
        };
    }

    public ResourceHandler<ItemResource> getExtractHandler() {
        return new ResourceHandler<>() {
            @Override
            public int size() {
                return inventory.size();
            }

            @Override
            public ItemResource getResource(int index) {
                return inventory.getResource(index);
            }

            @Override
            public long getAmountAsLong(int index) {
                return inventory.getAmountAsLong(index);
            }

            @Override
            public long getCapacityAsLong(int index, ItemResource resource) {
                return inventory.getCapacityAsLong(index, resource);
            }

            @Override
            public boolean isValid(int index, ItemResource resource) {
                return false;
            }

            @Override
            public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
                return 0;
            }

            @Override
            public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
                if (index < SLOT_OUTPUT_MIN) return 0;
                return inventory.extract(index, resource, amount, tx);
            }
        };
    }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return side == Direction.DOWN ? getExtractHandler() : getInsertHandler();
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ATEBlockEntities.ADVANCED_PLANTER_BLOCK_BE.get(),
                (be, dir) -> be instanceof AdvancedPlanterBlockEntity p ? p.getItemHandler(dir) : null);
        event.registerBlockEntity(Capabilities.Energy.BLOCK, ATEBlockEntities.ADVANCED_PLANTER_BLOCK_BE.get(),
                (be, dir) -> be instanceof AdvancedPlanterBlockEntity p ? p.getEnergyStorage(dir) : null);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) level.invalidateCapabilities(getBlockPos());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) level.invalidateCapabilities(getBlockPos());
    }

    public ItemStack getStack(int slot) {
        ItemResource res = inventory.getResource(slot);
        if (res.isEmpty()) return ItemStack.EMPTY;
        return res.toStack(inventory.getAmountAsInt(slot));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output);
        output.putInt("growthProgress", growthProgress);
        output.putInt("growthTicks", growthTicks);
        output.putBoolean("readyToHarvest", readyToHarvest);
        output.putInt("energyStored", energyStored);
        output.putInt("lastGrowthStage", lastGrowthStage);
        output.putFloat("currentTotalModifier", currentTotalModifier);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input);
        growthProgress = input.getIntOr("growthProgress", 0);
        growthTicks = input.getIntOr("growthTicks", 0);
        readyToHarvest = input.getBooleanOr("readyToHarvest", false);
        energyStored = input.getIntOr("energyStored", 0);
        lastGrowthStage = input.getIntOr("lastGrowthStage", -1);
        currentTotalModifier = input.getFloatOr("currentTotalModifier", 1.0F);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}