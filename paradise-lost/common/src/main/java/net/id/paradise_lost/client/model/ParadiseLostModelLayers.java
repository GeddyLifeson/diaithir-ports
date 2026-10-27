package net.id.paradise_lost.client.model;

import com.google.common.collect.Maps;
import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.client.model.armor.OrnateOlviteArmorModel;
import net.id.paradise_lost.client.model.entity.*;
import net.id.paradise_lost.client.rendering.block.PalaceDoorBlockEntityRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class ParadiseLostModelLayers {

    public static final LayerDefinition BIPED_MODEL_DATA = LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64);
    public static final ArmorModelSet<LayerDefinition> ARMOR_MODEL_DATA = HumanoidModel
            .createArmorMeshSet(new CubeDeformation(0.5F), new CubeDeformation(1.0F))
            .map(mesh -> LayerDefinition.create(mesh, 64, 32));

    public static final Map<ModelLayerLocation, LayerDefinition> ENTRIES = Maps.newHashMap();

    public static final ModelLayerLocation ENVOY = register("envoy", "main", SkeletonModel.createBodyLayer());
    public static final ArmorModelSet<ModelLayerLocation> ENVOY_ARMOR = registerArmorSet("envoy", ARMOR_MODEL_DATA);
    public static final ModelLayerLocation SENTINEL = register("sentinel", "main", BIPED_MODEL_DATA);
    public static final ArmorModelSet<ModelLayerLocation> SENTINEL_ARMOR = registerArmorSet("sentinel", ARMOR_MODEL_DATA);
    public static final ModelLayerLocation QUINT = register("quint", "main", QuintEntityModel.getTexturedModelData());
    public static final ModelLayerLocation MOA = register("moa", "main", MoaModel.getTexturedModelData());
    public static final ModelLayerLocation POPOM = register("popom", "main", PopomEntityModel.getTexturedModelData());

    public static final ModelLayerLocation ORNATE_OLVITE_ARMOR = register("ornate_olvite", "main", OrnateOlviteArmorModel.getTexturedModelData());

    public static final ModelLayerLocation PALACE_DOOR = register("palace_door", "main", PalaceDoorBlockEntityRenderer.getTexturedModelData());

    public static ModelLayerLocation register(Identifier id, String layer, LayerDefinition data) {
        ModelLayerLocation entityModelLayer = new ModelLayerLocation(id, layer);
        if (ENTRIES.put(entityModelLayer, data) != null) {
            throw new IllegalStateException("Duplicate registration for " + entityModelLayer);
        }
        return entityModelLayer;
    }

    public static ModelLayerLocation register(String id, String layer, LayerDefinition data) {
        return register(ModConstants.locate(id), layer, data);
    }

    public static ArmorModelSet<ModelLayerLocation> registerArmorSet(String id, ArmorModelSet<LayerDefinition> data) {
        return new ArmorModelSet<>(
                register(id, "helmet", data.head()),
                register(id, "chestplate", data.chest()),
                register(id, "leggings", data.legs()),
                register(id, "boots", data.feet()));
    }

    public static void initClient() {}
}
