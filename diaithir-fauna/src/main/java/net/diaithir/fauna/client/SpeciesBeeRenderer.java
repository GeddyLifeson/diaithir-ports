package net.diaithir.fauna.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.diaithir.fauna.bees.BeeSpecies;
import net.diaithir.fauna.bees.Bees;
import net.diaithir.fauna.bees.Genes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.bee.Bee;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** The vanilla bee drawn in its species' colour. The tinted textures are made from the player's own vanilla textures when first needed; nothing derived from them is shipped. */
public final class SpeciesBeeRenderer extends BeeRenderer {
    public static final class State extends BeeRenderState {
        public String species = "";
    }

    private static final Map<String, Identifier> CACHE = new ConcurrentHashMap<>();

    public SpeciesBeeRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override
    public BeeRenderState createRenderState() { return new State(); }

    @Override
    public void extractRenderState(Bee bee, BeeRenderState state, float partial) {
        super.extractRenderState(bee, state, partial);
        ((State) state).species = Genes.species(bees(bee));
    }

    private static String bees(Bee bee) {
        String g = bee.getData(Bees.SPECIES.get());
        return g == null ? "" : g;
    }

    @Override
    public Identifier getTextureLocation(BeeRenderState state) {
        Identifier base = super.getTextureLocation(state);
        String sp = ((State) state).species;
        BeeSpecies s = BeeSpecies.byId(sp);
        if (s == null || s.id().equals(BeeSpecies.DEFAULT)) return base;
        return CACHE.computeIfAbsent(sp + "|" + base, k -> tinted(base, s));
    }

    private static Identifier tinted(Identifier base, BeeSpecies s) {
        Minecraft mc = Minecraft.getInstance();
        try (var in = mc.getResourceManager().getResource(base).orElseThrow().open()) {
            NativeImage img = NativeImage.read(in);
            int tr = (s.color() >> 16) & 255, tg = (s.color() >> 8) & 255, tb = s.color() & 255;
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int argb = img.getPixel(x, y);
                    int a = argb >>> 24, r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
                    if (a == 0 || r < 140 || g < 90 || b > 120 || r < g) continue;   // keep stripes, eyes, wings, stinger
                    float k = (0.3f * r + 0.59f * g + 0.11f * b) / 190f;   // body yellow is about 190 bright
                    int nr = Math.min(255, Math.round(tr * k)), ng = Math.min(255, Math.round(tg * k)), nb = Math.min(255, Math.round(tb * k));
                    img.setPixel(x, y, (a << 24) | (nr << 16) | (ng << 8) | nb);
                }
            }
            Identifier id = Identifier.fromNamespaceAndPath("diaithirfauna", "dynamic/bee_" + s.id() + "_" + Math.abs(base.getPath().hashCode()));
            mc.getTextureManager().register(id, new DynamicTexture(() -> id.toString(), img));
            return id;
        } catch (Exception e) {
            return base;
        }
    }
}
