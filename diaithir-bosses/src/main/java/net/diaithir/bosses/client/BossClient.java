package net.diaithir.bosses.client;

import net.diaithir.bosses.BossMobs;
import net.diaithir.bosses.Data;
import net.diaithir.bosses.DiaithirBosses;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Renderers: the vanilla model of each body kind with the boss's own generated texture. */
public final class BossClient {
    public static void init(IEventBus bus) {
        bus.addListener(EntityRenderersEvent.RegisterRenderers.class, BossClient::renderers);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        for (Data.Profile p : Data.profiles().values()) {
            if (!p.ownsEntity()) continue;
            Identifier t = Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "textures/entity/" + p.id + ".png");
            EntityType type = (EntityType) DiaithirBosses.MOBS.get(p.id).get();
            switch (p.own.kind) {
                case "zombie" -> e.registerEntityRenderer(type, ctx -> new ZombieRenderer(ctx) {
                    @Override public Identifier getTextureLocation(ZombieRenderState s) { return t; }
                });
                case "illager" -> e.registerEntityRenderer(type, ctx -> new VindicatorRenderer(ctx) {
                    @Override public Identifier getTextureLocation(IllagerRenderState s) { return t; }
                });
                case "golem" -> e.registerEntityRenderer(type, ctx -> new IronGolemRenderer(ctx) {
                    @Override public Identifier getTextureLocation(IronGolemRenderState s) { return t; }
                });
                case "spider" -> e.registerEntityRenderer(type, ctx -> new SpiderRenderer(ctx) {
                    @Override public Identifier getTextureLocation(LivingEntityRenderState s) { return t; }
                });
                case "ravager" -> e.registerEntityRenderer(type, ctx -> new RavagerRenderer(ctx) {
                    @Override public Identifier getTextureLocation(RavagerRenderState s) { return t; }
                });
                case "blaze" -> e.registerEntityRenderer(type, ctx -> new BlazeRenderer(ctx) {
                    @Override public Identifier getTextureLocation(LivingEntityRenderState s) { return t; }
                });
                case "silverfish" -> e.registerEntityRenderer(type, ctx -> new SilverfishRenderer(ctx) {
                    @Override public Identifier getTextureLocation(LivingEntityRenderState s) { return t; }
                });
                case "phantom" -> e.registerEntityRenderer(type, ctx -> new PhantomRenderer(ctx) {
                    @Override public Identifier getTextureLocation(PhantomRenderState s) { return t; }
                });
                case "guardian" -> e.registerEntityRenderer(type, ctx -> new GuardianRenderer(ctx) {
                    @Override public Identifier getTextureLocation(GuardianRenderState s) { return t; }
                });
                case "hoglin" -> e.registerEntityRenderer(type, ctx -> new HoglinRenderer(ctx) {
                    @Override public Identifier getTextureLocation(HoglinRenderState s) { return t; }
                });
                default -> throw new IllegalStateException("no renderer for kind " + p.own.kind + " (" + BossMobs.KINDS.keySet() + ")");
            }
        }
    }

    private BossClient() {}
}
