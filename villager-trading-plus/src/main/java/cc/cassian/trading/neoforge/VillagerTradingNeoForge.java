package cc.cassian.trading.neoforge;

//? neoforge {
import cc.cassian.trading.VillagerTrading;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = VillagerTrading.MOD_ID, dist = Dist.CLIENT)
public final class VillagerTradingNeoForge {
    public VillagerTradingNeoForge() {
        // Run our common setup.
        VillagerTrading.init();
    }

}

//?}