/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.proxy.client;

import java.util.UUID;

import java.util.ArrayList;

import net.minecraft.world.phys.AABB;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.core.BlockPos;

import net.minecraft.client.Minecraft;

import net.neoforged.neoforge.client.event.ClientTickEvent;

import com.buuz135.industrial.item.infinity.OneThreeFiveHandler;

import com.buuz135.industrial.block.generator.tile.MycelialReactorTile;
import com.buuz135.industrial.block.tile.IndustrialAreaWorkingTile;
import com.buuz135.industrial.block.transportstorage.tile.ConveyorTile;
import com.buuz135.industrial.block.transportstorage.tile.TransporterTile;
import com.buuz135.industrial.entity.InfinityLauncherProjectileEntity;
import com.buuz135.industrial.entity.InfinityNukeEntity;
import com.buuz135.industrial.entity.InfinityTridentEntity;
import com.buuz135.industrial.entity.client.*;
import com.buuz135.industrial.module.*;
import com.buuz135.industrial.proxy.CommonProxy;
import com.buuz135.industrial.proxy.client.event.IFClientEvents;
import com.buuz135.industrial.proxy.client.particle.ParticleVex;
import com.buuz135.industrial.proxy.client.render.*;
import com.buuz135.industrial.proxy.client.render.item.HydroponicSimProcessorRenderer;
import com.buuz135.industrial.utils.Reference;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.BlockWithTile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterParticleGroupsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Calendar;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public static KeyMapping OPEN_BACKPACK;

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        registerAreaRender(event, ModuleCore.FLUID_EXTRACTOR);
        registerAreaRender(event, ModuleAgricultureHusbandry.PLANT_GATHERER);
        registerAreaRender(event, ModuleAgricultureHusbandry.PLANT_SOWER);
        registerAreaRender(event, ModuleAgricultureHusbandry.SEWER);
        registerAreaRender(event, ModuleAgricultureHusbandry.PLANT_FERTILIZER);
        registerAreaRender(event, ModuleAgricultureHusbandry.SLAUGHTER_FACTORY);
        registerAreaRender(event, ModuleAgricultureHusbandry.ANIMAL_RANCHER);
        registerAreaRender(event, ModuleAgricultureHusbandry.ANIMAL_FEEDER);
        registerAreaRender(event, ModuleAgricultureHusbandry.ANIMAL_BABY_SEPARATOR);
        registerAreaRender(event, ModuleAgricultureHusbandry.MOB_CRUSHER);
        registerAreaRender(event, ModuleAgricultureHusbandry.WITHER_BUILDER);
        registerAreaRender(event, ModuleMisc.STASIS_CHAMBER);
        registerAreaRender(event, ModuleResourceProduction.LASER_DRILL);
        registerAreaRender(event, ModuleAgricultureHusbandry.MOB_DUPLICATOR);
        registerAreaRender(event, ModuleMisc.MOB_DETECTOR);

        event.registerBlockEntityRenderer((BlockEntityType<? extends MycelialReactorTile>) ModuleGenerator.MYCELIAL_REACTOR.type().get(), MycelialReactorTESR::new);

        event.registerEntityRenderer((EntityType<? extends InfinityTridentEntity>) ModuleTool.TRIDENT_ENTITY_TYPE.value(), InfinityTridentRenderer::new);
        event.registerEntityRenderer((EntityType<? extends InfinityNukeEntity>) ModuleTool.INFINITY_NUKE_ENTITY_TYPE.value(), InfinityNukeRenderer::new);
        event.registerEntityRenderer((EntityType<? extends InfinityLauncherProjectileEntity>) ModuleTool.INFINITY_LAUNCHER_PROJECTILE_ENTITY_TYPE.value(), InfinityLauncherProjectileRenderer::new);

        event.registerBlockEntityRenderer(((BlockEntityType<? extends TransporterTile>) ModuleTransportStorage.TRANSPORTER.type().get()), TransporterTESR::new);

        event.registerBlockEntityRenderer(((BlockEntityType<? extends ConveyorTile>) ModuleTransportStorage.CONVEYOR.type().get()), FluidConveyorTESR::new);
    }

    private static void registerAreaRender(EntityRenderersEvent.RegisterRenderers event, BlockWithTile pair) {
        event.registerBlockEntityRenderer((BlockEntityType<? extends IndustrialAreaWorkingTile>) pair.type().get(), WorkingAreaTESR::new);
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (PlayerModelType skin : event.getSkins()) {
            AvatarRenderer<?> renderer = event.getPlayerRenderer(skin);
            renderer.addLayer(new InfinityLauncherProjectileArmorLayer(renderer, event.getContext()));
        }
    }

    @SubscribeEvent
    public static void layerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(InfinityTridentRenderer.TRIDENT_LAYER, InfinityTridentModel::createBodyLayer);
        event.registerLayerDefinition(InfinityNukeRenderer.NUKE_LAYER, InfinityNukeModel::createBodyLayer);
        event.registerLayerDefinition(InfinityNukeRenderer.NUKE_ARMED_LAYER, () -> InfinityNukeModelArmed.createBodyLayer(new CubeDeformation(0f)));
        event.registerLayerDefinition(InfinityNukeRenderer.NUKE_ARMED_BIG_LAYER, () -> InfinityNukeModelArmed.createBodyLayer(new CubeDeformation(0.2f)));
        event.registerLayerDefinition(InfinityLauncherProjectileRenderer.PROJECTILE_LAYER, InfinityLauncherProjectileModel::createBodyLayer);
        event.registerLayerDefinition(InfinityLauncherProjectileRenderer.PROJECTILE_STUCK_LAYER, InfinityLauncherProjectileModel::createStuckLayer);
    }

    @SubscribeEvent
    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "hydroponic_simulation_processor"), HydroponicSimProcessorRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerBlockTints(RegisterColorHandlersEvent.BlockTintSources event) {
        event.register(List.of(new IFItemModelParts.ConveyorTint()), ModuleTransportStorage.CONVEYOR.getBlock());
    }

    @SubscribeEvent
    public static void registerItemTints(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "mob_imprisonment_blacklisted"), IFItemModelParts.MobImprisonmentBlacklistedTint.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "infinity_tier"), IFItemModelParts.InfinityTierTint.MAP_CODEC);
        event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "fluid_content"), IFItemModelParts.FluidContentTint.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerItemProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "on_cooldown"), IFItemModelParts.OnCooldown.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerStandaloneModels(ModelEvent.RegisterStandalone event) {
        UpgradeModels.register(event);
    }

    @SubscribeEvent
    public static void registerParticleGroups(RegisterParticleGroupsEvent event) {
        event.register(ParticleVex.GROUP, ParticleVex.Group::new);
    }

    @Override
    public void run() {

        NeoForge.EVENT_BUS.register(new IFClientEvents());

        EventManager.forge(ItemTooltipEvent.class).filter(event -> BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).getNamespace().equals(Reference.MOD_ID)).process(event -> {
            if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1 && Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL) {
                event.getToolTip().add(Component.literal(Component.translatable("text.industrialforegoing.proxy.client.alt_f4").getString()).withStyle(ChatFormatting.DARK_AQUA));
            }
        }).subscribe();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.level() != null && !Minecraft.getInstance().isPaused() && Minecraft.getInstance().player.level().getGameTime() % 5 == 0) {
            BlockPos pos = new BlockPos(Minecraft.getInstance().player.blockPosition().getX(), Minecraft.getInstance().player.blockPosition().getY(), Minecraft.getInstance().player.blockPosition().getZ());
            Minecraft.getInstance().player.level().getEntitiesOfClass(LivingEntity.class, new AABB(pos.offset(32, 32, 32).getCenter(), pos.offset(-32, -32, -32).getCenter()),
                            input -> input.getUUID().toString().contains(OneThreeFiveHandler.SPECIAL)).
                    forEach(living -> Minecraft.getInstance().particleEngine.add(new ParticleVex(living)));
            Minecraft.getInstance().player.level().getEntitiesOfClass(Player.class, new AABB(pos.offset(32, 32, 32).getCenter(), pos.offset(-32, -32, -32).getCenter()),
                            input -> OneThreeFiveHandler.SPECIAL_ENTITIES.containsKey(input.getUUID())).
                    forEach(living -> Minecraft.getInstance().particleEngine.add(new ParticleVex(living)));
        }
        List<UUID> toRemove = new ArrayList<>();
        for (UUID uuid : OneThreeFiveHandler.SPECIAL_ENTITIES.keySet()) {
            if (System.currentTimeMillis() >= OneThreeFiveHandler.SPECIAL_ENTITIES.get(uuid) + 1000) {
                toRemove.add(uuid);
            }
        }
        toRemove.forEach(uuid -> OneThreeFiveHandler.SPECIAL_ENTITIES.remove(uuid));
    }
}
