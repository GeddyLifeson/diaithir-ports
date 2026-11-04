/*
 * Derived from NeoForged FancyModLoader's ErrorDisplay and ErrorDisplayWindow
 * (net.neoforged.fml.earlydisplay.error, FML 11.0.15).
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.diaithir.earlywindow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.earlydisplay.render.EarlyFramebuffer;
import net.neoforged.fml.earlydisplay.render.ElementShader;
import net.neoforged.fml.earlydisplay.render.GlState;
import net.neoforged.fml.earlydisplay.render.MaterializedTheme;
import net.neoforged.fml.earlydisplay.render.RenderContext;
import net.neoforged.fml.earlydisplay.render.SimpleBufferBuilder;
import net.neoforged.fml.earlydisplay.render.SimpleFont;
import net.neoforged.fml.earlydisplay.render.Texture;
import net.neoforged.fml.earlydisplay.theme.Theme;
import net.neoforged.fml.earlydisplay.theme.ThemeLoader;
import net.neoforged.fml.earlydisplay.theme.ThemeResource;
import net.neoforged.fml.earlydisplay.theme.TextureScaling;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.system.Callback;

/** The window shown when mods fail to load, in Diaithir's colours, with a link to the pack's help page. */
final class ErrorWindow {
    static final String HELP_URL = "https://github.com/GeddyLifeson/diaithir-ports/blob/claude/project-thread-ysjkxr/README.md#if-something-goes-wrong";

    private static final int W = 854;
    private static final int H = 480;
    private static final int BACKGROUND = 0xFF1B1D2A;
    private static final int PANEL = 0xFF14161F;
    private static final int GOLD = 0xFFC9A24A;
    private static final int EMBER = 0xFFF59A23;
    private static final int TEXT = 0xFFF3E3B5;
    private static final int MUTED = 0xFFA9AEB5;
    private static final int LIST_TOP = 84;
    private static final int LIST_BOTTOM = 356;
    private static final int LIST_X = 30;
    private static final int BUTTON_W = 380;
    private static final int BUTTON_H = 40;
    private static final long FRAME_NANOS = TimeUnit.MILLISECONDS.toNanos(10);

    private final long window;
    private final MaterializedTheme theme;
    private final SimpleFont font;
    private final Texture logo;
    private final EarlyFramebuffer framebuffer = new EarlyFramebuffer(W, H);
    private final SimpleBufferBuilder buffer = new SimpleBufferBuilder("diaithir_error", 8192);
    private final List<Button> buttons;
    private final List<String> lines = new ArrayList<>();
    private final int lineHeight;
    private boolean closed;
    private int offsetX, offsetY;
    private float scale = 1;
    private double mouseX = -1, mouseY = -1;
    private float scroll;

    private ErrorWindow(long window, List<ModLoadingIssue> issues, Path logFile, Path crashReportFile) {
        this.window = window;
        Path themeDir = FMLPaths.CONFIGDIR.get().resolve("fml");
        Theme loaded;
        try {
            loaded = ThemeLoader.load(themeDir, "diaithir");
        } catch (Exception e) {
            loaded = Theme.createDefaultTheme();
        }
        this.theme = MaterializedTheme.materialize(loaded, themeDir);
        this.font = theme.getFont(Theme.FONT_DEFAULT);
        this.lineHeight = font.lineSpacing() - 5;
        var image = new ThemeResource("diaithir_logo.png").tryLoadAsImage(themeDir);
        this.logo = image == null ? null : Texture.create(image, "diaithir_logo", new TextureScaling.Stretch(56, 56, true), null);
        if (image != null) image.close();

        int left = W / 2 - 10 - BUTTON_W, right = W / 2 + 10;
        this.buttons = List.of(
                new Button(left, 368, "Get help", true, true, () -> open(HELP_URL)),
                new Button(right, 368, "Open log", logFile != null, false, () -> open(logFile)),
                new Button(left, 420, "Open crash report", crashReportFile != null, false, () -> open(crashReportFile)),
                new Button(right, 420, "Quit", true, false, () -> closed = true));

        // Errors first, then warnings; colour codes from the issue text are dropped.
        issues.stream().filter(i -> i.severity() == ModLoadingIssue.Severity.ERROR).forEach(this::addIssue);
        issues.stream().filter(i -> i.severity() != ModLoadingIssue.Severity.ERROR).forEach(this::addIssue);
    }

