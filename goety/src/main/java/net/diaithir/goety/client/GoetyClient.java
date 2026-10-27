package net.diaithir.goety.client;

import net.diaithir.goety.Goety;
import net.diaithir.goety.GoetyEntities;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Renderers for the servants: the vanilla models with the textures of the original mod. */
@EventBusSubscriber(modid = Goety.MOD_ID, value = Dist.CLIENT)
public final class GoetyClient {
    private static Identifier tex(String path) {
        return Identifier.fromNamespaceAndPath(Goety.MOD_ID, "textures/entity/servants/" + path + ".png");
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        Identifier zombie = tex("zombie/zombie_servant"), husk = tex("zombie/husk_servant");
        Identifier skeleton = tex("skeleton/skeleton_servant"), stray = tex("skeleton/stray_servant"), wither = tex("skeleton/wither_skeleton_servant");
        Identifier spider = tex("spider/spider_servant"), cave = tex("spider/cave_spider_servant");
        e.registerEntityRenderer(GoetyClient.<Zombie>type("zombie_servant"), ctx -> new ZombieRenderer(ctx) {
            @Override public Identifier getTextureLocation(ZombieRenderState s) { return zombie; }
        });
        e.registerEntityRenderer(GoetyClient.<Zombie>type("husk_servant"), ctx -> new HuskRenderer(ctx) {
            @Override public Identifier getTextureLocation(ZombieRenderState s) { return husk; }
        });
        e.registerEntityRenderer(GoetyClient.<Skeleton>type("skeleton_servant"), ctx -> new SkeletonRenderer(ctx) {
            @Override public Identifier getTextureLocation(SkeletonRenderState s) { return skeleton; }
        });
        e.registerEntityRenderer(GoetyClient.<Stray>type("stray_servant"), ctx -> new StrayRenderer(ctx) {
            @Override public Identifier getTextureLocation(SkeletonRenderState s) { return stray; }
        });
        e.registerEntityRenderer(GoetyClient.<WitherSkeleton>type("wither_skeleton_servant"), ctx -> new WitherSkeletonRenderer(ctx) {
            @Override public Identifier getTextureLocation(SkeletonRenderState s) { return wither; }
        });
        e.registerEntityRenderer(GoetyClient.<Spider>type("spider_servant"), ctx -> new SpiderRenderer<Spider>(ctx) {
            @Override public Identifier getTextureLocation(LivingEntityRenderState s) { return spider; }
        });
        e.registerEntityRenderer(GoetyClient.<CaveSpider>type("cave_spider_servant"), ctx -> new CaveSpiderRenderer(ctx) {
            @Override public Identifier getTextureLocation(LivingEntityRenderState s) { return cave; }
        });
    }

    @SubscribeEvent
    public static void projectiles(EntityRenderersEvent.RegisterRenderers e) {
        GoetyEntities.PROJECTILES.values().forEach(t -> e.registerEntityRenderer(t.get(), NoopRenderer::new));
    }

    @SubscribeEvent
    public static void blockEntities(EntityRenderersEvent.RegisterRenderers e) {
        e.registerBlockEntityRenderer(net.diaithir.goety.GoetyBlockEntities.ALTAR.get(), ctx -> new HolderRenderer(ctx, 1.25F));
        e.registerBlockEntityRenderer(net.diaithir.goety.GoetyBlockEntities.PEDESTAL.get(), ctx -> new HolderRenderer(ctx, 1.1F));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Entity> EntityType<T> type(String id) {
        return (EntityType<T>) GoetyEntities.MOBS.get(id).get();
    }

    private GoetyClient() {}
}
