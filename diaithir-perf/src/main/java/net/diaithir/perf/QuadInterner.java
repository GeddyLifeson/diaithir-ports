package net.diaithir.perf;

import net.minecraft.client.resources.model.geometry.BakedQuad;

/** Implemented by the model baker's interner, so identical baked quads share one instance for the bake. */
public interface QuadInterner {
    BakedQuad diaithirperf$quad(BakedQuad quad);

    /** Drops the table once baking is done; the interner itself stays reachable from baked models. */
    void diaithirperf$clear();
}