    static void preload() {
        Button.class.getName();
    }

    static void show(long window, List<ModLoadingIssue> issues, Path logFile, Path crashReportFile) {
        for (Callback callback : new Callback[] {
                GLFW.glfwSetWindowCloseCallback(window, null), GLFW.glfwSetCursorPosCallback(window, null),
                GLFW.glfwSetScrollCallback(window, null), GLFW.glfwSetMouseButtonCallback(window, null),
                GLFW.glfwSetKeyCallback(window, null), GLFW.glfwSetCharCallback(window, null),
                GLFW.glfwSetFramebufferSizeCallback(window, null), GLFW.glfwSetWindowSizeCallback(window, null)}) {
            if (callback != null) callback.close();
        }
        var screen = new ErrorWindow(window, issues, logFile, crashReportFile);
        GLFW.glfwSetWindowTitle(window, "Diaithir");
        GLFW.glfwSetWindowCloseCallback(window, w -> screen.closed = true);
        GLFW.glfwSetCursorPosCallback(window, (w, x, y) -> {
            screen.mouseX = (x - screen.offsetX) / screen.scale;
            screen.mouseY = (y - screen.offsetY) / screen.scale;
        });
        GLFW.glfwSetScrollCallback(window, (w, dx, dy) -> screen.scrollBy(-dy * 10));
        GLFW.glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_1 && action == GLFW.GLFW_PRESS) {
                screen.buttons.stream().filter(b -> b.isOver(screen.mouseX, screen.mouseY)).findFirst().ifPresent(Button::press);
            }
        });
        GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_RELEASE) return;
            switch (key) {
                case GLFW.GLFW_KEY_ESCAPE -> screen.closed = true;
                case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_PAGE_UP -> screen.scrollBy(-10);
                case GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_PAGE_DOWN -> screen.scrollBy(10);
                default -> {}
            }
        });

        GlState.readFromOpenGL();
        long nextFrame = 0;
        while (!screen.closed) {
            long now = System.nanoTime();
            if (now >= nextFrame) {
                screen.render();
                nextFrame = now + FRAME_NANOS;
                GLFW.glfwPollEvents();
            } else {
                GLFW.glfwWaitEventsTimeout((nextFrame - now) / 1e9);
            }
        }
        System.exit(1);
    }

    private void addIssue(ModLoadingIssue issue) {
        String text = FMLTranslations.translateIssueEnglish(issue).replaceAll("§.", "");
        for (String paragraph : text.split("\n")) {
            StringBuilder line = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                if (!line.isEmpty() && font.stringWidth(line + " " + word) > W - 2 * LIST_X) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                line.append(line.isEmpty() ? "" : " ").append(word);
            }
            lines.add(line.toString());
        }
        lines.add("");
    }

    private void scrollBy(double delta) {
        float max = Math.max(lines.size() * lineHeight - (LIST_BOTTOM - LIST_TOP - 8), 0);
        scroll = (float) Math.clamp(scroll + delta, 0, max);
    }

    private void render() {
        framebuffer.activate();
        int[] fbW = new int[1], fbH = new int[1];
        GLFW.glfwGetFramebufferSize(window, fbW, fbH);
        framebuffer.resize(fbW[0], fbH[0]);

        // Fit the 854x480 layout into the window, keeping its aspect ratio.
        float aspect = W / (float) H;
        if (framebuffer.width() / (float) framebuffer.height() > aspect) {
            int width = (int) (aspect * framebuffer.height());
            offsetX = (framebuffer.width() - width) / 2;
            offsetY = 0;
            scale = (float) framebuffer.height() / H;
            GlState.viewport(offsetX, 0, width, framebuffer.height());
        } else {
            int height = (int) (framebuffer.width() / aspect);
            offsetX = 0;
            offsetY = (framebuffer.height() - height) / 2;
            scale = (float) framebuffer.width() / W;
            GlState.viewport(0, offsetY, framebuffer.width(), height);
        }
        GlState.clearColor(0, 0, 0, 1);
        GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT | GL11C.GL_DEPTH_BUFFER_BIT);
        GlState.enableBlend(true);
        GlState.blendFuncSeparate(GL11C.GL_SRC_ALPHA, GL11C.GL_ONE_MINUS_SRC_ALPHA, GL11C.GL_ZERO, GL11C.GL_ONE);
        for (ElementShader shader : theme.shaders().values()) {
            shader.activate();
            if (shader.hasUniform(ElementShader.UNIFORM_SCREEN_SIZE)) {
                shader.setUniform2f(ElementShader.UNIFORM_SCREEN_SIZE, W, H);
            }
        }

        var ctx = new RenderContext(buffer, theme, W, H, offsetX, offsetY, scale, 0);
        ctx.fillRect(0, 0, W, H, BACKGROUND);
        if (logo != null) ctx.blitTexture(logo, LIST_X, 14, 56, 56);
        text(ctx, 98, 16, "Diaithir could not start", TEXT);
        text(ctx, 98, 44, "Launch again first. Still stuck? Press Get help.", MUTED);

        ctx.fillRect(0, LIST_TOP - 2, W, 2, GOLD);
        ctx.fillRect(0, LIST_TOP, W, LIST_BOTTOM - LIST_TOP, PANEL);
        ctx.fillRect(0, LIST_BOTTOM, W, 2, GOLD);
        GlState.scissorTest(true);
        ctx.scissorBox(0, LIST_TOP, W, LIST_BOTTOM - LIST_TOP);
        float y = LIST_TOP + 6 - scroll;
        for (String line : lines) {
            if (y > LIST_TOP - lineHeight && y < LIST_BOTTOM) text(ctx, LIST_X, y, line, TEXT);
            y += lineHeight;
        }
        GlState.scissorTest(false);

        buttons.forEach(b -> b.render(ctx));

        framebuffer.deactivate();
        GlState.viewport(0, 0, framebuffer.width(), framebuffer.height());
        framebuffer.blitToScreen(theme.theme().colorScheme().screenBackground(), framebuffer.width(), framebuffer.height());
        GLFW.glfwSwapBuffers(window);
    }

    private void text(RenderContext ctx, float x, float y, String text, int color) {
        ctx.renderText(x, y, font, List.of(new SimpleFont.DisplayText(text, color)));
    }

    private static void open(Object target) {
        String uri = target instanceof Path path ? path.toUri().toString() : target.toString();
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String[] command = os.contains("win") ? new String[] { "rundll32", "url.dll,FileProtocolHandler", uri }
                : os.contains("mac") ? new String[] { "open", uri }
                : new String[] { "xdg-open", uri.replace("file:/", "file:///").replace("file:////", "file:///") };
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException ignored) {}
    }

    private final class Button {
        private final int x, y;
        private final String label;
        private final boolean active, primary;
        private final Runnable action;

        Button(int x, int y, String label, boolean active, boolean primary, Runnable action) {
            this.x = x;
            this.y = y;
            this.label = label;
            this.active = active;
            this.primary = primary;
            this.action = action;
        }

        boolean isOver(double mx, double my) {
            return mx >= x && mx < x + BUTTON_W && my >= y && my < y + BUTTON_H;
        }

        void press() {
            if (active) action.run();
        }

        void render(RenderContext ctx) {
            boolean hover = active && isOver(mouseX, mouseY);
            int border = !active ? 0xFF3A3D4A : hover ? TEXT : primary ? EMBER : GOLD;
            ctx.fillRect(x, y, BUTTON_W, BUTTON_H, border);
            ctx.fillRect(x + 2, y + 2, BUTTON_W - 4, BUTTON_H - 4, primary && active ? 0xFF7A1F0E : PANEL);
            float tx = x + (BUTTON_W - font.stringWidth(label)) / 2f;
            text(ctx, tx, y + (BUTTON_H - font.lineSpacing()) / 2f - 2, label, active ? TEXT : MUTED);
        }
    }
}
