package com.misterd.agritechevolved.blockentity.custom;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.Config;
import com.misterd.agritechevolved.block.custom.PlanterBlock;
import com.misterd.agritechevolved.blockentity.ATEBlockEntities;
import com.misterd.agritechevolved.datamap.ATEDataMaps;
import com.misterd.agritechevolved.datamap.FertilizerData;
import com.misterd.agritechevolved.datamap.SoilModifierData;
import com.misterd.agritechevolved.gui.custom.BasicPlanterMenu;
import com.misterd.agritechevolved.item.ATEItems;
import com.misterd.agritechevolved.integration.PlanterPostHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterPreHarvestEvent;
import com.misterd.agritechevolved.integration.PlanterProcessingTimeEvent;
import com.misterd.agritechevolved.recipe.ATERecipeTypes;
import com.misterd.agritechevolved.recipe.CropRecipe;
import com.misterd.agritechevolved.recipe.DropEntry;
import com.misterd.agritechevolved.recipe.TreeRecipe;
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
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nullable;
import java.util.*;

public class PlanterBlockEntity extends BlockEntity implements MenuProvider {

    private @Nullable CropRecipe cachedCropRecipe = null;
    private @Nullable TreeRecipe cachedTreeRecipe = null;
    private @Nullable Item cachedSeedItem = null;
    private Set<Item> cachedValidSoils = null;
    private int soilCacheRevision = -1;
    private int cachedRevision = -1;

