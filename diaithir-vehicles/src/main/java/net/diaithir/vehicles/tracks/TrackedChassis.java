package net.diaithir.vehicles.tracks;

import net.diaithir.vehicles.fuel.FuelTank;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.Vec3;

/**
 * A tracked chassis a player can sit in and drive: W/S throttle, A/D steer (pivots when standing). Fuel comes from its 600 L tank (diesel, 38.6 MJ/L);
 * the engine draws real power from {@link TrackDrive} and burns it through {@link FuelTank#burn}. Clean-room from tracked-vehicle behaviour; no Flan's Mod code or art.
 */
public class TrackedChassis extends Entity {
    public static final long TANK_L = 600;
    public final FuelTank tank = new FuelTank(TANK_L);
    public final TrackDrive drive = new TrackDrive(TrackDrive.Spec.LIGHT_TANK);
    private double speed;

    public TrackedChassis(EntityType<? extends TrackedChassis> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {}

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith(Entity other) {
        return true;
    }

    @Override
    public float maxUpStep() {
        return 1.0F; // tracks climb a full block
    }

    @Override
    protected boolean canAddPassenger(Entity p) {
        return getPassengers().isEmpty();
    }

    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player p ? p : null;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 at) {
        if (player.isSecondaryUseActive()) return InteractionResult.PASS;
        if (!level().isClientSide()) player.startRiding(this);
        return InteractionResult.SUCCESS;
    }

    public double speed() {
        return speed;
    }

    /** One tick of driving at the given controls; returns the joules of fuel energy burnt. Also called by the selftest. */
    public double driveStep(double throttle, double steer) {
        double dt = 0.05;
        boolean fuel = !tank.isEmpty();
        TrackDrive.Out o = drive.step(throttle, steer, dt, fuel);
        double burnt = 0;
        if (o.powerW() > 0) {
            double want = o.powerW() * dt;
            double got = tank.burn(want, TrackDrive.Spec.LIGHT_TANK.efficiency());
            burnt = got;
            if (got < want * 0.999) drive.stop(); // ran dry mid-step
        }
        speed = o.speed();
        float yaw = getYRot() + (float) Math.toDegrees(o.yawRate() * dt);
        setYRot(yaw);
        Vec3 fwd = Vec3.directionFromRotation(0, yaw);
        Vec3 m = getDeltaMovement();
        setDeltaMovement(fwd.x * o.speed() * dt, m.y - 0.08, fwd.z * o.speed() * dt);
        move(MoverType.SELF, getDeltaMovement());
        if (onGround()) setDeltaMovement(getDeltaMovement().multiply(1, 0, 1));
        return burnt;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        double t = 0, s = 0;
        if (getControllingPassenger() instanceof ServerPlayer sp) {
            Input in = sp.getLastClientInput();
            t = (in.forward() ? 1 : 0) - (in.backward() ? 1 : 0);
            s = (in.right() ? 1 : 0) - (in.left() ? 1 : 0);
        }
        driveStep(t, s);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false; // damage rules live in the crayfish module (health capability)
    }

    @Override
    protected void readAdditionalSaveData(ValueInput in) {
        tank.load(in.getStringOr("fuel", ""), in.getLongOr("amount", 0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        if (tank.fuelId() != null) {
            out.putString("fuel", tank.fuelId());
            out.putLong("amount", tank.amountL());
        }
    }
}
