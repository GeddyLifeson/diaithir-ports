/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.model;

import li.cil.oc2.api.API;
import net.minecraft.resources.Identifier;

// TODO(oc2-restore): NeoForge's IUnbakedGeometry/IGeometryBakingContext, ItemOverrides and BakedModel no longer
//  exist in 26.1.2. The baking of straight/support cable models needs to be re-implemented on top of the new
//  block model system. Until then this is a placeholder holding the model locations and the proxied base geometry.
public final class BusCableModel {
    static final Identifier BUS_CABLE_STRAIGHT_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/cable_straight");
    static final Identifier BUS_CABLE_SUPPORT_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/cable_support");
    private final Object proxy;

    BusCableModel(final Object proxy) {
        this.proxy = proxy;
    }

    ///////////////////////////////////////////////////////////////////

    Object getProxy() {
        return proxy;
    }
}
