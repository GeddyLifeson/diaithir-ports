package net.diaithir.crashtools.simplebackups.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class ClientEventHandler {

    private static final MutableComponent COMPONENT = Component.translatable("simplebackups.backups_paused").withStyle(ChatFormatting.DARK_RED);
    private static boolean isPaused = false;

    public static void setPaused(boolean paused) {
        isPaused = paused;
    }

    public static boolean isPaused() {
        return isPaused;
    }

    @SubscribeEvent
    public void onRenderText(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.HOTBAR, Identifier.fromNamespaceAndPath("simplebackups", "pause"), this::renderText);
    }

    private void renderText(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        if (!isPaused) {
            return;
        }

        guiGraphics.fill(3, 3, 20, 20, 0);
        guiGraphics.text(Minecraft.getInstance().font, COMPONENT, 3, 3, -1, true);
    }
}
