package net.diaithir.fauna;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The one mob class behind every plan; the species (stored as a string id, synced as an index) sets size, stats and behaviour. */
public class FaunaMob extends PathfinderMob {
    private static final EntityDataAccessor<Integer> SPECIES = SynchedEntityData.defineId(FaunaMob.class, EntityDataSerializers.INT);
    private static final Map<String, SoundEvent[]> SOUNDS = new ConcurrentHashMap<>();

    public final Plan plan;
    private boolean goalsSet;

    public FaunaMob(EntityType<? extends FaunaMob> type, Level level) {
        super(type, level);
        this.plan = planOf(type);
        if (plan.water()) {
            this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
            this.lookControl = new SmoothSwimmingLookControl(this, 10);
        } else if (plan.fly()) {
            this.moveControl = new FlyingMoveControl(this, 10, false);
        }
    }

    private static Plan planOf(EntityType<?> type) {
        return Plan.ALL.get(BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath());
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FLYING_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 2).add(Attributes.FOLLOW_RANGE, 24);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SPECIES, -1);
    }

    public @Nullable Species species() {
        int i = this.entityData.get(SPECIES);
        return i >= 0 && i < Species.ALL.size() ? Species.ALL.get(i) : null;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        Species s = SPECIES.equals(key) ? species() : null;
        if (s != null && level().isClientSide()) {
            setAttr(Attributes.SCALE, s.scale);
            refreshDimensions();
        }
    }

    public void setSpecies(Species s) {
        this.entityData.set(SPECIES, s.index);
        setAttr(Attributes.SCALE, s.scale);
        setAttr(Attributes.MAX_HEALTH, Mth.clamp(4 + Math.sqrt(s.kg) * 1.6, 4, 220));
        double speed = plan.water() ? 0.5 : 0.18 + (s.has(Species.F_FAST) ? 0.1 : 0) - Math.min(0.06, Math.log10(1 + s.kg) * 0.012);
        setAttr(Attributes.MOVEMENT_SPEED, speed);
        setAttr(Attributes.ATTACK_DAMAGE, Mth.clamp(Math.sqrt(s.kg) * 0.7, 1, 16));
        this.setHealth(this.getMaxHealth());
        this.refreshDimensions();
        if (!goalsSet && !level().isClientSide()) registerSpeciesGoals(s);
    }

    private void setAttr(net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> a, double v) {
        var inst = this.getAttribute(a);
        if (inst != null) inst.setBaseValue(v);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData group) {
        if (species() == null) {
            Species s = Habitat.pick(plan.name, level, blockPosition(), random);
            if (s == null && reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
                var all = Species.ofPlan(plan.name).stream().filter(x -> !x.fossil).toList();
                if (!all.isEmpty()) s = all.get(random.nextInt(all.size()));
            }
            if (s == null) {
                discard();
                return group;
            }
            setSpecies(s);
        }
        return super.finalizeSpawn(level, difficulty, reason, group);
    }

    @Override
    protected void registerGoals() {
        // goals depend on the species, so they are added in registerSpeciesGoals once it is known
    }

    private void registerSpeciesGoals(Species s) {
        goalsSet = true;
        boolean carn = s.diet.equals("carn") || s.diet.equals("pisc");
        boolean hunter = carn && s.kg >= 8 && !s.has(Species.F_SCAV);
        boolean fierce = s.has(Species.F_AGGRESSIVE) || (hunter && s.kg >= 60);
        goalSelector.addGoal(0, new FloatGoal(this));
        if (plan.water()) {
            if (fierce) goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
            else goalSelector.addGoal(1, new PanicGoal(this, 1.5));
            goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 40));
        } else if (plan.fly()) {
            goalSelector.addGoal(1, new PanicGoal(this, 1.4));
            goalSelector.addGoal(3, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        } else {
            if (fierce || hunter) goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15, true));
            else goalSelector.addGoal(1, new PanicGoal(this, 1.4));
            goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
        }
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        if (fierce || hunter) targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (fierce) targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        if (hunter) {
            targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, FaunaMob.class, true, (t, lvl) -> isPrey(t)));
        }
    }

    private boolean isPrey(net.minecraft.world.entity.LivingEntity t) {
        Species me = species();
        if (me == null || !(t instanceof FaunaMob o) || o.plan.water() != plan.water()) return false;
        Species them = o.species();
        return them != null && !them.diet.equals("carn") && them.kg * 2 < me.kg;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        Plan plan = planOf(getType()); // called from the super constructor, before this.plan is set
        if (plan.water()) return new WaterBoundPathNavigation(this, level);
        if (plan.fly()) {
            FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
            nav.setCanFloat(true);
            return nav;
        }
        return new GroundPathNavigation(this, level);
    }

    @Override
    public void travel(Vec3 input) {
        if (plan.water() && isInWater() && isEffectiveAi()) {
            moveRelative(getSpeed(), input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
            if (getTarget() == null) setDeltaMovement(getDeltaMovement().add(0, -0.005, 0));
        } else {
            super.travel(input);
        }
    }

    @Override
    public boolean isPushedByFluid() { return !plan.water(); }

    @Override
    public boolean canBreatheUnderwater() { return plan.water(); }

    @Override
    public void baseTick() {
        int air = getAirSupply();
        super.baseTick();
        if (plan.water() && isAlive()) {
            if (!isInWater()) {
                setAirSupply(air - 1);
                if (getAirSupply() <= -20) {
                    setAirSupply(0);
                    if (level() instanceof ServerLevel sl) hurtServer(sl, damageSources().drown(), 2);
                }
            } else {
                setAirSupply(300);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return !hasCustomName(); }

    @Override
    public int getMaxSpawnClusterSize() { return 6; }

    @Override
    protected Component getTypeName() {
        Species s = species();
        return s == null ? super.getTypeName() : Component.translatable("species.diaithirfauna." + s.id);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        super.addAdditionalSaveData(out);
        Species s = species();
        if (s != null) out.putString("Species", s.id);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        super.readAdditionalSaveData(in);
        Species s = Species.byId(in.getStringOr("Species", ""));
        if (s != null) setSpecies(s);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean byPlayer) {
        super.dropCustomDeathLoot(level, source, byPlayer);
        Species s = species();
        if (s == null) return;
        ItemStack carcass = FaunaButchery.carcass(s, this, level.getGameTime());
        if (carcass != null) {
            // diaithir-butchery present: one whole carcass, cut on a meat hook by the species' class (hide, feathers, bone, organs, meat by mass)
            spawnAtLocation(level, carcass);
            return;
        }
        // fallback without butchery: vanilla items; nothing from bodies too small to eat
        if (s.kg < 0.5) return;
        int n = Mth.clamp((int) Math.round(Math.sqrt(s.kg) / 3 * (1 + random.nextFloat())), 1, 12);
        ItemStack meat = new ItemStack(plan.water() ? Items.COD : plan.fly() || plan.name.equals("galliform") ? Items.CHICKEN : s.diet.equals("herb") ? Items.BEEF : Items.PORKCHOP, n);
        spawnAtLocation(level, meat);
        if (!plan.water() && s.kg >= 20) spawnAtLocation(level, new ItemStack(Items.LEATHER, Mth.clamp((int) (Math.sqrt(s.kg) / 5), 1, 8)));
        if (plan.fly()) spawnAtLocation(level, new ItemStack(Items.FEATHER, 1 + random.nextInt(3)));
        if (s.kg >= 100) spawnAtLocation(level, new ItemStack(Items.BONE, 1 + random.nextInt(3)));
    }

    // Sounds: the plan names a vanilla family; missing events fall back to the generic hurt and death sounds.
    private SoundEvent sound(int kind) {
        return SOUNDS.computeIfAbsent(plan.sound, fam -> {
            SoundEvent[] r = new SoundEvent[3];
            String[] ev = {"ambient", "hurt", "death"};
            for (int i = 0; i < 3; i++) {
                r[i] = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.withDefaultNamespace("entity." + fam + "." + ev[i]));
                if (r[i] == null && i > 0) r[i] = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.withDefaultNamespace("entity.generic." + (i == 1 ? "hurt" : "death")));
            }
            return r;
        })[kind];
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() { return sound(0); }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) { return sound(1); }

    @Override
    protected @Nullable SoundEvent getDeathSound() { return sound(2); }
}
