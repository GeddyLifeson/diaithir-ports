package net.diaithir.voiceinteraction;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

/**
 * Creatures of the wild notice speech. Works with Simple Voice Chat (through its plugin API) and with diaithir-voice
 * (which calls {@link net.diaithir.voiceinteraction.api.VoiceInteractionApi}). Clean-room: written from the described
 * behaviour of voice-interaction addons, no code taken from any of them.
 */
@Mod(DiaithirVoiceInteraction.ID)
public final class DiaithirVoiceInteraction {
    public static final String ID = "diaithirvoiceinteraction";
    static final Logger LOG = LogUtils.getLogger();
    private static final DeferredRegister<GameEvent> EVENTS = DeferredRegister.create(Registries.GAME_EVENT, ID);
    /** Vibration with frequency 7 (data/neoforge/data_maps/game_event/vibration_frequencies.json), heard by sculk sensors and wardens. */
    public static final DeferredHolder<GameEvent, GameEvent> SPEECH = EVENTS.register("speech", () -> new GameEvent(16));

    public DiaithirVoiceInteraction(IEventBus modBus, ModContainer container) {
        EVENTS.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, InteractionConfig.SPEC);
        modBus.addListener(ModConfigEvent.Reloading.class, e -> Noticing.reloadRules());
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, e -> Noticing.forget(e.getEntity().getUUID()));
        boolean svc = ModList.get().isLoaded("voicechat");
        boolean own = ModList.get().isLoaded("diaithirvoice");
        LOG.info("Speech is heard through: {}", svc || own ? (svc ? "Simple Voice Chat " : "") + (own ? "diaithir-voice" : "") : "nothing yet, install a voice mod");
    }
}
