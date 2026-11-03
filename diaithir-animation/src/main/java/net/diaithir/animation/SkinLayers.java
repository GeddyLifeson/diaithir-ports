package net.diaithir.animation;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.PlayerModelType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

/**
 * 3D skin layers (clean-room replacement for 3D Skin Layers): hat, jacket, sleeves and pants are drawn as extruded pixel voxels instead of flat
 * overlays. The voxel geometry (SkinVoxels) is built once per skin texture from its pixels, wrapped in a PlayerModel with the same bones as vanilla, so
 * every animation (vanilla, PAL, our body layer) moves it for free. Vanilla's flat overlay is switched off for that frame through the render state's show*
 * flags; beyond LOD_BLOCKS, or when the skin cannot be read, vanilla keeps drawing. First-person hands are untouched.
 */
public final class SkinLayers {
    static final double LOD_BLOCKS = 20;
    private static final ContextKey<Data> KEY = new ContextKey<>(Identifier.fromNamespaceAndPath(DiaithirAnimation.ID, "skin_layers"));
    private static final int CACHE_MAX = 48;
    private static EntityModelSet models;
    private static final Map<String, Optional<Mesh>> CACHE = new LinkedHashMap<>(16, 0.75F, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Optional<Mesh>> e) { return size() > CACHE_MAX; }
    };
    /** The player renderers that got our layer (identity); mannequins keep vanilla overlays. */
    private static final java.util.Set<Object> PLAYERS = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    private static final Map<String, Long> MISS = new java.util.HashMap<>();

    /** Original overlay flags, restored on the layer's own model. */
    record Flags(boolean hat, boolean jacket, boolean leftPants, boolean rightPants, boolean leftSleeve, boolean rightSleeve) { }
    record Mesh(SkinModel model) { }
    record Data(Mesh mesh, Identifier texture, Flags flags) { }

    static void init(IEventBus bus) {
        bus.addListener(EntityRenderersEvent.AddLayers.class, e -> {
            models = e.getEntityModels();
            verify();
            for (PlayerModelType t : PlayerModelType.values()) {
                AvatarRenderer<?> r = e.getPlayerRenderer(t);
                if (r != null) { addLayer(r); PLAYERS.add(r); }
            }
        });
        NeoForge.EVENT_BUS.addListener(RenderLivingEvent.Pre.class, e -> { if (PLAYERS.contains(e.getRenderer())) pre(e.getRenderState()); });
    }

    /** Client check against vanilla's own ModelPart.Cube: at thickness 0 the voxels must reproduce every vanilla vertex (position and uv) and the same winding. */
    static void verify() {
        var cube = new ModelPart.Cube(32, 0, -4, -8, -4, 8, 8, 8, 0, 0, 0, false, 64, 64, EnumSet.allOf(Direction.class));
        var flat = SkinVoxels.build(new SkinVoxels.Box(-4, -8, -4, 8, 8, 8, 32, 0, 0F), (x, y) -> 255);
        int missing = 0, wrongWind = 0;
        for (var poly : cube.polygons) {
            for (var v : poly.vertices()) {
                boolean hit = false;
                for (var q : flat) for (int k = 0; k < 4 && !hit; k++)
                    hit = Math.abs(q.pos()[k * 3] - v.x()) < 1e-4 && Math.abs(q.pos()[k * 3 + 1] - v.y()) < 1e-4 && Math.abs(q.pos()[k * 3 + 2] - v.z()) < 1e-4
                            && Math.abs(q.uv()[k * 2] - v.u()) < 1e-4 && Math.abs(q.uv()[k * 2 + 1] - v.v()) < 1e-4;
                if (!hit) missing++;
            }
            var p = poly.vertices();
            float[] a = {p[0].x(), p[0].y(), p[0].z()}, b = {p[1].x(), p[1].y(), p[1].z()}, c = {p[2].x(), p[2].y(), p[2].z()};
            var n = poly.normal();
            if (Math.signum(SkinVoxels.dot(SkinVoxels.cross(SkinVoxels.sub(b, a), SkinVoxels.sub(c, b)), new float[]{n.x(), n.y(), n.z()})) != SkinVoxels.FRONT) wrongWind++;
        }
        com.mojang.logging.LogUtils.getLogger().info("[skinlayers] verify vs vanilla cube: {} vertices missing, {} faces wound differently", missing, wrongWind);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addLayer(AvatarRenderer<?> r) { ((AvatarRenderer) r).addLayer(new Layer((RenderLayerParent) r)); }

    /** Before the avatar is drawn: swap vanilla's flat overlay for our voxels when we can. */
    private static void pre(Object o) {
        if (!(o instanceof AvatarRenderState s) || models == null) return;
        s.setRenderData(KEY, null);
        if (s.isInvisible || s.distanceToCameraSq > LOD_BLOCKS * LOD_BLOCKS) return;
        Identifier tex = s.skin.body().texturePath();
        Mesh m = mesh(tex, s.skin.model() == PlayerModelType.SLIM);
        if (m == null) return;
        s.setRenderData(KEY, new Data(m, tex, new Flags(s.showHat, s.showJacket, s.showLeftPants, s.showRightPants, s.showLeftSleeve, s.showRightSleeve)));
        s.showHat = s.showJacket = s.showLeftPants = s.showRightPants = s.showLeftSleeve = s.showRightSleeve = false;
    }

    private static Mesh mesh(Identifier tex, boolean slim) {
        String key = tex + (slim ? "#slim" : "#wide");
        Optional<Mesh> c = CACHE.get(key);
        if (c != null) return c.orElse(null);
        Long miss = MISS.get(key);
        long now = System.nanoTime();
        if (miss != null && now - miss < 5_000_000_000L) return null;
        Mesh built = null;
        try { built = build(tex, slim); } catch (RuntimeException ex) { com.mojang.logging.LogUtils.getLogger().warn("[skinlayers] {}: {}", tex, ex.toString()); }
        if (built == null) { MISS.put(key, now); return null; }
        MISS.remove(key);
        com.mojang.logging.LogUtils.getLogger().info("[skinlayers] built voxel model for {}", key);
        CACHE.put(key, Optional.of(built));
        return built;
    }

    /** Reads the skin's pixels (downloaded skins are DynamicTextures, default skins are resources) and builds the voxel model. */
    private static Mesh build(Identifier tex, boolean slim) {
        Minecraft mc = Minecraft.getInstance();
        AbstractTexture t = mc.getTextureManager().getTexture(tex);
        NativeImage img = null;
        boolean own = false;
        if (t instanceof DynamicTexture dt && !dt.getPixels().isClosed()) img = dt.getPixels();
        else {
            var res = mc.getResourceManager().getResource(tex);
            if (res.isEmpty()) return null;
            try (var in = res.get().open()) { img = NativeImage.read(in); own = true; } catch (java.io.IOException e) { return null; }
        }
        try {
            if (img.getWidth() != SkinVoxels.TEX || img.getHeight() != SkinVoxels.TEX) return null;
            final NativeImage im = img;
            java.util.function.IntBinaryOperator alpha = (x, y) -> im.getPixel(x, y) >>> 24;
            return new Mesh(new SkinModel(root(slim, alpha), slim));
        } finally {
            if (own) img.close();
        }
    }

    private static ModelPart root(boolean slim, java.util.function.IntBinaryOperator alpha) {
        ModelPart v = models.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER);
        Map<String, List<ModelPart.Cube>> cubes = new java.util.HashMap<>();
        SkinVoxels.boxes(slim).forEach((name, box) -> cubes.put(name, cubes(SkinVoxels.build(box, alpha))));
        return same(v, List.of(), Map.of(
                "head", same(v.getChild("head"), List.of(), Map.of("hat", same(v.getChild("head").getChild("hat"), cubes.get("hat"), Map.of()))),
                "body", same(v.getChild("body"), List.of(), Map.of("jacket", same(v.getChild("body").getChild("jacket"), cubes.get("jacket"), Map.of()))),
                "left_arm", same(v.getChild("left_arm"), List.of(), Map.of("left_sleeve", same(v.getChild("left_arm").getChild("left_sleeve"), cubes.get("left_sleeve"), Map.of()))),
                "right_arm", same(v.getChild("right_arm"), List.of(), Map.of("right_sleeve", same(v.getChild("right_arm").getChild("right_sleeve"), cubes.get("right_sleeve"), Map.of()))),
                "left_leg", same(v.getChild("left_leg"), List.of(), Map.of("left_pants", same(v.getChild("left_leg").getChild("left_pants"), cubes.get("left_pants"), Map.of()))),
                "right_leg", same(v.getChild("right_leg"), List.of(), Map.of("right_pants", same(v.getChild("right_leg").getChild("right_pants"), cubes.get("right_pants"), Map.of())))));
    }

        private static ModelPart same(ModelPart like, List<ModelPart.Cube> cubes, Map<String, ModelPart> children) {
        ModelPart p = new ModelPart(cubes, children);
        p.setInitialPose(like.getInitialPose());
        p.loadPose(like.getInitialPose());
        return p;
    }

    /** Packs quads into Cubes (a Cube holds at most six polygons; ours are overwritten with the voxel quads). */
    static List<ModelPart.Cube> cubes(List<SkinVoxels.Quad> quads) {
        List<ModelPart.Cube> out = new ArrayList<>();
        Direction[] dirs = {Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH};
        for (int i = 0; i < quads.size(); i += 6) {
            int n = Math.min(6, quads.size() - i);
            EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);
            for (int k = 0; k < n; k++) faces.add(dirs[k]);
            ModelPart.Cube c = new ModelPart.Cube(0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, false, SkinVoxels.TEX, SkinVoxels.TEX, faces);
            for (int k = 0; k < n; k++) c.polygons[k] = polygon(quads.get(i + k));
            out.add(c);
        }
        return out;
    }

    private static ModelPart.Polygon polygon(SkinVoxels.Quad q) {
        ModelPart.Vertex[] v = new ModelPart.Vertex[4];
        for (int k = 0; k < 4; k++) v[k] = new ModelPart.Vertex(q.pos()[k * 3], q.pos()[k * 3 + 1], q.pos()[k * 3 + 2], q.uv()[k * 2], q.uv()[k * 2 + 1]);
        return new ModelPart.Polygon(v, new Vector3f(q.nx(), q.ny(), q.nz()));
    }

    /** A vanilla-shaped PlayerModel whose overlay bones carry the voxels; shows the overlays the player had enabled. */
    static final class SkinModel extends PlayerModel {
        SkinModel(ModelPart root, boolean slim) { super(root, slim); }

        @Override public void setupAnim(AvatarRenderState s) {
            super.setupAnim(s);
            Data d = s.getRenderData(KEY);
            if (d == null) return;
            Flags f = d.flags();
            this.hat.visible = f.hat();
            this.jacket.visible = f.jacket();
            this.leftPants.visible = f.leftPants();
            this.rightPants.visible = f.rightPants();
            this.leftSleeve.visible = f.leftSleeve();
            this.rightSleeve.visible = f.rightSleeve();
        }
    }

    static final class Layer extends RenderLayer<AvatarRenderState, PlayerModel> {
        Layer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }

        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
            Data d = state.getRenderData(KEY);
            if (d == null || state.isInvisible) return;
            renderColoredCutoutModel(d.mesh().model(), d.texture(), poseStack, collector, lightCoords, state, -1, 1);
        }
    }

    private SkinLayers() { }
}
