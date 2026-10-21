package net.diaithir.compat;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.neoforge.client.gui.LoadingErrorScreen;

/** Stands in for NeoForge's load warnings screen: the same warnings, Diaithir's wording, and a link to the pack's help page. */
final class WarningsScreen extends Screen {
    private final Runnable next;
    private final Component warnings;

    WarningsScreen(LoadingErrorScreen original) {
        super(Component.translatable("diaithir.warnings.title"));
        this.next = nextScreenTask(original);
        this.warnings = Component.literal(String.join("\n\n", ModLoader.getLoadingIssues().stream().map(FMLTranslations::translateIssue).toList()));
    }

    // NeoForge keeps the main menu task private; without it the screen falls back to the title screen.
    private static Runnable nextScreenTask(LoadingErrorScreen original) {
        try {
            Field field = LoadingErrorScreen.class.getDeclaredField("nextScreenTask");
            field.setAccessible(true);
            return (Runnable) field.get(original);
        } catch (ReflectiveOperationException e) {
            return () -> Minecraft.getInstance().setScreen(null);
        }
    }

    @Override
    protected void init() {
        centered(12, title);
        centered(26, Component.translatable("diaithir.warnings.subtitle"));
        addRenderableWidget(new FittingMultiLineTextWidget(20, 44, width - 40, height - 104, warnings, font));
        int buttonWidth = Math.min(150, (width - 50) / 3);
        int x = width / 2 - (buttonWidth * 3 + 10) / 2;
        addRenderableWidget(Button.builder(Component.translatable("diaithir.warnings.continue"), b -> next.run()).bounds(x, height - 50, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("diaithir.menu.help"), b -> Util.getPlatform().openUri(DiaithirClient.HELP_PAGE)).bounds(x + buttonWidth + 5, height - 50, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("menu.quit"), b -> minecraft.stop()).bounds(x + 2 * (buttonWidth + 5), height - 50, buttonWidth, 20).build());
    }

    private void centered(int y, Component text) {
        int w = font.width(text);
        addRenderableWidget(new StringWidget(width / 2 - w / 2, y, w, 9, text, font));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
