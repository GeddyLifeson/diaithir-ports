package com.github.alexmodguy.alexscaves.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Diaithir: upstream's entity models are hand-built on Citadel's AdvancedModelBox and are not ported. Until they are, every
 * Alex's Caves entity is drawn as a box of its hitbox size textured with its own entity texture (or the missing texture).
 * Non-mob entities (boats, projectiles, bombs) are not drawn.
 */
public class FallbackRenderers {
    static class BoxModel extends EntityModel<LivingEntityRenderState> {
        BoxModel(ModelPart root) {
            super(root);
        }

        static BoxModel create(float w, float h) {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            int pw = Math.max(1, Math.round(w * 16)), ph = Math.max(1, Math.round(h * 16));
            root.addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 0).addBox(-pw / 2f, -ph, -pw / 2f, pw, ph, pw), PartPose.offset(0, 24, 0));
            return new BoxModel(LayerDefinition.create(mesh, 64, 64).bakeRoot());
        }
    }

    static class BoxRenderer<T extends Mob> extends net.minecraft.client.renderer.entity.MobRenderer<T, LivingEntityRenderState, BoxModel> {
        private final Identifier texture;
        private final float shadow;

        BoxRenderer(EntityRendererProvider.Context ctx, BoxModel model, Identifier texture, float shadow) {
            super(ctx, model, shadow);
            this.texture = texture;
            this.shadow = shadow;
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return texture;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register(EntityRenderersEvent.RegisterRenderers event, java.util.Set<EntityType<?>> skip) {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!AlexsCaves.MODID.equals(id.getNamespace()) || skip.contains(type)) continue;
            if (type.getCategory() == MobCategory.MISC) {
                event.registerEntityRenderer((EntityType) type, NoopRenderer::new);
                continue;
            }
            Identifier tex = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/" + id.getPath() + ".png");
            boolean have = Minecraft.getInstance().getResourceManager().getResource(tex).isPresent();
            if (!have) tex = Identifier.withDefaultNamespace("textures/entity/pig/pig_temperate.png");
            final Identifier t = tex;
            float w = type.getWidth(), h = type.getHeight();
            event.registerEntityRenderer((EntityType) type, ctx -> new BoxRenderer(ctx, BoxModel.create(w, h), t, Math.min(1.5f, w * 0.5f)));
        }
    }
}
