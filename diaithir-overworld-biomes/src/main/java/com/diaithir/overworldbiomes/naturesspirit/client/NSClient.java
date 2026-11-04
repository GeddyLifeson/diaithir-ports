package com.diaithir.overworldbiomes.naturesspirit.client;

import net.minecraft.world.item.Item;

import com.diaithir.overworldbiomes.naturesspirit.client.render.CheeseArrowEntityRenderer;
import com.diaithir.overworldbiomes.naturesspirit.registration.*;
import net.minecraft.client.particle.SuspendedParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Client-side setup ported from the Fabric ClientModInitializer.
 * Call {@link #init(IEventBus)} from the module's client setup with the mod event bus.
 * Render layers are now declared through block model/JSON render_type in 26.1, so the
 * former BlockRenderLayerMap calls are intentionally not reproduced here.
 */
@EventBusSubscriber(modid = "natures_spirit", value = Dist.CLIENT)
public class NSClient {

  public static void init(IEventBus modBus) {
    modBus.addListener(NSClient::registerRenderers);
    modBus.addListener(NSClient::registerParticles);
  }

  public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(NSEntityTypes.CHEESE_ARROW.get(), CheeseArrowEntityRenderer::new);
  }

  public static void registerParticles(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(NSParticleTypes.RED_MAPLE_LEAVES_PARTICLE_HOLDER.get(),
        sprites -> (options, level, x, y, z, xa, ya, za, random) -> new MapleLeavesParticle(level, x, y, z, sprites.get(random)));
    event.registerSpriteSet(NSParticleTypes.ORANGE_MAPLE_LEAVES_PARTICLE_HOLDER.get(),
        sprites -> (options, level, x, y, z, xa, ya, za, random) -> new MapleLeavesParticle(level, x, y, z, sprites.get(random)));
    event.registerSpriteSet(NSParticleTypes.YELLOW_MAPLE_LEAVES_PARTICLE_HOLDER.get(),
        sprites -> (options, level, x, y, z, xa, ya, za, random) -> new MapleLeavesParticle(level, x, y, z, sprites.get(random)));
    event.registerSpriteSet(NSParticleTypes.MILK_PARTICLE_HOLDER.get(), SuspendedParticle.UnderwaterProvider::new);
    event.registerSpriteSet(NSParticleTypes.CALCITE_BUBBLE_PARTICLE_HOLDER.get(), net.minecraft.client.particle.BubbleParticle.Provider::new);
  }
}
