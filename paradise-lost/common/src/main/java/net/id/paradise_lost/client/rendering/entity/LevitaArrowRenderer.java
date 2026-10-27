package net.id.paradise_lost.client.rendering.entity;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.client.rendering.entity.state.LevitaArrowRenderState;
import net.id.paradise_lost.entity.projectile.LevitaArrow;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class LevitaArrowRenderer extends ArrowRenderer<LevitaArrow, LevitaArrowRenderState> {
    private static final Identifier TEXTURE = ModConstants.locate("textures/entity/projectiles/levita_arrow.png");

    public LevitaArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public LevitaArrowRenderState createRenderState() {
        return new LevitaArrowRenderState();
    }

    @Override
    public void extractRenderState(LevitaArrow entity, LevitaArrowRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
    }

    @Override
    protected Identifier getTextureLocation(LevitaArrowRenderState state) {
        return TEXTURE;
    }
}
