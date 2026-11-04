package com.diaithir.overworldbiomes.naturesspirit.client.render;

import com.diaithir.overworldbiomes.naturesspirit.NatureSpirit;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public class NSEntityModelLayers {

  public static void registerEntityModelLayers() {
  }

  public static final ModelLayerLocation PIZZA_TOPPING = register("pizza", "toppings");

  private static ModelLayerLocation registerMain(String id) {
    return register(id, "main");
  }

  private static ModelLayerLocation register(String id, String layer) {
    return create(id, layer);
  }

  private static ModelLayerLocation create(String id, String layer) {
    return new ModelLayerLocation(Identifier.fromNamespaceAndPath(NatureSpirit.MOD_ID, id), layer);
  }
}
