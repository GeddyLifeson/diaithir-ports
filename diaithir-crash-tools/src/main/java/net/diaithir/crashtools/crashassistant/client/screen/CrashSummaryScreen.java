package net.diaithir.crashtools.crashassistant.client.screen;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.diaithir.crashtools.crashassistant.config.CrashToolsConfig;
import net.diaithir.crashtools.crashassistant.crash.CrashParser;
import net.diaithir.crashtools.crashassistant.crash.ReportFormatter;
import net.diaithir.crashtools.crashassistant.crash.SuspectAnalyzer;
import net.diaithir.crashtools.crashassistant.util.ClipboardUtil;
import net.diaithir.crashtools.crashassistant.util.FolderOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CrashSummaryScreen extends Screen {
    private final Screen parent;
    private final Path report;
    private final CrashParser.Parsed parsed;
    private final List<SuspectAnalyzer.Suspect> suspects;
    private Component status = Component.empty();

    public CrashSummaryScreen(Screen parent, Path report) {
        super(Component.literal("Crash Assistant"));
        this.parent = parent;
        this.report = report;
        this.parsed = CrashParser.parse(report);
        this.suspects = SuspectAnalyzer.analyze(parsed.frames(), CrashToolsConfig.SUSPECT_FRAME_DEPTH.get());
    }

    @Override
    protected void init() {
        int w = 150, h = 20, gap = 4;
        int x1 = width / 2 - w - gap / 2, x2 = width / 2 + gap / 2;
        int y = height - 74;
        addRenderableWidget(Button.builder(Component.literal("Open crash report"), b -> {
            if (!FolderOpener.open(report)) status = Component.literal("Could not open report");
        }).bounds(x1, y, w, h).build());
        addRenderableWidget(Button.builder(Component.literal("Open logs folder"), b -> {
            if (!FolderOpener.open(Minecraft.getInstance().gameDirectory.toPath().resolve("logs")))
                status = Component.literal("Could not open folder");
        }).bounds(x2, y, w, h).build());
        y += h + gap;
        addRenderableWidget(Button.builder(Component.literal("Copy report"), b ->
                copy(ReportFormatter.fullMarkdown(report, parsed, suspects), report)).bounds(x1, y, w, h).build());
        addRenderableWidget(Button.builder(Component.literal("Copy summary"), b ->
                copy(ReportFormatter.summary(parsed, suspects), null)).bounds(x2, y, w, h).build());
        y += h + gap;
        int count = CrashToolsConfig.ENABLE_UPLOAD.get() ? 3 : 2;
        int bw = 100, total = count * bw + (count - 1) * gap, x = width / 2 - total / 2;
        if (CrashToolsConfig.ENABLE_UPLOAD.get()) {
            addRenderableWidget(Button.builder(Component.literal("Upload"), b -> upload()).bounds(x, y, bw, h).build());
            x += bw + gap;
        }
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(x, y, bw, h).build());
        x += bw + gap;
        addRenderableWidget(Button.builder(Component.literal("Quit game"), b -> Minecraft.getInstance().stop())
                .bounds(x, y, bw, h).build());
    }

    private void copy(String text, Path fallbackBase) {
        if (ClipboardUtil.copy(text)) {
            status = Component.literal("Copied to clipboard");
            return;
        }
        Path out = ClipboardUtil.saveFallback(text, Minecraft.getInstance().gameDirectory.toPath());
        status = Component.literal(out != null ? "Clipboard failed; saved to " + out.getFileName() : "Clipboard and save failed");
    }

    private void upload() {
        String url = CrashToolsConfig.UPLOAD_URL.get();
        if (url.isBlank()) {
            status = Component.literal("No upload URL configured");
            return;
        }
        String body = ReportFormatter.fullMarkdown(report, parsed, suspects);
        status = Component.literal("Uploading...");
        Thread t = new Thread(() -> {
            try {
                var req = java.net.http.HttpRequest.newBuilder(java.net.URI.create(url))
                        .header("Content-Type", "text/plain; charset=utf-8")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body)).build();
                var resp = java.net.http.HttpClient.newHttpClient()
                        .send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                String msg = resp.statusCode() / 100 == 2 ? "Uploaded: " + resp.body().strip() : "Upload failed: HTTP " + resp.statusCode();
                Minecraft.getInstance().execute(() -> status = Component.literal(msg));
            } catch (Exception e) {
                Minecraft.getInstance().execute(() -> status = Component.literal("Upload failed: " + e.getMessage()));
            }
        }, "crashtools-upload");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float pt) {
        super.extractRenderState(g, mx, my, pt);
        int y = 12;
        g.centeredText(font, title, width / 2, y, 0xFFFFFFFF);
        y += 16;
        int x = 20;
        int maxW = width - 40;
        y = line(g, "Exception: " + parsed.exception(), x, y, maxW, 0xFFFF5555);
        if (!parsed.description().isEmpty()) y = line(g, "Description: " + parsed.description(), x, y, maxW, 0xFFCCCCCC);
        y = line(g, "Minecraft " + parsed.minecraftVersion() + " / NeoForge " + parsed.neoVersion(), x, y, maxW, 0xFFAAAAAA);
        y += 4;
        if (suspects.isEmpty()) {
            y = line(g, "Suspects: none identified (vanilla/unknown)", x, y, maxW, 0xFFFFFF55);
        } else {
            y = line(g, "Suspects:", x, y, maxW, 0xFFFFFF55);
            int i = 1;
            for (SuspectAnalyzer.Suspect s : suspects) {
                y = line(g, " " + i++ + ". " + s.modName() + " (" + s.modId() + ") - " + s.hits() + " frame(s)", x, y, maxW, 0xFFFFDD77);
            }
        }
        y += 4;
        y = line(g, "Top frames:", x, y, maxW, 0xFFFFFF55);
        int limit = height - 84;
        for (String f : parsed.frames()) {
            if (y + 10 > limit) break;
            y = line(g, "  " + f, x, y, maxW, 0xFFBBBBBB);
        }
        g.centeredText(font, status, width / 2, height - 12, 0xFF55FF55);
    }

    private int line(GuiGraphicsExtractor g, String s, int x, int y, int maxW, int color) {
        g.text(font, font.plainSubstrByWidth(s, maxW), x, y, color);
        return y + 10;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }
}
