package com.diaithir.overworld_biomes.naturescompass;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

import java.util.List;

public final class ClientHooks {
    static List<String> lastList = List.of();

    static void openScreen(InteractionHand hand) {
        if (!lastList.isEmpty()) Minecraft.getInstance().setScreen(new BiomeSelectScreen(lastList, hand == InteractionHand.MAIN_HAND));
    }

    static void openList(List<String> list, boolean main) {
        lastList = list;
        Minecraft.getInstance().setScreen(new BiomeSelectScreen(list, main));
    }

    private ClientHooks() {}
}
