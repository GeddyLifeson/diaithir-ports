package net.diaithir.goety.entity.servants;

import net.diaithir.goety.entity.Servant;
import net.diaithir.goety.entity.ServantCore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A stray raised by a rite and bound to its caster. */
public class StrayServant extends Stray implements Servant {
    private ServantCore core; // created on first use: registerGoals runs inside the super constructor

    public StrayServant(EntityType<? extends Stray> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    @Override public ServantCore core() { if (core == null) core = new ServantCore(this); return core; }

    @Override protected void registerGoals() {
        super.registerGoals();
        core().bindGoals(this.goalSelector, this.targetSelector);
    }

    @Override public boolean canAttack(LivingEntity target) { return !core().isFriend(target) && super.canAttack(target); }
    @Override protected boolean shouldDropLoot(ServerLevel level) { return false; }
    @Override public boolean shouldDropExperience() { return false; }
    @Override public void tick() { super.tick(); core().tick(); }
    @Override protected void addAdditionalSaveData(ValueOutput out) { super.addAdditionalSaveData(out); core().save(out); }
    @Override protected void readAdditionalSaveData(ValueInput in) { super.readAdditionalSaveData(in); core().load(in); }

}
