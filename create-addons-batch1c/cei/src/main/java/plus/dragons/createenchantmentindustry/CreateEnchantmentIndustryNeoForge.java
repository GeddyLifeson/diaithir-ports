package plus.dragons.createenchantmentindustry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import plus.dragons.createenchantmentindustry.client.CEIClient;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.network.CEINetwork;

@Mod("diaithir_create_enchantment_industry")
public class CreateEnchantmentIndustryNeoForge {
    public CreateEnchantmentIndustryNeoForge(IEventBus modBus, ModContainer container) {
        CEINetwork.register(modBus);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.OnDatapackSyncEvent.class, e -> e.sendRecipes(
                plus.dragons.createenchantmentindustry.common.registry.CEIRecipes.PRINTING.getType(),
                plus.dragons.createenchantmentindustry.common.registry.CEIRecipes.GRINDING.getType()));
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(FMLClientSetupEvent.class, event -> new CEIClient().onInitializeClient());
        }
    }
}
