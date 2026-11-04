// Ported from Talk Balloons (LGPL-3.0-only) https://github.com/CERBON-MODS/Talk-Balloons
// Modifications: balloon store + render via NeoForge events; only chat-bubble drawing (fills + text) with vanilla UI colours, no custom styles/atlas/mixins
package net.diaithir.hudoverlays.talkballoons.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.diaithir.hudoverlays.talkballoons.config.TalkBalloonsConfig;
import net.diaithir.hudoverlays.talkballoons.network.BalloonPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class TalkBalloonsClient {
    private static final Map<UUID, List<Balloon>> BALLOONS = new ConcurrentHashMap<>();
    private static boolean serverSupport = false;

    private static final class Balloon {
        final Component text;
        final long created;
        final int ticks;

        Balloon(Component text, int ticks) {
            this.text = text;
            this.created = System.currentTimeMillis();
            this.ticks = ticks;
        }
    }

    private TalkBalloonsClient() {}

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(new Handlers());
    }

    public static void handleBalloon(BalloonPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            serverSupport = true;
            addBalloon(payload.player(), payload.message(), payload.ticks());
        });
    }

    private static void addBalloon(UUID id, Component text, int ticks) {
        if (!TalkBalloonsConfig.ENABLED.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(id) && !TalkBalloonsConfig.SHOW_OWN_BALLOON.get()) {
            return;
        }
        List<Balloon> list = BALLOONS.computeIfAbsent(id, k -> new ArrayList<>());
        synchronized (list) {
            list.add(new Balloon(text, ticks));
            int max = TalkBalloonsConfig.MAX_BALLOONS.get();
            while (list.size() > max) {
                list.remove(0);
            }
        }
    }

    private static final class Handlers {
        @SubscribeEvent
        public void onTick(ClientTickEvent.Post e) {
            long now = System.currentTimeMillis();
            for (List<Balloon> list : BALLOONS.values()) {
                synchronized (list) {
                    Iterator<Balloon> it = list.iterator();
                    while (it.hasNext()) {
                        Balloon b = it.next();
                        if (b.ticks > 0 && now - b.created >= (long) b.ticks * 50L) {
                            it.remove();
                        }
                    }
                }
            }
            BALLOONS.values().removeIf(List::isEmpty);
        }

        @SubscribeEvent
        public void onChat(ClientChatReceivedEvent e) {
            if (serverSupport || !TalkBalloonsConfig.ENABLED.get() || e.getSender() == null) {
                return;
            }
            if (e.isSystem()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            Player p = mc.level.getPlayerByUUID(e.getSender());
            if (p == null) {
                return;
            }
            addBalloon(e.getSender(), Component.literal(e.getMessage().getString()), TalkBalloonsConfig.BALLOON_AGE.get() * 20);
            if (TalkBalloonsConfig.ONLY_DISPLAY_BALLOONS.get() && mc.player != null && mc.player.getUUID().equals(e.getSender())) {
                e.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onLogout(ClientPlayerNetworkEvent.LoggingOut e) {
            BALLOONS.clear();
            serverSupport = false;
        }

        @SubscribeEvent
        public void onNameTag(RenderNameTagEvent.DoRender e) {
            if (!TalkBalloonsConfig.ENABLED.get()) {
                return;
            }
            if (!(e.getEntityRenderState() instanceof AvatarRenderState state)) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            UUID uuid = null;
            for (AbstractClientPlayer p : mc.level.players()) {
                if (p.getId() == state.id) {
                    uuid = p.getUUID();
                    break;
                }
            }
            if (uuid == null) {
                return;
            }
            List<Balloon> list = BALLOONS.get(uuid);
            if (list == null || list.isEmpty() || state.isInvisible) {
                return;
            }
            List<Balloon> copy;
            synchronized (list) {
                copy = new ArrayList<>(list);
            }
            draw(e.getPoseStack(), e.getSubmitNodeCollector(), e.getCameraRenderState(), state, copy);
        }
    }

    private static void draw(PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera, AvatarRenderState state, List<Balloon> balloons) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int padding = TalkBalloonsConfig.BALLOON_PADDING.get();
        int maxWidth = TalkBalloonsConfig.MAX_BALLOON_WIDTH.get();
        int minWidth = TalkBalloonsConfig.MIN_BALLOON_WIDTH.get();
        int between = TalkBalloonsConfig.DISTANCE_BETWEEN_BALLOONS.get();
        long now = System.currentTimeMillis();
        int fadeMs = (int) (TalkBalloonsConfig.BALLOON_FADE_OUT.get() * 1000);
        double baseOpacity = state.isCrouching ? TalkBalloonsConfig.BALLOON_SNEAKING_OPACITY.get() : TalkBalloonsConfig.BALLOON_OPACITY.get();
        int textRgb = TalkBalloonsConfig.TEXT_COLOR.get() & 0xFFFFFF;
        int tintRgb = TalkBalloonsConfig.BALLOON_TINT.get() & 0xFFFFFF;

        float distance = 0f;
        for (int i = balloons.size() - 1; i >= 0; i--) {
            Balloon b = balloons.get(i);
            float fade = 1f;
            if (b.ticks > 0 && fadeMs > 0) {
                long remaining = (long) b.ticks * 50L - (now - b.created);
                if (remaining < fadeMs) {
                    fade = Math.max(0f, Math.min(1f, remaining / (float) fadeMs));
                }
            }
            int alpha = Math.max(10, (int) (baseOpacity * fade * 255));

            List<FormattedCharSequence> lines = font.split(b.text, maxWidth);
            if (lines.isEmpty()) {
                continue;
            }
            int textWidth = 0;
            for (FormattedCharSequence l : lines) {
                textWidth = Math.max(textWidth, font.width(l));
            }
            int width = Math.max(minWidth, textWidth) + padding * 2 + 2;
            int height = lines.size() * font.lineHeight + padding * 2 + 2;

            pose.pushPose();
            pose.translate(0.0F, state.boundingBoxHeight + TalkBalloonsConfig.HEIGHT_OFFSET.get().floatValue(), 0.0F);
            pose.mulPose(camera.orientation);
            pose.scale(0.025F, -0.025F, 0.025F);

            float top = -distance - height;
            int bg = (alpha << 24) | tintRgb;
            int border = (alpha << 24) | 0x373737;
            int txt = (Math.max(10, alpha) << 24) | textRgb;

            final int fx = -width / 2;
            final int fy = (int) top;
            final int fw = width;
            final int fh = height;

            collector.submitCustomGeometry(pose, net.minecraft.client.renderer.rendertype.RenderTypes.textBackgroundSeeThrough(), (p, buffer) -> {
                quad(p, buffer, fx - 1, fy, fx, fy + fh, border, state.lightCoords);
                quad(p, buffer, fx + fw, fy, fx + fw + 1, fy + fh, border, state.lightCoords);
                quad(p, buffer, fx, fy - 1, fx + fw, fy, border, state.lightCoords);
                quad(p, buffer, fx, fy + fh, fx + fw, fy + fh + 1, border, state.lightCoords);
                quad(p, buffer, fx, fy, fx + fw, fy + fh, bg, state.lightCoords);
            });

            int ly = fy + padding + 1;
            for (FormattedCharSequence l : lines) {
                float lx = -font.width(l) / 2f;
                collector.submitText(pose, lx, ly, l, false, Font.DisplayMode.NORMAL, state.lightCoords, txt, 0, 0);
                ly += font.lineHeight;
            }

            pose.popPose();
            distance += height + between;
        }
    }

    private static void quad(PoseStack.Pose p, com.mojang.blaze3d.vertex.VertexConsumer buf, float x0, float y0, float x1, float y1, int color, int light) {
        buf.addVertex(p, x0, y1, 0f).setColor(color).setLight(light);
        buf.addVertex(p, x1, y1, 0f).setColor(color).setLight(light);
        buf.addVertex(p, x1, y0, 0f).setColor(color).setLight(light);
        buf.addVertex(p, x0, y0, 0f).setColor(color).setLight(light);
    }
}
