package com.lowdragmc.lowdraglib.client;

import net.minecraft.client.resources.model.sprite.Material;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.lowdragmc.lowdraglib.CommonProxy;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;



import com.lowdragmc.lowdraglib.client.renderer.ATESRRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IRenderer;

import com.lowdragmc.lowdraglib.client.utils.WidgetClientTooltipComponent;
import com.lowdragmc.lowdraglib.core.mixins.ParticleEngineAccessor;
import com.lowdragmc.lowdraglib.core.mixins.accessor.ModelBakeryAccessor;
import com.lowdragmc.lowdraglib.gui.compass.CompassManager;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.WidgetTooltipComponent;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleProvider;

import net.minecraft.client.renderer.rendertype.RenderType;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public ClientProxy(IEventBus eventBus) {
        super(eventBus);
        eventBus.register(this);
    }


    /**
     * should be called when Minecraft is prepared.
     */
    public static void init() {
        Shaders.init();
        DrawerHelper.init();
        CompassManager.INSTANCE.init();
    }

    @SubscribeEvent
    public void onRegisterClientTooltipComponentFactoriesEvent(final RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(WidgetTooltipComponent.class, WidgetClientTooltipComponent::new);
    }

    @SubscribeEvent
    public void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {  /* ported stub */ }

    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent e) {  /* ported stub */ }

    @ApiStatus.Internal
    public static final Multimap<Identifier, Material> SCRAPED_TEXTURES = HashMultimap.create();/* ported: removed */

    @ApiStatus.Internal
    public static void textureScraped(Identifier modelLocation, Material material) {
        SCRAPED_TEXTURES.put(modelLocation, material);
    }/* ported: removed */

    @SubscribeEvent
    public void modelBake(final ModelEvent.ModifyBakingResult event) {  /* ported stub */ }/* ported: removed *//* ported: removed */

    public static ParticleProvider getProvider(ParticleType<?> type) {
        if (Minecraft.getInstance().particleEngine instanceof ParticleEngineAccessor accessor) {
            return accessor.getProviders().get(BuiltInRegistries.PARTICLE_TYPE.getKey(type));
        }
        return null;
    }

}
