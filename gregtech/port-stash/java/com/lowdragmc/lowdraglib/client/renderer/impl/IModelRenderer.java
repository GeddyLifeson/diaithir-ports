package com.lowdragmc.lowdraglib.client.renderer.impl;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.resources.model.sprite.Material;
import com.lowdragmc.lowdraglib.LDLib;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;
import com.lowdragmc.lowdraglib.client.renderer.ISerializableRenderer;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.StringConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DialogWidget;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.minecraft.util.TriState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@LDLRegisterClient(name = "json_model", group = "renderer")
public class IModelRenderer implements ISerializableRenderer {
    @Getter
    @Configurable(forceUpdate = false)
    protected Identifier modelLocation;/* ported: removed *//* ported: removed *//* ported: removed */

    protected IModelRenderer() {
        modelLocation = Identifier.withDefaultNamespace("block/furnace");
    }

    public IModelRenderer(Identifier modelLocation) {
        this.modelLocation = modelLocation;
        initRenderer();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @Nonnull
    public TextureAtlasSprite getParticleTexture() { return null; /* ported stub */ }

    @OnlyIn(Dist.CLIENT)
    protected UnbakedModel getModel() {
        return ModelFactory.getUnBakedModel(modelLocation);
    }/* ported: removed */

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean useBlockLight(ItemStack stack) {
        var model = getItemBakedModel(stack);
        if (model != null) {
            return model.usesBlockLight();
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public TriState useAO() {
        var model = getItemBakedModel();
        if (model != null) {
            return model.useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;
        }
        return TriState.FALSE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<BakedQuad> renderModel(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        var ibakedmodel = getBlockBakedModel(level, pos, state);
        if (ibakedmodel == null) return Collections.emptyList();
        return ibakedmodel.getQuads(state, side, rand, data, renderType);
    }/* ported: removed *//* ported: removed */

    @OnlyIn(Dist.CLIENT)
    @Nullable
    @Deprecated(forRemoval = true, since = "1.21")
    protected BakedModel getBlockBakedModel(@Nullable BlockPos pos, @Nullable BlockAndTintGetter level) {
        return getRotatedModel(Direction.NORTH);
    }/* ported: removed */

    @OnlyIn(Dist.CLIENT)
    @Deprecated(forRemoval = true, since = "1.21")
    public BakedModel getRotatedModel(Direction frontFacing) {
        return blockModels.computeIfAbsent(frontFacing, facing -> getModel().bake(
                ModelFactory.getModelBaker(),
                this::materialMapping,
                ModelFactory.getRotation(facing)));
    }

    @OnlyIn(Dist.CLIENT)
    protected TextureAtlasSprite materialMapping(Material material) { return null; /* ported stub */ }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(Identifier atlasName, Consumer<Identifier> register) {
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) {
            itemModel = null;
            blockModels.clear();
            modelCaches.clear();
        }
    }/* ported: removed */

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isGui3d() {
        var model = getItemBakedModel();
        if (model == null) {
            return ISerializableRenderer.super.isGui3d();
        }
        return model.isGui3d();
    }

    // ISerializableRenderer
    public void initRenderer() {
        if (this.modelLocation != null) {
            if (LDLib.isClient()) {
                blockModels = new ConcurrentHashMap<>();
                modelCaches = new ConcurrentHashMap<>();
                registerEvent();
            }
        }
    }

    @ConfigSetter(field = "modelLocation")
    @SuppressWarnings("unused")
    public void updateModelWithoutReloadingResource(Identifier modelLocation) {
        this.modelLocation = modelLocation;
        if (LDLib.isClient()) {
            itemModel = null;
            if (blockModels != null) blockModels.clear();
            if (modelCaches != null) modelCaches.clear();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void updateModelWithReloadingResource(Identifier modelLocation) {  /* ported stub */ }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {  /* ported stub */ }

    private static Identifier getModelFromFile(File path, File r){
        var id = path.getPath().replace('\\', '/').split("assets/")[1].split("/")[0];
        return Identifier.fromNamespaceAndPath(id, r.getPath().replace(path.getPath(), "").replace(".json", "").replace('\\', '/').substring(1));
    }
}
