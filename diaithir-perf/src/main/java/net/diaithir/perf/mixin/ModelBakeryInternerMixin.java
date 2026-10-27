package net.diaithir.perf.mixin;

import java.util.concurrent.ConcurrentHashMap;
import net.diaithir.perf.QuadInterner;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "net.minecraft.client.resources.model.ModelBakery$InternerImpl")
abstract class ModelBakeryInternerMixin implements QuadInterner {
    @Unique private volatile ConcurrentHashMap<BakedQuad, BakedQuad> diaithirperf$quads = new ConcurrentHashMap<>();

    @Override
    public BakedQuad diaithirperf$quad(BakedQuad quad) {
        ConcurrentHashMap<BakedQuad, BakedQuad> quads = diaithirperf$quads;
        if (quads == null) return quad;
        BakedQuad seen = quads.putIfAbsent(quad, quad);
        return seen != null ? seen : quad;
    }

    @Override
    public void diaithirperf$clear() {
        diaithirperf$quads = null;
    }
}
