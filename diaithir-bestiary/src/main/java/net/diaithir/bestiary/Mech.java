package net.diaithir.bestiary;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** A walker any player can climb into and pilot. With a fuel entry it only moves while it has fuel, which burns while it is crewed. */
public class Mech extends Creature {
    private static final EntityDataAccessor<Integer> FUEL = SynchedEntityData.defineId(Mech.class, EntityDataSerializers.INT);

    public Mech(EntityType<? extends Mech> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(FUEL, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8F));
    }

    public int fuel() {
        return entityData.get(FUEL);
    }

    public boolean needsFuel() {
        return def() != null && def().fuel().isPresent();
    }

    @Override
    public boolean canBeRiddenBy(Player player) {
        return def() != null && !isVehicle();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (needsFuel() && stack.is(def().fuel().get().item())) {
            if (!level().isClientSide()) {
                stack.consume(1, player);
                entityData.set(FUEL, fuel() + def().fuel().get().ticks());
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected Vec3 getRiddenInput(Player controller, Vec3 selfInput) {
        return needsFuel() && fuel() <= 0 ? Vec3.ZERO : super.getRiddenInput(controller, selfInput);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && isVehicle() && fuel() > 0) entityData.set(FUEL, fuel() - 1);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Fuel", fuel());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(FUEL, input.getIntOr("Fuel", 0));
    }
}
