package net.id.paradise_lost.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import net.id.paradise_lost.entity.vehicle.ParadiseLostBoatType;
import net.id.paradise_lost.client.rendering.entity.ParadiseLostBoatRenderer;

import net.id.paradise_lost.block.blockentity.ParadiseLostBlockEntityTypes;
import net.id.paradise_lost.client.rendering.block.CalciteDecoratedPotBlockEntityRenderer;
import net.id.paradise_lost.client.rendering.block.CherineCampfireBlockEntityRenderer;
import net.id.paradise_lost.client.rendering.block.PalaceDoorBlockEntityRenderer;
import net.id.paradise_lost.client.rendering.block.IncubatorBlockEntityRenderer;
import net.id.paradise_lost.client.rendering.block.TreeTapBlockEntityRenderer;
import net.id.paradise_lost.client.rendering.entity.BlockLikeEntityRenderer;
import net.id.paradise_lost.client.rendering.entity.LevitaArrowRenderer;
import net.id.paradise_lost.client.rendering.entity.hostile.EnvoyEntityRenderer;
import net.id.paradise_lost.client.rendering.entity.hostile.QuintEntityRenderer;
import net.id.paradise_lost.client.rendering.entity.hostile.SentinelEntityRenderer;
import net.id.paradise_lost.client.rendering.entity.passive.MoaEntityRenderer;
import net.id.paradise_lost.client.rendering.entity.passive.PopomEntityRenderer;
import net.id.paradise_lost.registry.EntityRegistry;
import net.id.paradise_lost.block.blockentity.PalaceDoorBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ClientRegistration {
    private ClientRegistration() {}

    @FunctionalInterface
    public interface EntityRendererRegistrar {
        <E extends Entity> void register(EntityType<? extends E> type, EntityRendererProvider<E> provider);
    }

    @FunctionalInterface
    public interface BlockEntityRendererRegistrar {
        <E extends BlockEntity, S extends BlockEntityRenderState> void register(BlockEntityType<? extends E> type, BlockEntityRendererProvider<E, S> provider);
    }

    public static void registerRenderers(
            EntityRendererRegistrar entities,
            BlockEntityRendererRegistrar blockEntities,
            BlockEntityRendererProvider<PalaceDoorBlockEntity, PalaceDoorBlockEntityRenderer.State> palaceDoorRenderer
    ) {
        entities.register(EntityRegistry.FLOATING_BLOCK.get(), BlockLikeEntityRenderer::new);
        entities.register(EntityRegistry.SLIDER.get(), BlockLikeEntityRenderer::new);
        entities.register(EntityRegistry.ENVOY.get(), EnvoyEntityRenderer::new);
        entities.register(EntityRegistry.SENTINEL.get(), SentinelEntityRenderer::new);
        entities.register(EntityRegistry.MOA.get(), MoaEntityRenderer::new);
        entities.register(EntityRegistry.POPOM.get(), PopomEntityRenderer::new);
        entities.register(EntityRegistry.QUINT.get(), QuintEntityRenderer::new);
        entities.register(EntityRegistry.THROWN_NITRA.get(), ThrownItemRenderer::new);
        entities.register(EntityRegistry.LEVITA_ARROW.get(), LevitaArrowRenderer::new);
        for (ParadiseLostBoatType wood : ParadiseLostBoatType.values()) {
            registerBoat(entities, EntityRegistry.boatType(wood), wood.boatTexture(), false);
            registerBoat(entities, EntityRegistry.chestBoatType(wood), wood.chestBoatTexture(), true);
        }

        blockEntities.register(ParadiseLostBlockEntityTypes.INCUBATOR.get(), IncubatorBlockEntityRenderer::new);
        blockEntities.register(ParadiseLostBlockEntityTypes.CHERINE_CAMPFIRE.get(), CherineCampfireBlockEntityRenderer::new);
        blockEntities.register(ParadiseLostBlockEntityTypes.TREE_TAP.get(), TreeTapBlockEntityRenderer::new);
        blockEntities.register(ParadiseLostBlockEntityTypes.SIGN.get(), StandingSignRenderer::new);
        blockEntities.register(ParadiseLostBlockEntityTypes.HANGING_SIGN.get(), HangingSignRenderer::new);
        blockEntities.register(ParadiseLostBlockEntityTypes.CALCITE_DECORATED_POT.get(), CalciteDecoratedPotBlockEntityRenderer::new);
        blockEntities.register(ParadiseLostBlockEntityTypes.PALACE_DOOR.get(), palaceDoorRenderer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerBoat(EntityRendererRegistrar entities, EntityType<?> type, Identifier texture, boolean chestBoat) {
        EntityRendererProvider<AbstractBoat> provider = context -> new ParadiseLostBoatRenderer(context, texture, chestBoat);
        entities.register((EntityType) type, (EntityRendererProvider) provider);
    }
}
