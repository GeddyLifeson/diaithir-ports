package net.diaithir.druidry.ars.common.block.tile;

import net.diaithir.druidry.ars.api.client.ITooltipProvider;
import net.diaithir.druidry.ars.api.item.IWandable;
import net.diaithir.druidry.ars.api.recipe.CraftingManager;
import net.diaithir.druidry.ars.api.recipe.IRecipeWrapper;
import net.diaithir.druidry.ars.api.recipe.MultiRecipeWrapper;
import net.diaithir.druidry.ars.api.recipe.PotionCraftingManager;
import net.diaithir.druidry.ars.api.util.SourceUtil;
import net.diaithir.druidry.ars.client.particle.ColorPos;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.block.WixieCauldron;
import net.diaithir.druidry.ars.common.entity.EntityFollowProjectile;
import net.diaithir.druidry.ars.common.entity.EntityWixie;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.common.util.PotionUtil;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WixieCauldronTile extends SummoningTile implements ITooltipProvider, IWandable {

    public List<BlockPos> boundedInvs = new ArrayList<>();
    private ItemStack stackBeingCrafted = ItemStack.EMPTY;
    public int entityID;
    public boolean hasSource;
    public boolean isCraftingPotion;
    private boolean needsPotionStorage;
    public CraftingManager craftManager = new CraftingManager();
    private int craftCooldown; // We set a 1 tick cooldown to allow redstone updates to apply?

    public int craftingIndex;

    public WixieCauldronTile(BlockEntityType<? extends WixieCauldronTile> wixieCauldronType, BlockPos pos, BlockState state) {
        super(wixieCauldronType, pos, state);
    }

    public WixieCauldronTile(BlockPos pos, BlockState state) {
        this(BlockRegistry.WIXIE_CAULDRON_TYPE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide())
            return;
        if(craftCooldown > 0){
            craftCooldown--;
            return;
        }

        if (!hasSource && level.getGameTime() % 5 == 0 && SourceUtil.takeSourceWithParticles(worldPosition, level, 6, 50) != null) {
            this.hasSource = true;
            level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(WixieCauldron.FILLED, true));
            setChanged();
        }

        if (!hasSource) return;

        if(!level.isClientSide() && level.getGameTime() % 20 == 0){
            if(craftManager.isCraftCompleted()){
                rotateCraft();
            }
        }
    }

    @Override
    public void onFinishedConnectionLast(@Nullable BlockPos storedPos, @Nullable LivingEntity storedEntity, Player playerEntity) {
        if(storedPos == null)
            return;

        ResourceHandler<ItemResource> itemHandler = level.getCapability(Capabilities.Item.BLOCK, storedPos, null);
        if(itemHandler == null){
            return;
        }
        storedPos = storedPos.immutable();
        if(!this.boundedInvs.contains(storedPos)){
            this.boundedInvs.add(storedPos);
            PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.wixie_cauldron.bound"));
        }else{
            this.boundedInvs.remove(storedPos);
            PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.wixie_cauldron.removed"));
        }
        updateBlock();
    }

    @Override
    public void onWanded(Player playerEntity) {
        if(!this.boundedInvs.isEmpty()){
            this.boundedInvs = new ArrayList<>();
            PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.wixie_cauldron.cleared"));
            updateBlock();
        }
    }

    @Override
    public List<ColorPos> getWandHighlight(List<ColorPos> list) {
        for(BlockPos blockPos : boundedInvs){
            list.add(ColorPos.centered(blockPos,  ParticleColor.FROM_HIGHLIGHT));
        }
        return list;
    }

    /**
     * Picks the next recipe to craft collecting items from the pedestals and the cauldron
     */
    public void rotateCraft(){
        BlockPos leftBound = worldPosition.below().south().east();
        BlockPos rightBound = worldPosition.above().north().west();
        List<ItemStack> itemStacks = new ArrayList<>();
        for(BlockPos pos : BlockPos.betweenClosed(leftBound, rightBound)){
            if(level.getBlockEntity(pos) instanceof ArcanePedestalTile pedestalTile
                    && !pedestalTile.getStack().isEmpty()
                    && !pedestalTile.hasSignal){
                itemStacks.add(pedestalTile.getStack().copy());
            }
        }
        if(itemStacks.isEmpty())
            return;
        // Get the next recipe to craft
        if(this.craftingIndex >= itemStacks.size()){
            this.craftingIndex = 0;
        }
        ItemStack nextStack = itemStacks.get(this.craftingIndex);
        MultiRecipeWrapper recipeWrapper = getRecipesForStack(nextStack);
        craftingIndex++;
        if(recipeWrapper == null || recipeWrapper.isEmpty()){
            return;
        }
        Map<Item, Integer> count = getInventoryCount();

        IRecipeWrapper.InstructionsForRecipe instructions = recipeWrapper.canCraft(count, level, worldPosition);
        if(instructions == null)
            return;
        if (!recipeWrapper.isEmpty() && instructions.recipe().recipeIngredients.get(0).getCustomIngredient() instanceof DataComponentIngredient custom) {
            Ingredient itemIngred = instructions.recipe().recipeIngredients.get(1);
            List<ItemStack> needed = new ArrayList<>();
            itemIngred.items().forEach(holder -> needed.add(new ItemStack(holder.value())));
            PotionContents potionNeeded = null;
            for (var compEntry : custom.components().entrySet()) {
                if (compEntry.getKey() == DataComponents.POTION_CONTENTS && compEntry.getValue().isPresent()) {
                    potionNeeded = (PotionContents) compEntry.getValue().get();
                }
            }
            PotionContents potionOutput = PotionUtil.getContents(instructions.recipe().outputStack);
            boolean foundInput = potionNeeded.is(Potions.WATER) || findNeededPotion(potionNeeded, 300, level, worldPosition) != null;
            boolean foundRoomForOutput = findPotionStorage(level, worldPosition, potionOutput) != null;
            if(!foundRoomForOutput || !foundInput){
                return;
            }
            craftManager = new PotionCraftingManager(potionNeeded, needed, potionOutput);
            //BrewingRecipe
        } else {
            craftManager = new CraftingManager(instructions.recipe().outputStack.copy(), instructions.itemsNeeded());
        }
        onCraftStart();
        level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);
        stackBeingCrafted = nextStack.copy();
        updateBlock();
    }

    public boolean hasWixie() {
        return !this.converted || level.getEntity(entityID) != null;
    }

    public boolean isCraftingDone() {
        return craftManager.canBeCompleted();
    }

    public boolean needsPotion() {
        return craftManager instanceof PotionCraftingManager potionCraftingManager && potionCraftingManager.needsPotion();
    }

    public PotionContents getNeededPotion() {
        return craftManager instanceof PotionCraftingManager potionCraftingManager ? potionCraftingManager.getPotionNeeded() : null;
    }

    public void givePotion() {
        if(craftManager instanceof PotionCraftingManager potionCraftingManager) {
            potionCraftingManager.setObtainedPotion(true);
            level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);
        }
    }

    public boolean giveItem(ItemStack stack) {
        boolean res = craftManager.giveItem(stack.getItem());
        level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);
        return res;
    }

    public void attemptFinish() {
        if(!craftManager.canBeCompleted() || craftManager.isCraftCompleted())
            return;
        craftManager.completeCraft(this);
        this.craftCooldown = 1;
        this.stackBeingCrafted = ItemStack.EMPTY;
        updateBlock();
    }

    public MultiRecipeWrapper getRecipesForStack(ItemStack stack) {
        return MultiRecipeWrapper.fromStack(stack, level);
    }

    public static @Nullable BlockPos findPotionStorage(Level level, BlockPos worldPosition, PotionContents passedPot) {
        for(BlockPos bPos : BlockPos.withinManhattan(worldPosition.below(2), 4, 3, 4)){
            if (level.getBlockEntity(bPos) instanceof PotionJarTile tile && tile.canAccept(passedPot, 300)) {
                return bPos.immutable();
            }
        }
        return null;
    }

    public static @Nullable BlockPos findNeededPotion(PotionContents passedPot, int amount, Level level, BlockPos worldPosition) {
        for(BlockPos bPos : BlockPos.withinManhattan(worldPosition.below(2), 4, 3, 4)){
            if (level.getBlockEntity(bPos) instanceof PotionJarTile tile &&
                    tile.getAmount() >= amount &&
                    PotionUtil.arePotionContentsEqual(tile.getData(), passedPot)) {
                return bPos.immutable();
            }
        }
        return null;
    }

    public void convertedEffect() {
        super.convertedEffect();
        if (tickCounter >= 120 && !level.isClientSide()) {
            converted = true;
            level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(WixieCauldron.FILLED, false).setValue(SummoningTile.CONVERTED, true));
            EntityWixie wixie = new EntityWixie(level, worldPosition);
            wixie.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5);
            level.addFreshEntity(wixie);
            ParticleUtil.spawnPoof((ServerLevel) level, worldPosition.above());
            entityID = wixie.getId();
            tickCounter = 0;
            setChanged();
            return;
        }
        if (tickCounter % 10 == 0 && !level.isClientSide()) {
            RandomSource r = level.getRandom();
            int min = -2;
            int max = 2;
            EntityFollowProjectile proj1 = new EntityFollowProjectile(level, worldPosition.offset(r.nextInt(max - min) + min, 3, r.nextInt(max - min) + min), worldPosition, r.nextInt(255), r.nextInt(255), r.nextInt(255));
            level.addFreshEntity(proj1);
        }
    }

    private Map<Item, Integer> getInventoryCount() {
        List<BlockPos> stale = new ArrayList<>();
        Map<Item, Integer> itemsAvailable = new HashMap<>();
        if (boundedInvs.isEmpty())
            return itemsAvailable;
        for (BlockPos p : getInventories()) {
            BlockEntity blockEntity = level.getBlockEntity(p);
            if(blockEntity == null) {
                stale.add(p);
                continue;
            }
            ResourceHandler<ItemResource> handler = level.getCapability(Capabilities.Item.BLOCK, p, null);
            if(handler == null) {
                stale.add(p);
                continue;
            }

            for (int i = 0; i < handler.size(); i++) {
                ItemResource resource = handler.getResource(i);
                if (resource.isEmpty()) {
                    continue;
                }
                Item item = resource.getItem();
                int amount = handler.getAmountAsInt(i);
                if (!itemsAvailable.containsKey(item)) {
                    itemsAvailable.put(item, amount);
                    continue;
                }
                itemsAvailable.put(item, itemsAvailable.get(item) + amount);
            }
        }
        return itemsAvailable;
    }

    @Override
    protected void loadAdditional(ValueInput compound) {
        super.loadAdditional(compound);
        stackBeingCrafted = compound.read("currentCraft", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        CompoundTag managerTag = compound.read("craftManager", CompoundTag.CODEC).orElse(new CompoundTag());
        craftManager = CraftingManager.fromTag(compound.lookup(), managerTag);
        this.entityID = compound.getIntOr("entityid", 0);
        this.hasSource = compound.getBooleanOr("hasmana", false);
        this.isCraftingPotion = compound.getBooleanOr("isPotion", false);
        needsPotionStorage = compound.getBooleanOr("storage", false);
        craftingIndex = compound.getIntOr("craftingIndex", 0);
        boundedInvs = new ArrayList<>();
        compound.read("boundedInvs", BlockPos.CODEC.listOf()).ifPresent(list -> {
            for (BlockPos pos : list) {
                boundedInvs.add(pos);
            }
        });
        craftCooldown = compound.getIntOr("craftCooldown", 0);
    }

    @Override
    public void saveAdditional(ValueOutput compound) {
        super.saveAdditional(compound);

        if (!stackBeingCrafted.isEmpty()) {
            compound.store("currentCraft", ItemStack.CODEC, stackBeingCrafted);
        }
        if (craftManager != null) {
            CompoundTag managerTag = new CompoundTag();
            RegistryAccess access = level != null ? level.registryAccess() : RegistryAccess.EMPTY;
            craftManager.write(access, managerTag);
            compound.store("craftManager", CompoundTag.CODEC, managerTag);
        }

        compound.putInt("entityid", entityID);
        compound.putBoolean("hasmana", hasSource);
        compound.putBoolean("isPotion", isCraftingPotion);
        compound.putBoolean("storage", needsPotionStorage);
        compound.putInt("craftingIndex", craftingIndex);
        compound.store("boundedInvs", BlockPos.CODEC.listOf(), new ArrayList<>(boundedInvs));
        compound.putInt("craftCooldown", craftCooldown);
    }

    @Override
    public void getTooltip(List<Component> tooltip) {
        if(this.craftCooldown > 0)
            return;
        if (stackBeingCrafted.isEmpty()) {
            tooltip.add(Component.translatable("ars_nouveau.no_stack_crafting").withStyle(ChatFormatting.GOLD));
        }

        if (isOff) {
            tooltip.add(Component.translatable("ars_nouveau.tooltip.turned_off"));
        }

        tooltip.add(Component.translatable("ars_nouveau.cauldron.num_bounded", boundedInvs.size()));


        if (!stackBeingCrafted.isEmpty() && this.craftManager != null && !(this.craftManager instanceof PotionCraftingManager)) {
            tooltip.add(Component.literal(
                    Component.translatable("ars_nouveau.wixie.crafting").getString() +
                            Component.translatable(stackBeingCrafted.getItem().getDescriptionId()).getString())
            );
            if(stackBeingCrafted.getItem() == Items.POTION){
                PotionContents pc = PotionUtil.getContents(stackBeingCrafted);
                pc.addPotionTooltip(pc.getAllEffects(), tooltip::add, 1.0f, 20f);
            }
        } else if (this.craftManager instanceof PotionCraftingManager potionCraftingManager) {
            ItemStack potionStack = new ItemStack(Items.POTION);
            potionStack.set(DataComponents.POTION_CONTENTS, potionCraftingManager.potionOut);
            tooltip.add(Component.literal(Component.translatable("ars_nouveau.wixie.crafting").getString() + potionStack.getHoverName().getString()));
            potionCraftingManager.potionOut.addPotionTooltip(potionCraftingManager.potionOut.getAllEffects(), tooltip::add, 1.0f, 20f);
        }

        if (!hasSource) {
            tooltip.add(Component.translatable("ars_nouveau.wixie.need_mana").withStyle(ChatFormatting.GOLD));
        }
        if (this.craftManager != null && !this.craftManager.neededItems.isEmpty()) {
            ItemStack neededStack = this.craftManager.neededItems.get(0);
            tooltip.add(Component.literal(
                    Component.translatable("ars_nouveau.wixie.needs").getString() +
                            Component.translatable(neededStack.getItem().getDescriptionId()).getString()).withStyle(ChatFormatting.GOLD));
            if(neededStack.getItem() == Items.POTION){
                PotionContents contents = PotionUtil.getContents(neededStack);
                contents.addPotionTooltip(contents.getAllEffects(), tooltip::add, 1.0f, 20f);
            }
        }
        if (this.craftManager instanceof PotionCraftingManager potionCraftingManager && potionCraftingManager.needsPotion()) {
            ItemStack potionStack = new ItemStack(Items.POTION);
            potionStack.set(DataComponents.POTION_CONTENTS, potionCraftingManager.getPotionNeeded());
            tooltip.add(Component.literal(Component.translatable("ars_nouveau.wixie.needs").getString() + potionStack.getHoverName().getString()).withStyle(ChatFormatting.GOLD));
        }
        if (this.needsPotionStorage)
            tooltip.add(Component.translatable("ars_nouveau.wixie.needs_storage").withStyle(ChatFormatting.GOLD));

    }

    public List<BlockPos> getInventories() {
        return boundedInvs;
    }

    public boolean needsPotionStorage() {
        return needsPotionStorage;
    }

    public void setNeedsPotionStorage(boolean needsPotionStorage) {
        this.needsPotionStorage = needsPotionStorage;
        updateBlock();
    }


    /**
     * Called when the crafting is complete
     */
    public void onCraftingComplete() {
        level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(WixieCauldron.FILLED, false));
        level.playSound(null, worldPosition, SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.BLOCKS, 0.15f, 0.6f);
    }

    /**
     * Called when the crafting is about to start, if ingredients are available
     */
    public void onCraftStart() {
    }

}
