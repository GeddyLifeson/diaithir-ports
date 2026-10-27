package com.diaithir.addons.newage.content.energising;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.diaithir.addons.newage.CNARecipeTypes;
import com.diaithir.addons.newage.content.energising.recipe.EnergisingRecipe;
import com.diaithir.addons.newage.esl.EnergyHelper;
import com.diaithir.addons.newage.esl.EnergyStorage;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import com.diaithir.addons.newage.esl.TransactionStack;
import org.joml.Math;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnergiserBehaviour extends BeltProcessingBehaviour {
    protected int tier;
    protected EnergiserBlockEntity be;
    public boolean capacitorMode = false;

    public EnergiserBehaviour(EnergiserBlockEntity be) {
        super(be);
        whenItemEnters(this::itemEnter);
        whileItemHeld(this::itemHeld);
        this.be = be;
    }

    public EnergisingRecipe getRecipe(ItemStack stack) {
        if (be.getLevel() == null) {
            return null;
        }

        if (!(be.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel))
            return null;

        return serverLevel.recipeAccess()
                .getRecipeFor(CNARecipeTypes.ENERGISING.get(), new net.minecraft.world.item.crafting.SingleRecipeInput(stack), serverLevel)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    public EnergisingRecipe currentRecipe;
    public long charged;
    public long needed;

    private boolean shouldCreateParticles = false;

    @Override
    public void read(ValueInput nbt, boolean clientPacket) {
        charged = nbt.getLongOr("charged", 0L);
        needed = nbt.getLongOr("needed", 0L);
        shouldCreateParticles = nbt.getBooleanOr("shouldCreateParticles", false);
        capacitorMode = nbt.getBooleanOr("capacitorModer", false);
        super.read(nbt, clientPacket);
    }

    @Override
    public void write(ValueOutput nbt, boolean clientPacket) {
        nbt.putLong("charged", charged);
        nbt.putLong("needed", needed);
        nbt.putBoolean("shouldCreateParticles",shouldCreateParticles);
        nbt.putBoolean("capacitorMode", capacitorMode);
        if (clientPacket)
            shouldCreateParticles = false;
        super.write(nbt, clientPacket);
    }

    public long sinceUpdate = 0;

    @Override
    public void tick() {
        super.tick();
        if (be.getLevel() == null) {
            return;
        }
        if (!be.getLevel().isClientSide() && needed > 0) {
            sinceUpdate--;
            if (sinceUpdate <= 0) {
                needed = 0;
                be.getEnergyStorage().internalInsert(charged, false);
                charged = 0;
                currentRecipe = null;
                capacitorMode = false;
                blockEntity.sendData();
            }
        }

        if (!capacitorMode) {
            be.lastCharged = -1;

            if (needed > 0) {
                be.lastCharged = be.getEnergyStorage().internalExtract(
                        (long) Math.min(eSpeed(),
                                needed - charged), false);
                charged += be.lastCharged;
            }
        }

        if (be.getLevel().isClientSide()) {
            be.size -= 0.15f;
            be.size = Math.clamp(0, 1, be.size);
            if (needed > 0 && charged > 0) {
                be.size = (float) Math.lerp(be.size, (float) charged / needed, 0.6);
            } else if(shouldCreateParticles) {
                shouldCreateParticles = false;
                var rand = be.getLevel().getRandom();
                for (int i = 0 ; i < 6 ; i++) {
                    be.getLevel().addAlwaysVisibleParticle(ParticleTypes.GLOW,
                            be.getBlockPos().getX() + 0.5 + (rand.nextFloat() - 0.5) * 0.4,
                            be.getBlockPos().getY() - 1.4 + (rand.nextFloat() - 0.5) * 0.4,
                            be.getBlockPos().getZ() + 0.5 + (rand.nextFloat() - 0.5) * 0.4,
                            0.0, 0.5, 0.0);

                }
                be.getLevel().playLocalSound(be.getBlockPos().getX() + 0.5,
                        be.getBlockPos().getY() - 1.4,
                        be.getBlockPos().getZ() + 0.5,
                        SoundEvents.ENDER_EYE_DEATH, SoundSource.BLOCKS, 0.3f, 2.0f, true);
            }
        }
    }

    private ProcessingResult itemHeld(TransportedItemStack transportedItemStack, TransportedItemStackHandlerBehaviour handler) {
        if (currentRecipe == null) {
            currentRecipe = getRecipe(transportedItemStack.stack);
        }
        if (be.getSpeed() == 0 || (currentRecipe == null && !capacitorMode) || be.getLevel() == null) {
            return ProcessingResult.PASS;
        }
        if (capacitorMode) {
            EnergyStorage itemStorage = EnergyStorage.findForItem(transportedItemStack.stack);
            
            if (itemStorage != null) {
                try (Transaction t = TransactionStack.get().openOuter()) {
                    be.lastCharged = EnergyHelper.moveEnergy(be.getEnergyStorage(), itemStorage, eSpeed(), t);

                    charged = itemStorage.getStoredEnergy();
                    needed = itemStorage.getCapacity();
                    sinceUpdate = 10;

                    blockEntity.sendData();


                    if (charged >= needed) {
                        capacitorMode = false;
                        charged = 0;
                        needed = 0;
                        shouldCreateParticles = true;
                        return ProcessingResult.PASS;
                    }
                    
                    t.commit();
                }

                return ProcessingResult.HOLD;
            }

            capacitorMode = false;
            return ProcessingResult.PASS;
        }

        int count = transportedItemStack.stack.getCount();
        needed = (long) count * currentRecipe.getEnergyNeeded();
        sinceUpdate = 10;

        if (charged >= needed) {
            List<TransportedItemStack> out = currentRecipe.assemble(new net.minecraft.world.item.crafting.SingleRecipeInput(transportedItemStack.stack), be.getLevel().getRandom()).stream()
                    .map(stack -> {
                        TransportedItemStack copy = transportedItemStack.copy();
                        boolean centered = BeltHelper.isItemUpright(stack);
                        stack.setCount(stack.getCount() * count);
                        copy.stack = stack;
                        copy.angle = centered ? 180 : be.getLevel().getRandom().nextInt(360);
                        return copy;
                    })
                    .collect(Collectors.toList());

            if (charged > needed) {
                be.getEnergyStorage().internalInsert(charged - needed, false);
            }

            charged = 0;
            needed = 0;

            if (out.isEmpty()) {
                handler.handleProcessingOnItem(transportedItemStack,
                        TransportedItemStackHandlerBehaviour.TransportedResult.removeItem());
            } else {
                handler.handleProcessingOnItem(transportedItemStack,
                        TransportedItemStackHandlerBehaviour.TransportedResult.convertTo(out));
            }
            shouldCreateParticles = true;
            blockEntity.sendData();
            currentRecipe = null;
        }

        return ProcessingResult.HOLD;
    }

    private long eSpeed() {
        return EnergiserBlock.getStrength(be.tier) * (long) Math.abs(be.getSpeed() * 0.1);
    }

    private ProcessingResult itemEnter(TransportedItemStack transportedItemStack, TransportedItemStackHandlerBehaviour transportedItemStackHandlerBehaviour) {
        if (be.getSpeed() == 0) {
            return ProcessingResult.PASS;
        }

        EnergyStorage itemStorage = EnergyStorage.findForItem(transportedItemStack.stack);

        if (itemStorage != null && itemStorage.getStoredEnergy() < itemStorage.getCapacity()) {
            capacitorMode = true;
            charged = itemStorage.getStoredEnergy();
            needed = itemStorage.getCapacity();
            sinceUpdate = 10;
            return ProcessingResult.HOLD;
        }
        
        capacitorMode = false;

        currentRecipe = getRecipe(transportedItemStack.stack);
        sinceUpdate = 10;
        if (currentRecipe == null)
            return ProcessingResult.PASS;

        return ProcessingResult.HOLD;
    }

}
