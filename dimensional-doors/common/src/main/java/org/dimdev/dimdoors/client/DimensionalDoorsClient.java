package org.dimdev.dimdoors.client;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.dimdev.dimdoors.DimensionalDoors;
import org.dimdev.dimdoors.block.entity.ModBlockEntityTypes;
import org.dimdev.dimdoors.entity.MaskEntity;
import org.dimdev.dimdoors.entity.ModEntityTypes;
import org.dimdev.dimdoors.network.client.ClientPacketListener;
import org.dimdev.dimdoors.network.packet.c2s.NetworkHandlerInitializedC2SPacket;
import org.dimdev.dimdoors.particle.client.LimboAshParticle;
import org.dimdev.dimdoors.particle.client.MonolithParticle;
import org.dimdev.dimdoors.particle.client.RiftParticle;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.dimdev.dimdoors.particle.ModParticleTypes.*;

public class DimensionalDoorsClient {
    public static final Identifier childItem = DimensionalDoors.id("item/child_item");

    private static IClientSided sided;

    public static void init(IClientSided sided) {
        DimensionalDoorsClient.sided = sided;
        sided.onClientPlayerJoin(() -> ClientPacketListener.sendPacket(new NetworkHandlerInitializedC2SPacket()));

//    ModFluids.initClient();

        registerListeners();

//    ModRecipeBookGroups.init();
    }

    public static void initEntitiesClient(BiConsumer<EntityType, EntityRendererProvider> consumer, BiConsumer<BlockEntityType, BlockEntityRendererProvider> blockConsumer) {
        consumer.accept(ModEntityTypes.MONOLITH, MonolithRenderer::new);
        consumer.accept(ModEntityTypes.MASK, context -> new EntityRenderer<MaskEntity, EntityRenderState>(context) {
            @Override
            public EntityRenderState createRenderState() {
                return new EntityRenderState();
            }
        });

        blockConsumer.accept(ModBlockEntityTypes.ENTRANCE_RIFT, EntranceRiftBlockEntityRenderer::new);
        blockConsumer.accept(ModBlockEntityTypes.DETACHED_RIFT, DetachedRiftBlockEntityRenderer::new);
    }

    private static void registerListeners() {
    }

    public static void initParticles(BiConsumer<ParticleType<? extends ParticleOptions>, ParticleProvider<?>> specialProvider, BiConsumer<ParticleType<?>, Function<SpriteSet, ? extends ParticleProvider<? extends ParticleOptions>>> spriteProivder) {
        specialProvider.accept(MONOLITH, (particleOptions, clientLevel, x, y, z, g, h, i, random) -> new MonolithParticle(clientLevel, x, y, z));
        spriteProivder.accept(RIFT, RiftParticle.Factory::new);
        spriteProivder.accept(LIMBO_ASH, LimboAshParticle.Factory::new);
    }


    public static IClientSided getClientSided() {
        return sided;
    }
}
