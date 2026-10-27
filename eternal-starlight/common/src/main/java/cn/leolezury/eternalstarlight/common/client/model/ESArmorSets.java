package cn.leolezury.eternalstarlight.common.client.model;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.ArmorModelSet;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Helpers for the per-slot armor model sets introduced in 26.1.
 */
public final class ESArmorSets {
	public static final CubeDeformation INNER_DEFORMATION = new CubeDeformation(0.5F);
	public static final CubeDeformation OUTER_DEFORMATION = new CubeDeformation(1.0F);

	private ESArmorSets() {
	}

	public static ArmorModelSet<ModelLayerLocation> create(String name) {
		return new ArmorModelSet<>(
			new ModelLayerLocation(EternalStarlight.id(name), "helmet"),
			new ModelLayerLocation(EternalStarlight.id(name), "chestplate"),
			new ModelLayerLocation(EternalStarlight.id(name), "leggings"),
			new ModelLayerLocation(EternalStarlight.id(name), "boots")
		);
	}

	public static void register(ArmorModelSet<ModelLayerLocation> locations, BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registration) {
		register(locations, INNER_DEFORMATION, OUTER_DEFORMATION, registration);
	}

	public static void register(ArmorModelSet<ModelLayerLocation> locations, CubeDeformation inner, CubeDeformation outer, BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> registration) {
		registration.accept(locations.head(), () -> LayerDefinition.create(HumanoidModel.createArmorMeshSet(inner, outer).head(), 64, 32));
		registration.accept(locations.chest(), () -> LayerDefinition.create(HumanoidModel.createArmorMeshSet(inner, outer).chest(), 64, 32));
		registration.accept(locations.legs(), () -> LayerDefinition.create(HumanoidModel.createArmorMeshSet(inner, outer).legs(), 64, 32));
		registration.accept(locations.feet(), () -> LayerDefinition.create(HumanoidModel.createArmorMeshSet(inner, outer).feet(), 64, 32));
	}
}
