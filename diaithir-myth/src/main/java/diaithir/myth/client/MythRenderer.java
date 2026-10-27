package diaithir.myth.client;

import diaithir.myth.Creature;
import diaithir.myth.DiaithirMyth;
import diaithir.myth.MythBeast;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class MythRenderer extends MobRenderer<MythBeast, MythRenderState, MythModel> {
    private final Identifier texture;

    public static ModelLayerLocation layer(String id) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(DiaithirMyth.MODID, id), "main");
    }

    public MythRenderer(EntityRendererProvider.Context ctx, Creature c, ModelData data) {
        super(ctx, new MythModel(ctx.bakeLayer(layer(c.id)), data), (float) Math.max(0.3D, c.width * 0.5D));
        this.texture = Identifier.fromNamespaceAndPath(DiaithirMyth.MODID, "textures/entity/" + c.id + ".png");
    }

    @Override
    public MythRenderState createRenderState() {
        return new MythRenderState();
    }

    @Override
    public void extractRenderState(MythBeast entity, MythRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flying = entity.def.flyer && !entity.onGround();
        state.enraged = entity.isEnraged();
        state.swing = entity.getAttackAnim(partialTicks);
    }

    @Override
    public Identifier getTextureLocation(MythRenderState state) {
        return texture;
    }
}
