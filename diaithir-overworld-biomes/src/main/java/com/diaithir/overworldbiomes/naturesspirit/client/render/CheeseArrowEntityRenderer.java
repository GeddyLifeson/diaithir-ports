package com.diaithir.overworldbiomes.naturesspirit.client.render;

import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import com.diaithir.overworldbiomes.naturesspirit.entity.CheeseArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;

public class CheeseArrowEntityRenderer extends ArrowRenderer<CheeseArrowEntity, ArrowRenderState> {

  public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(NatureSpirit.MOD_ID, "textures/entity/projectiles/cheese_arrow.png");

  public CheeseArrowEntityRenderer(EntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  protected Identifier getTextureLocation(ArrowRenderState state) {
    return TEXTURE;
  }

  @Override
  public ArrowRenderState createRenderState() {
    return new ArrowRenderState();
  }
}
