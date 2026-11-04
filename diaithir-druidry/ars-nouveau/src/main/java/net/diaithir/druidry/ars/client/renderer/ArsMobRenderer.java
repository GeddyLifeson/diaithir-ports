package net.diaithir.druidry.ars.client.renderer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

/**
 * Placeholder renderer for the mobs of the Ars port. No third-party assets are used: the models are built in code from cubes and
 * tinted by a vanilla solid-colour texture. Behaviour, goals and animation state live on the entities; this only draws them.
 */
public class ArsMobRenderer<T extends Mob> extends MobRenderer<T, LivingEntityRenderState, ArsMobRenderer.ProceduralModel> {

    /** Body plans. */
    public enum Profile {
        /** Starbuncle, Drygmy: short quadruped with a big tail. */
        QUADRUPED(0.8f, "orange_wool", 0.3f),
        /** Whirlisprig, Wixie and their familiars: floating body with wings. */
        FLOATING(0.7f, "lime_wool", 0.2f),
        /** Golem, Weald walkers, Wilden, decoys: upright biped. */
        BIPED(1.0f, "purple_wool", 0.5f),
        /** Wilden chimera and large things. */
        LARGE(1.8f, "red_wool", 0.9f);

        final float scale;
        final String block;
        final float shadow;

        Profile(float scale, String block, float shadow) {
            this.scale = scale;
            this.block = block;
            this.shadow = shadow;
        }
    }

    private final Identifier texture;
    private final float scale;

    public ArsMobRenderer(EntityRendererProvider.Context ctx, Profile profile, String blockTexture) {
        super(ctx, new ProceduralModel(profile), profile.shadow);
        this.texture = Identifier.withDefaultNamespace("textures/block/" + (blockTexture == null ? profile.block : blockTexture) + ".png");
        this.scale = profile.scale;
    }

    public static <E extends Mob> EntityRendererProvider<E> of(Profile profile, String blockTexture) {
        return ctx -> new ArsMobRenderer<>(ctx, profile, blockTexture);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return texture;
    }

    @Override
    public void extractRenderState(T entity, LivingEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.scale = scale;
    }

    /** Head, body, four limbs and a tail, posed from the walk cycle and head rotation. */
    public static class ProceduralModel extends EntityModel<LivingEntityRenderState> {
        private final ModelPart head;
        private final ModelPart limbA;
        private final ModelPart limbB;
        private final ModelPart limbC;
        private final ModelPart limbD;
        private final ModelPart tail;
        private final boolean floating;

        ProceduralModel(Profile profile) {
            super(createRoot(profile));
            this.head = root.getChild("head");
            this.limbA = root.getChild("limb_a");
            this.limbB = root.getChild("limb_b");
            this.limbC = root.getChild("limb_c");
            this.limbD = root.getChild("limb_d");
            this.tail = root.getChild("tail");
            this.floating = profile == Profile.FLOATING;
        }

        private static ModelPart createRoot(Profile profile) {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition p = mesh.getRoot();
            boolean biped = profile == Profile.BIPED || profile == Profile.LARGE;
            float bodyH = biped ? 12 : 6;
            float bodyW = biped ? 8 : 6;
            // 16x16 texture, every face samples the same solid-colour tile
            p.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-bodyW / 2, -bodyH, -2, bodyW, bodyH, 4), PartPose.offset(0, 24 - (biped ? 12 : 4), 0));
            p.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -6, -3, 6, 6, 6), PartPose.offset(0, 24 - (biped ? 12 : 4) - bodyH, 0));
            float legY = 24 - (biped ? 12 : 4);
            float legLen = biped ? 12 : 4;
            if (profile == Profile.FLOATING) {
                // wings instead of legs
                p.addOrReplaceChild("limb_a", CubeListBuilder.create().texOffs(0, 0).addBox(0, -4, 0, 6, 8, 1), PartPose.offset(3, legY - 4, 2));
                p.addOrReplaceChild("limb_b", CubeListBuilder.create().texOffs(0, 0).addBox(-6, -4, 0, 6, 8, 1), PartPose.offset(-3, legY - 4, 2));
                p.addOrReplaceChild("limb_c", CubeListBuilder.create(), PartPose.ZERO);
                p.addOrReplaceChild("limb_d", CubeListBuilder.create(), PartPose.ZERO);
            } else {
                p.addOrReplaceChild("limb_a", CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, legLen, 2), PartPose.offset(-bodyW / 2 + 1, legY, 1));
                p.addOrReplaceChild("limb_b", CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, legLen, 2), PartPose.offset(bodyW / 2 - 1, legY, 1));
                p.addOrReplaceChild("limb_c", CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, legLen, 2), PartPose.offset(-bodyW / 2 + 1, legY, -1));
                p.addOrReplaceChild("limb_d", CubeListBuilder.create().texOffs(0, 0).addBox(-1, 0, -1, 2, legLen, 2), PartPose.offset(bodyW / 2 - 1, legY, -1));
            }
            p.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5f, -1.5f, 0, 3, 3, 6), PartPose.offset(0, legY - 2, 2));
            return LayerDefinition.create(mesh, 16, 16).bakeRoot();
        }

        @Override
        public void setupAnim(LivingEntityRenderState state) {
            super.setupAnim(state);
            head.yRot = state.yRot * Mth.DEG_TO_RAD;
            head.xRot = state.xRot * Mth.DEG_TO_RAD;
            float swing = Mth.cos(state.walkAnimationPos * 0.6662f) * 1.4f * state.walkAnimationSpeed;
            if (floating) {
                float flap = Mth.cos(state.ageInTicks * 1.2f) * 0.6f;
                limbA.zRot = 0.4f + flap;
                limbB.zRot = -0.4f - flap;
                root.y = Mth.sin(state.ageInTicks * 0.1f) * 1.5f - 6;
            } else {
                limbA.xRot = swing;
                limbB.xRot = -swing;
                limbC.xRot = -swing;
                limbD.xRot = swing;
            }
            tail.xRot = 0.3f + Mth.cos(state.ageInTicks * 0.15f) * 0.1f;
        }
    }
}
