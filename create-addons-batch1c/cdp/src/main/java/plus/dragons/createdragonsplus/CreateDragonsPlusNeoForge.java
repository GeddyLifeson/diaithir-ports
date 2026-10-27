package plus.dragons.createdragonsplus;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import plus.dragons.createdragonsplus.network.CDPNetworking;

@Mod("diaithir_create_dragons_plus")
public class CreateDragonsPlusNeoForge {
    public CreateDragonsPlusNeoForge(IEventBus modBus, ModContainer container) {
        new CreateDragonsPlus().onInitialize();
        plus.dragons.createdragonsplus.compat.ServerLifecycleEvents.init();
        CDPNetworking.register(modBus);
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(FMLClientSetupEvent.class,
                    event -> new plus.dragons.createdragonsplus.client.CreateDragonsPlusClient().onInitializeClient());
        }
    }
}