    public final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(15) {
        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return (index == 0 || index == 1) ? 1 : resource.toStack().getMaxStackSize();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (resource.isEmpty()) return false;
            ItemStack stack = resource.toStack();
            return switch (index) {
                case 0 -> isValidPlant(stack);
                case 1 -> isValidSoilForAnyRecipe(stack);
                case 2 -> isFertilizer(stack);
                default -> true;
            };
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            if (index == 0) invalidateRecipeCache();
            PlanterBlockEntity.this.setChanged();
            Level lvl = PlanterBlockEntity.this.level;
            if (lvl != null && !lvl.isClientSide()) {
                lvl.sendBlockUpdated(
                        PlanterBlockEntity.this.getBlockPos(),
                        PlanterBlockEntity.this.getBlockState(),
                        PlanterBlockEntity.this.getBlockState(),
                        3
                );
            }
        }
    };

    public int growthProgress = 0;
    public int growthTicks = 0;
    private boolean readyToHarvest = false;
    private int lastGrowthStage = -1;

    public PlanterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATEBlockEntities.PLANTER_BLOCK_BE.get(), pos, blockState);
    }

    private void invalidateRecipeCache() {
        cachedCropRecipe = null;
        cachedTreeRecipe = null;
        cachedSeedItem = null;
        cachedRevision = -1;
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
        return findTreeRecipe(getStack(0)).isPresent();
    }

    private static boolean isFertilizer(ItemStack stack) {
        return stack.getItem().builtInRegistryHolder().getData(ATEDataMaps.FERTILIZERS) != null;
    }

    private float getClocheGrowthModifier(BlockState state) {
        return state.getValue(PlanterBlock.CLOCHED) ? (float) Config.getClocheSpeedMultiplier() : 1.0F;
    }

    private float getClocheYieldModifier(BlockState state) {
        return state.getValue(PlanterBlock.CLOCHED) ? (float) Config.getClocheYieldMultiplier() : 1.0F;
    }

    private float getFertilizerGrowthModifier() {
        ItemStack stack = getStack(2);
        if (stack.isEmpty()) return 1.0F;
        FertilizerData data = stack.getItem().builtInRegistryHolder().getData(ATEDataMaps.FERTILIZERS);
        return data != null ? data.speedMultiplier() : 1.0F;
    }

    private float getFertilizerYieldModifier() {
        ItemStack stack = getStack(2);
        if (stack.isEmpty()) return 1.0F;
        FertilizerData data = stack.getItem().builtInRegistryHolder().getData(ATEDataMaps.FERTILIZERS);
        return data != null ? data.yieldMultiplier() : 1.0F;
    }

    public float getSoilGrowthModifier(ItemStack soilStack) {
        if (soilStack.isEmpty()) return 1.0F;
        SoilModifierData data = soilStack.getItem().builtInRegistryHolder().getData(ATEDataMaps.SOIL_MODIFIERS);
        return data != null ? data.growthModifier() : 1.0F;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PlanterBlockEntity be) {
        if (level.isClientSide()) return;

        ItemStack plantStack = be.getStack(0);
        ItemStack soilStack = be.getStack(1);

        if (plantStack.isEmpty() || soilStack.isEmpty()) {
            be.resetGrowth();
            return;
        }

        if (!be.isValidPlantSoilCombination(plantStack, soilStack)) {
            be.resetGrowth();
            return;
        }

        if (!be.readyToHarvest) {
            float soilMod = be.getSoilGrowthModifier(soilStack);
            float fertMod = be.getFertilizerGrowthModifier();
            float clocheMod = be.getClocheGrowthModifier(state);
            float totalMod = soilMod * fertMod * clocheMod;
            int baseTime = Config.getPlanterBaseProcessingTime();
            int adjustedTime = Math.max(1, Math.round((float) baseTime / totalMod));

            PlanterProcessingTimeEvent timeEvent = new PlanterProcessingTimeEvent(be, plantStack, adjustedTime);
            NeoForge.EVENT_BUS.post(timeEvent);
            adjustedTime = timeEvent.getProcessingTime();

            be.growthTicks++;

            if (be.growthTicks >= adjustedTime) {
                be.readyToHarvest = true;
                be.growthProgress = 100;
                be.lastGrowthStage = be.getGrowthStage();
                level.sendBlockUpdated(pos, state, state, 3);
                be.setChanged();
            } else {
                be.growthProgress = (int) ((float) be.growthTicks / adjustedTime * 100.0F);
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
                be.harvestPlant(state);
                tryOutputItemsBelow(level, pos, be);
            }
        }
    }

    private void resetGrowth() {
        this.growthProgress = 0;
        this.growthTicks = 0;
        this.readyToHarvest = false;
        this.lastGrowthStage = -1;
        this.setChanged();
    }

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    public void harvestPlant(BlockState state) {
        if (!readyToHarvest) return;

        PlanterPreHarvestEvent preEvent = new PlanterPreHarvestEvent(this, getStack(0));
        NeoForge.EVENT_BUS.post(preEvent);
        ItemStack seedForDrops = preEvent.getSeed();

        if (!ItemStack.matches(seedForDrops, getStack(0))) {
            try (Transaction tx = Transaction.openRoot()) {
                inventory.extract(0, ItemResource.of(getStack(0)), 1, tx);
                inventory.insert(0, ItemResource.of(seedForDrops), 1, tx);
                tx.commit();
            }
        }

        float fertYield = getFertilizerYieldModifier();
        float clocheYield = getClocheYieldModifier(state);
        List<ItemStack> drops = new ArrayList<>(applyYieldModifier(getHarvestDrops(seedForDrops), fertYield * clocheYield));

        PlanterPostHarvestEvent postEvent = new PlanterPostHarvestEvent(this, getStack(0), drops);
        NeoForge.EVENT_BUS.post(postEvent);
        drops = postEvent.getDrops();

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();
            ItemResource res = ItemResource.of(drop);

            for (int slot = 3; slot <= 14 && remaining > 0; slot++) {
                ItemStack existing = getStack(slot);
                if (!existing.isEmpty() && existing.is(drop.getItem())) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    if (space <= 0) continue;
                    int toAdd = Math.min(space, remaining);
                    try (Transaction tx = Transaction.openRoot()) {
                        int inserted = inventory.insert(slot, res, toAdd, tx);
                        tx.commit();
                        remaining -= inserted;
                    }
                }
            }

            for (int slot = 3; slot <= 14 && remaining > 0; slot++) {
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

    public void applyManualFertilizer(float speedMultiplier) {
        if (readyToHarvest) return;
        ItemStack plantStack = getStack(0);
        ItemStack soilStack = getStack(1);
        if (plantStack.isEmpty() || soilStack.isEmpty()) return;

        float soilMod = getSoilGrowthModifier(soilStack);
        float clocheMod = getClocheGrowthModifier(getBlockState());
        int adjustedTime = Math.max(1, Math.round(Config.getPlanterBaseProcessingTime() / (soilMod * clocheMod)));

        int boost = Math.max(1, Math.round(adjustedTime * 0.25F * speedMultiplier));
        growthTicks = Math.min(adjustedTime, growthTicks + boost);
        growthProgress = (int) ((float) growthTicks / adjustedTime * 100.0F);

        if (growthTicks >= adjustedTime) {
            readyToHarvest = true;
            growthProgress = 100;
        }

        lastGrowthStage = getGrowthStage();
        setChanged();
    }

    private void consumeFertilizer() {
        ItemStack stack = getStack(2);
        if (stack.isEmpty()) return;
        try (Transaction tx = Transaction.openRoot()) {
            inventory.extract(2, ItemResource.of(stack), 1, tx);
            tx.commit();
        }
        setChanged();
    }

    private List<ItemStack> applyYieldModifier(List<ItemStack> drops, float mod) {
        if (mod == 1.0F) return drops;
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack drop : drops) {
            out.add(drop.copyWithCount(Math.max(1, Math.round(drop.getCount() * mod))));
        }
        return out;
    }

    private List<ItemStack> getHarvestDrops(ItemStack plantStack) {
        if (plantStack.isEmpty()) return List.of();

        Optional<CropRecipe> crop = findCropRecipe(plantStack);
        List<DropEntry> entries = crop.map(CropRecipe::getDrops)
                .orElseGet(() -> findTreeRecipe(plantStack).map(TreeRecipe::getDrops).orElse(List.of()));

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

    private static void tryOutputItemsBelow(Level level, BlockPos pos, PlanterBlockEntity be) {
        BlockPos below = pos.below();
        if (level.getBlockEntity(below) == null) return;

        ResourceHandler<ItemResource> target = level.getCapability(Capabilities.Item.BLOCK, below, Direction.UP);
        if (target == null) return;

        boolean changed = false;
        for (int slot = 3; slot <= 14; slot++) {
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

    public boolean hasOutputSpace() {
        List<ItemStack> drops = getHarvestDrops(getStack(0));

        Map<Integer, Integer> simAmounts = new HashMap<>();
        Map<Integer, Item> simItems = new HashMap<>();
        Map<Integer, Integer> simCapacity = new HashMap<>();

        for (int slot = 3; slot <= 14; slot++) {
            ItemStack s = getStack(slot);
            simAmounts.put(slot, s.getCount());
            simItems.put(slot, s.isEmpty() ? null : s.getItem());
            simCapacity.put(slot, s.isEmpty() ? 64 : s.getMaxStackSize());
        }

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();

            for (int slot = 3; slot <= 14 && remaining > 0; slot++) {
                Item here = simItems.get(slot);
                if (here != null && here == drop.getItem()) {
                    int space = simCapacity.get(slot) - simAmounts.get(slot);
                    int toAdd = Math.min(space, remaining);
                    simAmounts.merge(slot, toAdd, Integer::sum);
                    remaining -= toAdd;
                }
            }

            for (int slot = 3; slot <= 14 && remaining > 0; slot++) {
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

    public ResourceHandler<ItemResource> getInsertHandler() {
        return new ResourceHandler<>() {
            @Override
            public int size() { return inventory.size(); }

            @Override
            public ItemResource getResource(int index) { return inventory.getResource(index); }

            @Override
            public long getAmountAsLong(int index) { return inventory.getAmountAsLong(index); }

            @Override
            public long getCapacityAsLong(int index, ItemResource resource) {
                return inventory.getCapacityAsLong(index, resource);
            }

            @Override
            public boolean isValid(int index, ItemResource resource) {
                if (resource.isEmpty()) return false;
                ItemStack stack = resource.toStack();
                return switch (index) {
                    case 0 -> isValidPlant(stack);
                    case 1 -> isValidSoilForAnyRecipe(stack);
                    case 2 -> isFertilizer(stack);
                    default -> true;
                };
            }

            @Override
            public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
                if (index != 2) return 0;
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
            public int size() { return inventory.size(); }

            @Override
            public ItemResource getResource(int index) { return inventory.getResource(index); }

            @Override
            public long getAmountAsLong(int index) { return inventory.getAmountAsLong(index); }

            @Override
            public long getCapacityAsLong(int index, ItemResource resource) {
                return inventory.getCapacityAsLong(index, resource);
            }

            @Override
            public boolean isValid(int index, ItemResource resource) { return false; }

            @Override
            public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
                return 0;
            }

            @Override
            public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
                if (index < 3) return 0;
                return inventory.extract(index, resource, amount, tx);
            }
        };
    }

    public ItemStack getStack(int slot) {
        ItemResource res = inventory.getResource(slot);
        if (res.isEmpty()) return ItemStack.EMPTY;
        return res.toStack(inventory.getAmountAsInt(slot));
    }

    public float getGrowthProgress() {
        return growthProgress / 100.0F;
    }

    public int getGrowthStage() {
        if (isTree()) return growthProgress > 50 ? 1 : 0;
        return Math.min(8, (int) (growthProgress / 12.5F));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (state.getValue(PlanterBlock.CLOCHED)) {
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

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output);
        output.putInt("growthProgress", growthProgress);
        output.putInt("growthTicks", growthTicks);
        output.putBoolean("readyToHarvest", readyToHarvest);
        output.putInt("lastGrowthStage", lastGrowthStage);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input);
        growthProgress = input.getIntOr("growthProgress", 0);
        growthTicks = input.getIntOr("growthTicks", 0);
        readyToHarvest = input.getBooleanOr("readyToHarvest", false);
        lastGrowthStage = input.getIntOr("lastGrowthStage", -1);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.agritechevolved.basic_planter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BasicPlanterMenu(i, inventory, this);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
}