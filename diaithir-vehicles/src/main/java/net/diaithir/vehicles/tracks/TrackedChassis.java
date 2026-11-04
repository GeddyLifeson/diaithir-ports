package net.diaithir.vehicles.tracks;

import net.diaithir.units.fuel.Engine;
import net.diaithir.units.fuel.FuelStore;
import net.diaithir.units.fuel.Fuels;
import net.diaithir.units.fuel.SimpleStore;
import net.diaithir.vehicles.FuelBurn;
import net.diaithir.vehicles.FuelHolder;
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
public class TrackedChassis extends Entity implements FuelHolder {
    public static final double TANK_L = 600;
    public final SimpleStore tank = new SimpleStore(TANK_L);
    /** What pumps and cans see: the tank, but it only accepts what the diesel engine class accepts (diesel, biodiesel, heavy fuel oil). */
    private final FuelStore view = new FuelStore() {
        @Override public net.diaithir.units.fuel.Fuel fuel() { return tank.fuel(); }
        @Override public double quantity() { return tank.quantity(); }
        @Override public double capacity() { return tank.capacity(); }
        @Override public double insert(net.diaithir.units.fuel.Fuel f, double amount) { return f != null && ENGINE.accepts(f.id()) ? tank.insert(f, amount) : 0; }
        @Override public double extract(double amount) { return tank.extract(amount); }
    };
    private static final Engine ENGINE = Fuels.engine("diesel_engine");

    @Override
    public FuelStore fuelStore() {
        return view;
    }
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
        boolean fuel = FuelBurn.canFeed(tank, ENGINE);
        TrackDrive.Out o = drive.step(throttle, steer, dt, fuel);
        double burnt = 0;
        if (o.powerW() > 0) {
            double want = o.powerW() * dt;
            double got = FuelBurn.feed(tank, ENGINE, o.powerW(), dt);
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
        var f = Fuels.get(in.getStringOr("fuel", ""));
        double q = in.getDoubleOr("litres", 0);
        if (f != null && q > 0) tank.insert(f, q);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput out) {
        if (tank.fuel() != null) {
            out.putString("fuel", tank.fuel().id());
            out.putDouble("litres", tank.quantity());
        }
    }
}
