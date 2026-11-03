package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.api.ArsNouveauAPI;
import net.diaithir.druidry.ars.api.block.IPedestalMachine;
import net.diaithir.druidry.ars.api.util.SourceUtil;
import net.diaithir.druidry.ars.client.particle.*;
import net.diaithir.druidry.ars.common.block.ArcanePlatform;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.crafting.recipes.ApparatusRecipeInput;
import net.diaithir.druidry.ars.common.crafting.recipes.IEnchantingRecipe;
import net.diaithir.druidry.ars.common.network.HighlightAreaPacket;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketOneShotAnimation;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.diaithir.druidry.ars.setup.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class EnchantingApparatusTile extends SingleItemTile implements Container, IPedestalMachine, ITickable {
    private int counter;
    public boolean isCrafting;
    public static final int craftingLength = 210;

    public EnchantingApparatusTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.ENCHANTING_APP_TILE.get(), pos, state);
    }

    @Override
    public void lightPedestal(Level level) {
        if (level != null) {
            for (BlockPos pos : pedestalList()) {
                ParticleUtil.spawnOrb(level, ParticleColor.makeRandomColor(255, 255, 255, level.getRandom()), pos.above(), 300);
            }
        }
    }

    @Override
    public void tick() {
        if (level.isClientSide()) {
            if (this.isCrafting) {
                Level world = getLevel();
                BlockPos pos = getBlockPos().offset(0, 0, 0);
                RandomSource rand = world.getRandom();

                Vec3 particlePos = new Vec3(pos.getX(), pos.getY(), pos.getZ()).add(0.5, 0, 0.5);
                particlePos = particlePos.add(ParticleUtil.pointInSphere());
                world.addParticle(ParticleLineData.createData(new ParticleColor(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))),
                        particlePos.x(), particlePos.y() + 1, particlePos.z(),
                        pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

                for (BlockPos p : pedestalList()) {
                    if (level.getBlockEntity(p) instanceof ArcanePedestalTile pedestalTile && pedestalTile.getStack() != null && !pedestalTile.getStack().isEmpty()) {
                        var yOffset = level.getBlockState(p).getBlock() instanceof ArcanePlatform ? 0.6 : 1.3;
                        getLevel().addParticle(
                                GlowParticleData.createData(new ParticleColor(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))),
                                p.getX() + 0.5 + ParticleUtil.inRange(-0.2, 0.2), p.getY() + yOffset + ParticleUtil.inRange(-0.3, 0.3), p.getZ() + 0.5 + ParticleUtil.inRange(-0.2, 0.2),
                                0, 0, 0);
                    }
                }

            }
            return;
        }

        if (isCrafting) {
            if (this.getRecipe(stack, null) == null) {
                this.isCrafting = false;
                setChanged();
            }
            counter += 1;
        }

        if (counter > craftingLength) {
            counter = 0;

            if (this.isCrafting) {
                IEnchantingRecipe recipe = this.getRecipe(stack, null);
                if (recipe != null) {
                    this.stack = recipe.assemble(new ApparatusRecipeInput(this), level.registryAccess());
                    clearItems();
                    setChanged();
                    ParticleUtil.spawnPoof((ServerLevel) level, worldPosition);
                    level.playSound(null, getBlockPos(), SoundRegistry.APPARATUS_FINISH.get(), SoundSource.BLOCKS, 1, 1);
                }

                this.isCrafting = false;
                setChanged();
            }
            updateBlock();
        }
    }


    public void clearItems() {
        for (BlockPos blockPos : pedestalList()) {
            if (level.getBlockEntity(blockPos) instanceof ArcanePedestalTile tile && tile.getStack() != null) {
                tile.setStack(net.diaithir.druidry.ars.common.util.PortUtil.remainder(tile.getStack()));
                BlockState state = level.getBlockState(blockPos);
                level.sendBlockUpdated(blockPos, state, state, 3);
                tile.setChanged();
            }
        }
    }

    public List<BlockPos> pedestalList() {
        return pedestalList(getBlockPos(), 3, getLevel());
    }

    public List<ItemStack> getPedestalItems() {
        ArrayList<ItemStack> pedestalItems = new ArrayList<>();
        for (BlockPos blockPos : pedestalList()) {
            if (level.getBlockEntity(blockPos) instanceof ArcanePedestalTile tile && tile.getStack() != null && !tile.getStack().isEmpty()) {
                pedestalItems.add(tile.getStack());
            }
        }
        return pedestalItems;
    }

    public IEnchantingRecipe getRecipe(ItemStack stack, @Nullable Player playerEntity) {
        List<ItemStack> pedestalItems = getPedestalItems();
        var holder = ArsNouveauAPI.getInstance().getEnchantingApparatusRecipes(level).stream().filter(r -> r.value().matches(new ApparatusRecipeInput(stack, pedestalItems, playerEntity), level)).findFirst().orElse(null);
        return holder != null ? holder.value() : null;
    }

    public boolean attemptCraft(ItemStack catalyst, @Nullable Player playerEntity) {
        if (isCrafting)
            return false;
        if (!craftingPossible(catalyst, playerEntity)) {
            return false;
        }
        IEnchantingRecipe recipe = this.getRecipe(catalyst, playerEntity);
        if (recipe.consumesSource())
            SourceUtil.takeSourceWithParticles(worldPosition, level, 10, recipe.sourceCost());
        this.isCrafting = true;
        updateBlock();
        Networking.sendToNearbyClient(level, worldPosition, new PacketOneShotAnimation(worldPosition));
        level.playSound(null, getBlockPos(), SoundRegistry.APPARATUS_CHANNEL.get(), SoundSource.BLOCKS, 1, 1);
        return true;
    }

    public boolean craftingPossible(ItemStack stack, Player playerEntity) {
        if (isCrafting || stack.isEmpty())
            return false;
        IEnchantingRecipe recipe = this.getRecipe(stack, playerEntity);
        if(recipe == null && playerEntity != null){
            List<ColorPos> colorPos = new ArrayList<>();
            for(BlockPos pos : pedestalList()){
                if(level.getBlockEntity(pos) instanceof ArcanePedestalTile tile){
                    colorPos.add(ColorPos.centeredAbove(tile.getBlockPos()));
                }
            }
            Networking.sendToNearbyClient(level, worldPosition, new HighlightAreaPacket(colorPos, 60));
        }

        return recipe != null && (!recipe.consumesSource() || (recipe.consumesSource() && SourceUtil.hasSourceNearby(worldPosition, level, 10, recipe.sourceCost())));
    }

    @Override
    public void loadAdditional(ValueInput compound) {
        super.loadAdditional(compound);
        isCrafting = compound.getBooleanOr("is_crafting", false);
        counter = compound.getIntOr("counter", 0);
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putBoolean("is_crafting", isCrafting);
        tag.putInt("counter", counter);
    }

    @Override
    public ItemStack getItem(int index) {
        if (isCrafting)
            return ItemStack.EMPTY;
        return super.getItem(index);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack newStack) {
        if (isCrafting || newStack.isEmpty())
            return false;
        return this.stack.isEmpty() && craftingPossible(newStack, null);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (isCrafting)
            return ItemStack.EMPTY;
        return super.removeItem(index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (isCrafting)
            return ItemStack.EMPTY;
        return super.removeItemNoUpdate(index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (isCrafting)
            return;
        super.setItem(index, stack);
        attemptCraft(stack, null);
    }



    @Override
    public double getBoneResetTime() {
        return 0;
    }


}
