package buildcraft.lib;

import net.diaithir.upgrades.UpgradeHost;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Diaithir integration: reads the shared upgrade effects of a block entity (INTEGRATION.md section 4). 1.0 when none apply. */
public final class DiaithirUpgrades {
    private DiaithirUpgrades() {}

    private static UpgradeHost host(BlockEntity be) {
        if (be == null || be.getLevel() == null) return null;
        return be.getLevel().getCapability(UpgradeHost.CAP, be.getBlockPos(), null);
    }

    /** Same as {@link #consumption(BlockEntity)} for a tile known only through an interface. */
    public static double consumptionOf(Object tile) {
        return tile instanceof BlockEntity be ? consumption(be) : 1.0;
    }

    /** Work-rate multiplier from modules and beacons. */
    public static double speed(BlockEntity be) {
        UpgradeHost h = host(be);
        return h == null ? 1.0 : h.effects().speedMultiplier();
    }

    /** Energy-draw multiplier (below 1 with efficiency modules). */
    public static double consumption(BlockEntity be) {
        UpgradeHost h = host(be);
        return h == null ? 1.0 : h.effects().consumptionMultiplier();
    }
}
