package com.diaithir.overworld_biomes.planets;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client side of the colony picker: the /colonies command opens {@link ColonyScreen}. Only loaded on the client. */
final class ColonyClient {
    private ColonyClient() {}

    static void init() {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, e -> {
            CommandDispatcher<CommandSourceStack> d = e.getDispatcher();
            d.register(Commands.literal("colonies").executes(c -> {
                Minecraft mc = Minecraft.getInstance();
                mc.execute(() -> mc.setScreen(new ColonyScreen()));
                return 1;
            }));
        });
    }
}
