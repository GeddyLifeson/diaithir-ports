package com.github.alexmodguy.alexscaves.client.render;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Glow composite targets (upstream's PostEffectRegistry irradiated and purple witch chains) on the 26.1 render pipeline.
 * Render types for a channel draw into the channel's own render target (via an OutputTarget). After the translucent features the
 * target is blurred with the vanilla box_blur shader and composited onto the main target by a post chain
 * (post_effect/glow_irradiated.json, glow_witch.json).
 */
@EventBusSubscriber(modid = AlexsCaves.MODID, value = Dist.CLIENT)
public class GlowBloom {
    public static final Channel IRRADIATED = new Channel("irradiated_glow", "glow_irradiated");
    public static final Channel WITCH = new Channel("witch_glow", "glow_witch");
    private static final Channel[] CHANNELS = {IRRADIATED, WITCH};
    private static final CrossFrameResourcePool POOL = new CrossFrameResourcePool(3);

    public static class Channel {
        public final Identifier targetId;
        final Identifier chainId;
        public final OutputTarget output;
        private TextureTarget target;
        private boolean requested;

        Channel(String targetName, String chain) {
            this.targetId = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, targetName);
            this.chainId = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, chain);
            this.output = new OutputTarget("alexscaves_" + targetName, () -> target);
        }

        /** Called every frame by anything that is about to draw into this channel. */
        public void request() {
            requested = true;
        }

        private void prepare(RenderTarget main) {
            if (target == null) {
                target = new TextureTarget("AC " + targetId.getPath(), main.width, main.height, true);
            } else if (target.width != main.width || target.height != main.height) {
                target.resize(main.width, main.height);
            }
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(target.getColorTexture(), 0, target.getDepthTexture(), 1.0);
            target.copyDepthFrom(main);
        }

        private void composite(Minecraft mc, RenderTarget main) {
            PostChain chain = mc.getShaderManager().getPostChain(chainId, Set.of(PostChain.MAIN_TARGET_ID, targetId));
            if (chain == null) return;
            FrameGraphBuilder frame = new FrameGraphBuilder();
            Map<Identifier, ResourceHandle<RenderTarget>> handles = new HashMap<>();
            handles.put(PostChain.MAIN_TARGET_ID, frame.importExternal("main", main));
            handles.put(targetId, frame.importExternal(targetId.getPath(), target));
            chain.addToFrame(frame, main.width, main.height, new PostChain.TargetBundle() {
                @Override
                public void replace(Identifier id, ResourceHandle<RenderTarget> handle) {
                    handles.put(id, handle);
                }

                @Override
                public ResourceHandle<RenderTarget> get(Identifier id) {
                    return handles.get(id);
                }
            });
            frame.execute(POOL);
            POOL.endFrame();
        }
    }

    @SubscribeEvent
    public static void afterOpaqueFeatures(RenderLevelStageEvent.AfterOpaqueFeatures event) {
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        for (Channel c : CHANNELS) {
            if (c.requested) c.prepare(main);
        }
    }

    @SubscribeEvent
    public static void afterTranslucentFeatures(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget main = mc.getMainRenderTarget();
        for (Channel c : CHANNELS) {
            if (c.requested && c.target != null) c.composite(mc, main);
            c.requested = false;
        }
    }
}
