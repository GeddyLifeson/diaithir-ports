package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.api.block.IPedestalMachine;
import net.diaithir.druidry.ars.api.client.ITooltipProvider;
import net.diaithir.druidry.ars.api.imbuement_chamber.IImbuementRecipe;
import net.diaithir.druidry.ars.api.registry.ImbuementRecipeRegistry;
import net.diaithir.druidry.ars.api.source.AbstractSourceMachine;
import net.diaithir.druidry.ars.api.source.ISpecialSourceProvider;
import net.diaithir.druidry.ars.api.util.SourceUtil;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleLineData;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.crafting.recipes.ImbuementRecipe;
import net.diaithir.druidry.ars.common.entity.EntityFlyingItem;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class ImbuementTile extends AbstractSourceMachine implements Container, ITickable, ITooltipProvider, IPedestalMachine, RecipeInput {
    public ItemStack stack = ItemStack.EMPTY;
    public ItemEntity entity;
    public boolean draining;
    IImbuementRecipe recipe;
    int backoff;
    public float frames;
    boolean hasRecipe;
    int craftTicks;

    public ImbuementTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.IMBUEMENT_TILE.get(), pos, state);
    }

    @Override
    public int getTransferRate() {
        return 0;
    }

    @Override
    public void lightPedestal(Level level) {
        if (level != null) {
            for (BlockPos pos : pedestalList(getBlockPos(), 1, level)) {
                ParticleUtil.spawnOrb(level, ParticleColor.makeRandomColor(255, 255, 255, level.getRandom()), pos.above(), 300);
            }
        }
    }

    @Override
    public void tick() {
        if (level == null) return;
        if (level.isClientSide()) {

            int baseAge = draining ? 20 : 40;
            int randBound = draining ? 3 : 6;
            int numParticles = draining ? 2 : 1;
            float scaleAge = draining ? (float) ParticleUtil.inRange(0.1, 0.2) : (float) ParticleUtil.inRange(0.05, 0.15);
            if (level.getRandom().nextInt(randBound) == 0 && !Minecraft.getInstance().isPaused()) {
                for (int i = 0; i < numParticles; i++) {
                    Vec3 particlePos = new Vec3(getX(), getY(), getZ()).add(0.5, 0.5, 0.5);
                    particlePos = particlePos.add(ParticleUtil.pointInSphere());
                    level.addParticle(ParticleLineData.createData(new ParticleColor(255, 25, 180), scaleAge, baseAge + level.getRandom().nextInt(20)),
                            particlePos.x(), particlePos.y(), particlePos.z(),
                            getX() + 0.5, getY() + 0.5, getZ() + 0.5);
                }
            }
            if (!stack.isEmpty() && recipe == null) {
                var holder = getRecipeNow();
                this.recipe = holder != null ? holder.value() : null;
            }
            return;
        }
        this.hasRecipe = recipe != null;
        if (backoff > 0) {
            backoff--;
            return;
        }
        if (stack.isEmpty()) {
            return;
        }
        if (craftTicks > 0)
            craftTicks--;

        // Restore the recipe on world restart
        if (recipe == null) {
            var foundRecipe = getRecipeNow();
            if (foundRecipe != null) {
                this.recipe = foundRecipe.value();
                this.craftTicks = 100;
            }
        }

        if (recipe == null || !recipe.matches(this, level)) {
            backoff = 20;
            recipe = null;
            if (this.draining) {
                this.draining = false;
                updateBlock();
            }
            return;
        }

        int transferRate = 200;

        int cost = recipe.getSourceCost(this);

        if (this.level.getGameTime() % 20 == 0 && this.getSource() < cost) {
            if (!canAcceptSource(Math.min(200, cost)))
                return;

            ISpecialSourceProvider takePos = SourceUtil.takeSource(worldPosition, level, 2, Math.min(200, cost));
            if (takePos != null) {
                this.addSource(transferRate);
                EntityFlyingItem item = new EntityFlyingItem(level, takePos.getCurrentPos().above(), worldPosition, 255, 50, 80)
                        .withNoTouch();
                item.setDistanceAdjust(2f);
                level.addFreshEntity(item);
                if (!draining) {
                    draining = true;
                    updateBlock();
                }
            } else {
                this.addSource(10);
                if (draining) {
                    draining = false;
                    updateBlock();
                }
            }

        }

        if (this.getSource() >= cost && craftTicks <= 0) {
            this.setItem(0, recipe.assemble(this, this.level.registryAccess()).copy());
            this.addSource(-cost);
            ParticleUtil.spawnTouchPacket(level, worldPosition, ParticleColor.defaultParticleColor());
            updateBlock();
        }
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        stack = tag.read("itemStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);

        draining = tag.getBooleanOr("draining", false);
        this.hasRecipe = tag.getBooleanOr("hasRecipe", false);
        this.craftTicks = tag.getIntOr("craftTicks", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (!stack.isEmpty()) {
            tag.store("itemStack", ItemStack.OPTIONAL_CODEC, stack);
        }
        tag.putBoolean("draining", draining);
        tag.putBoolean("hasRecipe", hasRecipe);
        tag.putInt("craftTicks", craftTicks);
    }

    @Override
    public int getMaxSource() {
        return 10000000;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty() || !this.stack.isEmpty())
            return false;
        this.stack = stack.copy();
        var recipe = getRecipeNow();
        this.stack = ItemStack.EMPTY;
        return recipe != null;
    }

    @Override
    public boolean isEmpty() {
        return this.stack.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return stack;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack split = stack.split(count);
        updateBlock();
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack stack = this.stack.copy();
        this.stack = ItemStack.EMPTY;
        setChanged();
        return stack;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        this.stack = stack;
        this.craftTicks = 100;
        updateBlock();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.stack = ItemStack.EMPTY;
        updateBlock();
    }









    public List<ItemStack> getPedestalItems() {
        ArrayList<ItemStack> pedestalItems = new ArrayList<>();
        for (BlockPos p : getNearbyPedestals()) {
            if (level.getBlockEntity(p) instanceof ArcanePedestalTile pedestalTile && !pedestalTile.getStack().isEmpty()) {
                pedestalItems.add(pedestalTile.getStack());
            }
        }
        return pedestalItems;
    }

    public List<BlockPos> getNearbyPedestals() {
        return pedestalList(getBlockPos(), 1, getLevel());
    }

    public @Nullable RecipeHolder<? extends IImbuementRecipe> getRecipeNow() {
        return ImbuementRecipeRegistry.INSTANCE.getRecipes().stream().filter(r -> r.value().matches(this, level)).findFirst().orElse(null);
    }

    @Override
    public void getTooltip(List<Component> tooltip) {
        var holder = getRecipeNow();
        var recipe = holder == null ? null : holder.value();
        if(recipe != null && !recipe.getResultItem(this.level.registryAccess()).isEmpty() && stack != null && !stack.isEmpty()) {
            int cost = recipe.getSourceCost(this);
            tooltip.add(recipe.getCraftingText(this));
            if(cost > 0) {
                int progress = Math.min(100, (getSource() * 100 / cost));
                tooltip.add(recipe.getCraftingProgressText(this, progress));
            }
        }
    }
}
