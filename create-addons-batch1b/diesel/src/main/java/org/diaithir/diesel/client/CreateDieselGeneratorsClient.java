package org.diaithir.diesel.client;

import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.KineticAudioBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.diaithir.diesel.registries.CDGBlockEntityTypes;
import org.diaithir.diesel.registries.CDGFluids;

public class CreateDieselGeneratorsClient {
    public static void init(IEventBus bus) {
        bus.addListener(FMLClientSetupEvent.class, e -> {
            CDGFluids.ENTRIES.values().forEach(f -> AllFluidConfigs.model(f.still));
            e.enqueueWork(CreateDieselGeneratorsClient::register);
        });
    }

    private static void register() {
        var type = CDGBlockEntityTypes.DIESEL_ENGINE.get();
        AllBlockEntityRenders.visual(type, EngineShaftRenderer::new, OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF));
        AllBlockEntityBehaviours.add(type, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        var mod = CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get();
        AllBlockEntityRenders.visual(mod, ModularShaftRenderer::new, com.zurrtum.create.client.content.kinetics.base.SingleAxisRotatingVisual.of(AllPartialModels.SHAFT));
        AllBlockEntityBehaviours.add(mod, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
        var burner = CDGBlockEntityTypes.BURNER.get();
        AllBlockEntityBehaviours.add(burner, KineticAudioBehaviour::new, KineticTooltipBehaviour::new);
    }
}
