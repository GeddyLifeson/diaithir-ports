package net.diaithir.bosses;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * The one body all planet bosses share. Which boss it is lives in the synced {@code look} (a planet boss profile id): the client
 * builds that boss's own model and texture from it. The id is also kept as an entity tag, so it survives a reload without NBT code.
 */
public final class PlanetBoss extends Monster {
    public static final String TAG = DiaithirBosses.ID + ":pb:";
    private static final EntityDataAccessor<String> LOOK = SynchedEntityData.defineId(PlanetBoss.class, EntityDataSerializers.STRING);

    private static final EntityDataAccessor<String> ANIM = SynchedEntityData.defineId(PlanetBoss.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> ANIM_PHASE = SynchedEntityData.defineId(PlanetBoss.class, EntityDataSerializers.INT);
    private long animStart;

    public PlanetBoss(EntityType<? extends PlanetBoss> type, Level level) { super(type, level); }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LOOK, "");
        builder.define(ANIM, "");
        builder.define(ANIM_PHASE, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** Attack animation for the client: type (slam, rewind...) and phase (0 none, 1 wind-up, 2 strike). Set by the BossController. */
    public void playAnim(String type, int phase) {
        this.entityData.set(ANIM, type == null ? "" : type);
        this.entityData.set(ANIM_PHASE, phase);
        this.animStart = this.level().getGameTime();
    }

    public String animType() { return this.entityData.get(ANIM); }

    public int animPhase() { return this.entityData.get(ANIM_PHASE); }

    public long animStart() { return animStart; }

    public String lookId() { return this.entityData.get(LOOK); }

    public void setLook(String id) {
        this.entityData.set(LOOK, id);
        this.addTag(TAG + id);
    }

    /** The boss id: the synced look, or (right after a reload, before it is synced) the tag. */
    public String bossId() {
        String id = lookId();
        if (!id.isEmpty()) return id;
        for (String t : this.entityTags()) {
            if (t.startsWith(TAG)) {
                setLook(t.substring(TAG.length()));
                return lookId();
            }
        }
        return "";
    }

    @Override
    public boolean removeWhenFarAway(double distance) { return false; }
}
