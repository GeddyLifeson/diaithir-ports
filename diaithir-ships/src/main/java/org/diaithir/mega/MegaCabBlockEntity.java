package org.diaithir.mega;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.valkyrienskies.tournament.util.helper.Helper3dKt;

/**
 * Same rule as carts' Autonomy (entity vehicles), for block-built VS2 machines: crewless only when the type is capable (catalog flag)
 * AND a module is fitted; otherwise a living crew member must stand within CREW_RANGE metres of the cab, whatever the Age.
 */
public class MegaCabBlockEntity extends BlockEntity {
    public enum Status { UNCREWED, CREWED, AUTONOMOUS }

    public static final double CREW_RANGE = 4.0;
    /** Crew jobs a fitted module replaces; same set as carts' Autonomy.AUTOMATED_JOBS plus the mega operator jobs. */
    public static final java.util.Set<String> AUTOMATED_JOBS =
            java.util.Set.of("driver", "train_driver", "cart_driver", "crane_operator", "machine_operator", "excavator_operator", "truck_driver");

    private String specId = "";
    private boolean moduleFitted;

    public MegaCabBlockEntity(BlockPos pos, BlockState state) { super(MegaMod.CAB_BE.get(), pos, state); }

    public String specId() { return specId; }

    public void setSpec(String id) {
        if (MegaCatalog.get(id) == null) throw new IllegalArgumentException("unknown mega vehicle " + id);
        specId = id;
        setChanged();
    }

    public boolean capable() {
        MegaCatalog.Row r = MegaCatalog.get(specId);
        return r != null && r.autonomyCapable();
    }

    public boolean moduleFitted() { return moduleFitted; }

    /** Fits a module. False when the machine is not capable or one is already fitted. */
    public boolean fitModule() {
        if (!capable() || moduleFitted) return false;
        moduleFitted = true;
        setChanged();
        return true;
    }

    public boolean autonomous() { return capable() && moduleFitted; }

    public Status status() {
        if (autonomous()) return Status.AUTONOMOUS;
        return crewNearby() ? Status.CREWED : Status.UNCREWED;
    }

    public boolean mayRun() { return status() != Status.UNCREWED; }

    /** True when this job on this machine is automated, so the wages thread can retire that crew. */
    public static boolean autonomousCrew(String job, MegaCabBlockEntity cab) {
        return AUTOMATED_JOBS.contains(job) && cab.autonomous();
    }

    private boolean crewNearby() {
        if (level == null) return false;
        Vector3d w = Helper3dKt.convertShipToWorldSpace(level, worldPosition);   // cab may sit on a ship; crew positions are world coordinates
        AABB box = new AABB(w.x - CREW_RANGE, w.y - CREW_RANGE, w.z - CREW_RANGE, w.x + CREW_RANGE, w.y + CREW_RANGE, w.z + CREW_RANGE);
        return !level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && !(e instanceof Player p && p.isSpectator())).isEmpty();
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        specId = in.getStringOr("spec", "");
        moduleFitted = in.getBooleanOr("module", false);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("spec", specId);
        out.putBoolean("module", moduleFitted);
    }
}
