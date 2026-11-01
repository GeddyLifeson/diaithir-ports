package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.layered.*;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.item.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/** Draws Alex's Caves' custom 3D armour meshes on humanoid renderers (the vanilla equipment layers cannot hold extra geometry). */
public class CustomArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
    private static final Identifier DARKNESS_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/armor/darkness_armor_glow.png");

    private record Kind(ModelLayerLocation layer, Function<ModelPart, HumanoidModel<HumanoidRenderState>> factory, String tex, boolean pair) {}

    private static final Map<Class<?>, Kind> KINDS = new java.util.LinkedHashMap<>();
    static {
        KINDS.put(PrimordialArmorItem.class, new Kind(ACModelLayers.PRIMORDIAL_ARMOR, PrimordialArmorModel::new, "primordial_armor", true));
        KINDS.put(HazmatArmorItem.class, new Kind(ACModelLayers.HAZMAT_ARMOR, HazmatArmorModel::new, "hazmat_suit", true));
        KINDS.put(DivingArmorItem.class, new Kind(ACModelLayers.DIVING_ARMOR, DivingArmorModel::new, "diving_suit", true));
        KINDS.put(DarknessArmorItem.class, new Kind(ACModelLayers.DARKNESS_ARMOR, DarknessArmorModel::new, "darkness_armor", false));
        KINDS.put(RainbounceBootsItem.class, new Kind(ACModelLayers.RAINBOUNCE_ARMOR, RainbounceArmorModel::new, "rainbounce_boots", false));
        KINDS.put(GingerbreadArmorItem.class, new Kind(ACModelLayers.GINGERBREAD_ARMOR, GingerbreadArmorModel::new, "gingerbread_armor", true));
    }

    private final Map<Kind, Map<EquipmentSlot, HumanoidModel<HumanoidRenderState>>> models = new java.util.HashMap<>();

    public CustomArmorLayer(RenderLayerParent<S, M> parent) {
        super(parent);
    }

    private HumanoidModel<HumanoidRenderState> model(Kind kind, EquipmentSlot slot) {
        return models.computeIfAbsent(kind, k -> new EnumMap<>(EquipmentSlot.class)).computeIfAbsent(slot, s -> {
            HumanoidModel<HumanoidRenderState> m = kind.factory().apply(Minecraft.getInstance().getEntityModels().bakeLayer(kind.layer()));
            m.head.visible = false; m.hat.visible = false; m.body.visible = false; m.rightArm.visible = false; m.leftArm.visible = false; m.rightLeg.visible = false; m.leftLeg.visible = false;
            switch (s) {
                case HEAD -> m.head.visible = true;
                case CHEST -> { m.body.visible = true; m.rightArm.visible = true; m.leftArm.visible = true; }
                case LEGS -> { m.body.visible = true; m.rightLeg.visible = true; m.leftLeg.visible = true; }
                case FEET -> { m.rightLeg.visible = true; m.leftLeg.visible = true; }
                default -> { }
            }
            return m;
        });
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        piece(poseStack, collector, light, state, state.chestEquipment, EquipmentSlot.CHEST);
        piece(poseStack, collector, light, state, state.legsEquipment, EquipmentSlot.LEGS);
        piece(poseStack, collector, light, state, state.feetEquipment, EquipmentSlot.FEET);
        piece(poseStack, collector, light, state, state.headEquipment, EquipmentSlot.HEAD);
    }

    private void piece(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, ItemStack stack, EquipmentSlot slot) {
        if (stack.isEmpty()) return;
        Kind kind = null;
        for (Map.Entry<Class<?>, Kind> e : KINDS.entrySet()) {
            if (e.getKey().isInstance(stack.getItem())) { kind = e.getValue(); break; }
        }
        if (kind == null) return;
        HumanoidModel<HumanoidRenderState> m = model(kind, slot);
        String path = kind.pair() ? "textures/armor/" + kind.tex() + (slot == EquipmentSlot.LEGS ? "_1.png" : "_0.png") : "textures/armor/" + kind.tex() + ".png";
        Identifier tex = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, path);
        boolean darkness = stack.getItem() instanceof DarknessArmorItem;
        boolean rainbounce = stack.getItem() instanceof RainbounceBootsItem;
        RenderType rt = darkness ? RenderTypes.entityTranslucent(tex) : rainbounce ? ACRenderTypes.getTeslaBulb(tex) : RenderTypes.entityCutout(tex);
        collector.submitModel(m, state, poseStack, rt, rainbounce ? 240 : light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        if (darkness) {
            collector.order(1).submitModel(m, state, poseStack, ACRenderTypes.getEyesAlphaEnabled(DARKNESS_GLOW), 240, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        }
    }
}
