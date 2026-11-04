package net.diaithir.creatureutilities.guards;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Village guard. Behaviour follows Guard Villagers Fabric by mrsterner (CC0, 1.20.1): melee guards that patrol near
 * home, defend villagers and players, and attack hostile mobs. Rewritten against 26.1.2 Mojang mappings.
 */
public class GuardEntity extends PathfinderMob {
    public GuardEntity(EntityType<? extends GuardEntity> type, Level level) {
        super(type, level);
        setCanPickUpLoot(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.5D)
            .add(Attributes.ATTACK_DAMAGE, 1.0D)
            .add(Attributes.FOLLOW_RANGE, 20.0D)
            .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true));
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.9D));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, GuardEntity.class, IronGolem.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.Mob.class, 5, true, false,
            (target, lvl) -> target instanceof Enemy && !(target instanceof net.minecraft.world.entity.monster.Creeper)));
    }

    /** Equip the starting kit; called on conversion. */
    public void giveStarterKit(ItemStack weapon) {
        setItemSlot(EquipmentSlot.MAINHAND, weapon.isEmpty() ? new ItemStack(Items.IRON_SWORD) : weapon);
        setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        for (EquipmentSlot s : EquipmentSlot.values()) setDropChance(s, 0.1F);
    }

    /** Convert a villager into a guard in place. Returns the guard or null. */
    public static GuardEntity convert(Villager v, ItemStack weapon) {
        GuardEntity g = Guards.GUARD.get().create(v.level(), net.minecraft.world.entity.EntitySpawnReason.CONVERSION);
        if (g == null) return null;
        g.snapTo(v.getX(), v.getY(), v.getZ(), v.getYRot(), v.getXRot());
        g.setCustomName(v.getCustomName());
        g.giveStarterKit(weapon);
        g.setHealth(g.getMaxHealth());
        g.setHomePos(v);
        v.level().addFreshEntity(g);
        v.discard();
        return g;
    }

    private void setHomePos(Villager v) {
        var home = v.getBrain().getMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.HOME);
        net.minecraft.core.BlockPos p = home.map(net.minecraft.core.GlobalPos::pos).orElse(blockPosition());
        setHomeTo(p, 24);
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount) {
        // Friendly fire from a player holding the line is ignored, as in the source mod.
        if (source.getEntity() instanceof GuardEntity) return false;
        return super.hurtServer(level, source, amount);
    }
}
