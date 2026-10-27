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

	/**
	 * Armor item models contain the parts of every slot; since a model renders after the layer has moved on to the next slot, one model instance is kept per slot with a fixed visibility.
	 */
	public static void applySlotVisibility(HumanoidModel<?> model, net.minecraft.world.entity.EquipmentSlot slot) {
		model.head.visible = slot == net.minecraft.world.entity.EquipmentSlot.HEAD;
		model.hat.visible = slot == net.minecraft.world.entity.EquipmentSlot.HEAD;
		model.body.visible = slot == net.minecraft.world.entity.EquipmentSlot.CHEST || slot == net.minecraft.world.entity.EquipmentSlot.LEGS;
		model.rightArm.visible = slot == net.minecraft.world.entity.EquipmentSlot.CHEST;
		model.leftArm.visible = slot == net.minecraft.world.entity.EquipmentSlot.CHEST;
		model.rightLeg.visible = slot == net.minecraft.world.entity.EquipmentSlot.LEGS || slot == net.minecraft.world.entity.EquipmentSlot.FEET;
		model.leftLeg.visible = slot == net.minecraft.world.entity.EquipmentSlot.LEGS || slot == net.minecraft.world.entity.EquipmentSlot.FEET;
	}
}
