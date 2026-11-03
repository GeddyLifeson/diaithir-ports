/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.model;

// TODO(oc2-restore): ElementsModel and IGeometryLoader (net.neoforged.neoforge.client.model.geometry) do not exist
//  in 26.1.2. The bus cable geometry loader is reduced to a no-op until the custom model loading is ported to the
//  new baked model / block state model API. The original implementation was:
//
//  public final class BusCableModelLoader implements IGeometryLoader<BusCableModel> {
//      @Override
//      public BusCableModel read(final JsonObject modelContents, final JsonDeserializationContext context) {
//          return new BusCableModel(ElementsModel.Loader.INSTANCE.read(modelContents, context));
//      }
//  }
public final class BusCableModelLoader {
}
