package com.diaithir.overworld_biomes.naturescompass;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

final class ClientState {
    static int searchRadius = -1;

    static void onProgress(CompassNetwork.SearchProgress p) {
        searchRadius = p.radius();
        show(Component.translatable("msg.diaithir_overworld_biomes.searching", p.radius()));
    }

    static void onResult(CompassNetwork.SearchResult r) {
        searchRadius = -1;
        show(switch (r.status()) {
            case 1 -> Component.translatable("msg.diaithir_overworld_biomes.found", r.distance());
            case 2 -> Component.translatable("msg.diaithir_overworld_biomes.not_found", CompassConfig.MAX_RADIUS.get());
            default -> Component.translatable("msg.diaithir_overworld_biomes.not_locatable");
        });
    }

    private static void show(Component c) {
        if (!CompassConfig.SHOW_READOUT.get()) return;
        var mc = Minecraft.getInstance();
        if (mc.player != null) mc.gui.setOverlayMessage(c, false);
    }

    private ClientState() {}
}
