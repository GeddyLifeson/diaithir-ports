package net.diaithir.animation;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Client test tools for the 3D skin layers: /skinlayers on|off, /skinlayerstest (synthetic wide and slim skins through the downloaded-skin path), /skinlayersbench N. */
final class SkinTest {
    private static long frames, since = System.nanoTime();

    static void init() {
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, e -> {
            var d = e.getDispatcher();
            d.register(Commands.literal("skinlayers")
                    .then(Commands.literal("on").executes(c -> { SkinLayers.enabled = true; return say("skin layers on"); }))
                    .then(Commands.literal("off").executes(c -> { SkinLayers.enabled = false; return say("skin layers off"); })));
            d.register(Commands.literal("skinlayerstest").executes(c -> test()));
            d.register(Commands.literal("skinlayersbench").then(Commands.argument("copies", IntegerArgumentType.integer(0, 400)).executes(c -> {
                SkinLayers.benchCopies = IntegerArgumentType.getInteger(c, "copies");
                frames = 0; since = System.nanoTime();
                return say("bench copies " + SkinLayers.benchCopies);
            })));
        });
        NeoForge.EVENT_BUS.addListener(RenderFrameEvent.Post.class, e -> {
            frames++;
            long now = System.nanoTime();
            if (now - since > 5_000_000_000L) {
                com.mojang.logging.LogUtils.getLogger().info("[skinbench] layers={} copies={} frames={} avg {} ms/frame", SkinLayers.enabled, SkinLayers.benchCopies, frames, String.format("%.1f", (now - since) / 1e6 / Math.max(1, frames)));
                frames = 0; since = now;
            }
        });
    }

    private static int say(String s) {
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(s));
        com.mojang.logging.LogUtils.getLogger().info("[skinlayers] {}", s);
        return 1;
    }

    /** A synthetic 64x64 skin registered as a DynamicTexture (what a downloaded skin is): alpha checker on the overlay boxes. Wide and slim must both build, match a direct SkinVoxels count, and slim must have fewer quads. */
    private static int test() {
        var log = com.mojang.logging.LogUtils.getLogger();
        NativeImage img = new NativeImage(64, 64, true);
        for (int x = 0; x < 64; x++) for (int y = 0; y < 64; y++) img.setPixel(x, y, ((x + y) % 3 == 0) ? 0x00000000 : 0xFF3A7BD5);
        Identifier id = Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "test_skin");
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(() -> "skinlayers test", img));
        int bad = 0;
        int[] want = new int[2];
        for (int s = 0; s < 2; s++) {
            boolean slim = s == 1;
            for (var b : SkinVoxels.boxes(slim).values()) want[s] += SkinVoxels.build(b, (x, y) -> ((x + y) % 3 == 0) ? 0 : 255).size();
            long t0 = System.nanoTime();
            var m = SkinLayers.mesh(id, slim);
            double ms = (System.nanoTime() - t0) / 1e6;
            boolean ok = m != null && m.quads() == want[s];
            if (!ok) bad++;
            log.info("[skinlayers] test {} skin via DynamicTexture: quads {} (want {}), built in {} ms -> {}", slim ? "slim" : "wide", m == null ? -1 : m.quads(), want[s], String.format("%.1f", ms), ok ? "ok" : "BAD");
        }
        if (want[1] >= want[0]) { bad++; log.info("[skinlayers] test BAD: slim has {} quads, wide {}", want[1], want[0]); }
        log.info("[skinlayers] test done, bad={}", bad);
        return say("skinlayerstest bad=" + bad);
    }

    private SkinTest() { }
}
