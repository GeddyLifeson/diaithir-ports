package net.diaithir.entityanimation.skinlayers3d.client;

import net.neoforged.bus.api.IEventBus;

/** Placeholder holder for per-render state set by the layer itself; no mixins or extra events needed. */
public final class RenderContext {
    private RenderContext() {}

    static void registerEvents(IEventBus gameBus) {
    }
}
