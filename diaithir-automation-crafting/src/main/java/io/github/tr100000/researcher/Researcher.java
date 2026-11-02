package io.github.tr100000.researcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.tr100000.researcher.command.ResearchCommand;
import io.github.tr100000.researcher.command.ResearcherCommand;
import io.github.tr100000.researcher.config.ResearcherConfigs;
import io.github.tr100000.researcher.networking.ResearcherNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Researcher (Tr100000, MIT), ported to NeoForge 26.1.2 for the Diaithir automation and crafting module. */
@Mod(Researcher.MODID)
public class Researcher {
    public static final String MODID = "researcher";
    public static final Logger LOGGER = LoggerFactory.getLogger("Researcher");
    public static final net.minecraft.sounds.SoundEvent RESEARCH_FINISHED_SOUND = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(ModUtils.id("ui.toast.research_finished"));
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Researcher(IEventBus modBus, ModContainer container) {
        ResearcherConfigs.init(container);

        modBus.addListener(ResearcherRegistries::onNewRegistry);
        modBus.addListener(ResearcherNetworking::registerPayloads);
        modBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.TRIGGER_TYPE)) {
                ResearcherCriteriaTriggers.flush();
            }
            else if (event.getRegistryKey().equals(Registries.SOUND_EVENT)) {
                event.register(Registries.SOUND_EVENT, ModUtils.id("ui.toast.research_finished"), () -> RESEARCH_FINISHED_SOUND);
            }
            else if (event.getRegistryKey().equals(ResearcherRegistries.RESEARCH_REWARD_TYPE_KEY)) {
                ResearcherRewardTypes.flush();
            }
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            ResearcherCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
            ResearchCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
        });

        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.AddServerReloadListenersEvent event) ->
                event.addListener(ResearchManager.ID, event.getServerResources().researcher$getServerManager()));

        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.OnDatapackSyncEvent event) ->
                event.sendRecipes(net.minecraft.world.item.crafting.RecipeType.CRAFTING));

        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.level.block.BreakBlockEvent event) -> {
            if (event.getPlayer() instanceof ServerPlayer player) {
                ResearcherCriteriaTriggers.BLOCK_BROKEN.trigger(player, event.getState());
            }
        });
    }

    public static String getVersion() {
        return ModList.get().getModContainerById(MODID).orElseThrow().getModInfo().getVersion().toString();
    }
}
