package com.diaithir.overworldbiomes.naturesspirit.client;

import com.diaithir.overworldbiomes.naturesspirit.client.render.CheeseArrowEntityRenderer;
import com.diaithir.overworldbiomes.naturesspirit.client.render.NSEntityModelLayers;
import com.diaithir.overworldbiomes.naturesspirit.client.render.PizzaBlockEntityRenderer;
import com.diaithir.overworldbiomes.naturesspirit.client.render.PizzaToppingModel;
import com.diaithir.overworldbiomes.naturesspirit.registration.*;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.particle.SuspendedParticle;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
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
    modBus.addListener(NSClient::registerBlockColors);
    modBus.addListener(NSClient::registerItemColors);
    modBus.addListener(NSClient::registerRenderers);
    modBus.addListener(NSClient::registerLayerDefinitions);
    modBus.addListener(NSClient::registerParticles);
  }

  private static final BlockColor GRASS_TINT = (blockState, level, blockPos, i) -> level != null && blockPos != null ? BiomeColors.getAverageGrassColor(level,
      blockState.hasProperty(DoublePlantBlock.HALF) && blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? blockPos.below() : blockPos
  ) : -1;

  private static final BlockColor SIMPLE_GRASS_TINT = (blockState, level, blockPos, i) -> level != null && blockPos != null ? BiomeColors.getAverageGrassColor(level,
      blockPos
  ) : -1;

  private static final BlockColor FOLIAGE_TINT = (blockState, level, blockPos, i) -> level != null && blockPos != null ? BiomeColors.getAverageFoliageColor(level,
      blockPos
  ) : -1;

  private static final BlockColor LOTUS_FLOWER_TINT = (blockState, level, blockPos, i) -> level != null && blockPos != null ? BiomeColors.getAverageGrassColor(level,
      new BlockPos(blockPos.getX(), -64, blockPos.getZ())
  ) : -1;

  public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
    event.register(GRASS_TINT, NSMiscBlocks.CATTAIL);
    event.register(FOLIAGE_TINT, NSWoods.SUGI.getLeaves());
    event.register(FOLIAGE_TINT, NSWoods.MAHOGANY.getLeaves());
    event.register(FOLIAGE_TINT, NSWoods.LARCH.getLeaves());
    event.register(FOLIAGE_TINT, NSWoods.ASPEN.getLeaves());
    event.register(SIMPLE_GRASS_TINT, NSMiscBlocks.LOTUS_STEM);
    event.register(SIMPLE_GRASS_TINT, NSMiscBlocks.LARGE_LUSH_FERN);
    event.register(SIMPLE_GRASS_TINT, NSMiscBlocks.LUSH_FERN);
    event.register(SIMPLE_GRASS_TINT, NSMiscBlocks.POTTED_LUSH_FERN);
    event.register(LOTUS_FLOWER_TINT, NSMiscBlocks.LOTUS_FLOWER);
  }

  public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
    ItemColor foliage = (stack, tintIndex) -> FoliageColor.FOLIAGE_DEFAULT;
    ItemColor grass = (stack, tintIndex) -> GrassColor.getDefaultColor();
    event.register(foliage, NSWoods.SUGI.getLeaves());
    event.register(foliage, NSWoods.LARCH.getLeaves());
    event.register(foliage, NSWoods.MAHOGANY.getLeaves());
    event.register(foliage, NSWoods.ASPEN.getLeaves());
    event.register(grass, NSMiscBlocks.LUSH_FERN);
    event.register(grass, NSMiscBlocks.LARGE_LUSH_FERN);
  }

  public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    NSEntityModelLayers.registerEntityModelLayers();
    event.registerLayerDefinition(NSEntityModelLayers.PIZZA_TOPPING, PizzaToppingModel::getTexturedModelData);
  }

  public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerBlockEntityRenderer(NSMiscBlocks.PIZZA_BLOCK_ENTITY_TYPE, PizzaBlockEntityRenderer::new);
    event.registerEntityRenderer(NSEntityTypes.CHEESE_ARROW, CheeseArrowEntityRenderer::new);
  }

  public static void registerParticles(RegisterParticleProvidersEvent event) {
    event.registerSpriteSet(NSParticleTypes.RED_MAPLE_LEAVES_PARTICLE,
        (spriteProvider) -> (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> new MapleLeavesParticle(world, x, y, z, spriteProvider)
    );
    event.registerSpriteSet(NSParticleTypes.ORANGE_MAPLE_LEAVES_PARTICLE,
        (spriteProvider) -> (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> new MapleLeavesParticle(world, x, y, z, spriteProvider)
    );
    event.registerSpriteSet(NSParticleTypes.YELLOW_MAPLE_LEAVES_PARTICLE,
        (spriteProvider) -> (parameters, world, x, y, z, velocityX, velocityY, velocityZ) -> new MapleLeavesParticle(world, x, y, z, spriteProvider)
    );
    event.registerSpriteSet(NSParticleTypes.MILK_PARTICLE, SuspendedParticle.UnderwaterProvider::new);
  }
}
