package net.diaithir.activity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.objects.AtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;

/** Draws the icon line above another player's nameplate. Client only. */
final class ActivityRenderer {
    private static final ContextKey<String> KEY = new ContextKey<>(Identifier.fromNamespaceAndPath(DiaithirActivity.ID, "activity"));
    /** Pixel gap between nameplate lines, as vanilla uses for the score line. */
    private static final float LINE = 9.0F * 1.15F * 0.025F;

    private ActivityRenderer() {}

    static void register(IEventBus game, IEventBus mod) {
        // render data is wiped after the name tag event, so it is filled by a render state modifier instead
        mod.addListener(RegisterRenderStateModifiersEvent.class, e -> e.registerAvatarEntityModifier(new AvatarRenderStateModifier() {
            @Override
            public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
                extract(avatar, state);
            }
        }));
        game.addListener(RenderNameTagEvent.DoRender.class, ActivityRenderer::draw);
        game.addListener(ClientPlayerNetworkEvent.LoggingOut.class, e -> ClientActivity.clear());
    }

    /** Runs when the render state is filled: decide whether this player gets an activity line this frame. */
    private static void extract(Object avatar, EntityRenderState state) {
        if (!ActivityConfig.SHOW.get() || !(avatar instanceof Player other)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || other == mc.player) return;
        String id = ClientActivity.get(other.getId());
        if (id == null) return;
        double range = ActivityConfig.RANGE.get();
        if (mc.player.distanceToSqr(other) > range * range) return;
        state.setRenderData(KEY, id);
    }

    private static void draw(RenderNameTagEvent.DoRender e) {
        EntityRenderState state = e.getEntityRenderState();
        String id = state.getRenderData(KEY);
        Vec3 at = state.nameTagAttachment;
        // only beside the name line itself, not the score line
        if (id == null || at == null || e.getContent() != state.nameTag) return;
        ActivityData.Def def = DiaithirActivity.data().defs().get(id);
        if (def == null) return;
        Component line = line(def);
        float scale = ActivityConfig.ICON_SCALE.get().floatValue();
        PoseStack pose = e.getPoseStack();
        int rows = state.scoreText != null ? 2 : 1;
        pose.pushPose();
        pose.translate(at.x, at.y + 0.5 + LINE * rows, at.z);
        pose.scale(scale, scale, scale);
        // submitNameTag adds the attachment and half a block itself; cancel that so the scaled line stays anchored
        e.getSubmitNodeCollector().submitNameTag(pose, new Vec3(0, -0.5, 0), 0, line, !state.isDiscrete, state.lightCoords, state.distanceToCameraSq, e.getCameraRenderState());
        pose.popPose();
    }

    static Component line(ActivityData.Def def) {
        MutableComponent out = Component.object(new AtlasSprite(Identifier.parse(def.atlas()), Identifier.parse(def.sprite())));
        if (ActivityConfig.SHOW_LABEL.get()) {
            out.append(Component.literal(" ")).append(Component.translatable(def.label()).withColor(color()));
        }
        return out;
    }

    private static int color() {
        try {
            return Integer.parseInt(ActivityConfig.LABEL_COLOR.get().replace("#", ""), 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return 0xDEAA46;
        }
    }
}
