package com.github.alexthe666.citadel;

import com.github.alexthe666.citadel.server.message.AnimationMessage;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

/** Citadel entry point (LGPL-3.0, Alexthe668), Diaithir port to 26.1.2: animation sync, model helpers, legacy render shim. */
@Mod(Citadel.MODID)
public class Citadel {
    public static final String MODID = "citadel";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Citadel(IEventBus modBus) {
        modBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(AnimationMessage.TYPE, AnimationMessage.CODEC, AnimationMessage::handle);
    }
}
