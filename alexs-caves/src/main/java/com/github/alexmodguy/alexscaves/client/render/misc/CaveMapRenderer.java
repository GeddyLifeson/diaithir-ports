package com.github.alexmodguy.alexscaves.client.render.misc;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.item.CaveMapItem;
import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACTagRegistry;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.Vec3;

import java.util.*;

/** 26.1 port of the cave map renderer: a 128x128 biome texture with labels and a player marker, drawn as a flat quad. */
public class CaveMapRenderer {
    private static final Map<String, CaveMapRenderer> CACHE = new HashMap<>();
    private static final Map<String, int[]> BACKGROUNDS = new HashMap<>();
    private static final Identifier PLAYER_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/misc/map/cave_map_player.png");

    public final BlockPos target;
    private final DynamicTexture texture;
    private final RenderType renderType;
    private final List<BiomeLabel> labels = new ArrayList<>();
    private final int[] mapBiomes;
    private final Random random;

    private record BiomeLabel(ResourceKey<Biome> biome, int x, int y, int rotation) {
        boolean conflictsWith(BiomeLabel o) {
            int xd = x - o.x, yd = y - o.y;
            return Math.sqrt(xd * xd + yd * yd) <= 40 || biome.equals(o.biome);
        }
    }

    private CaveMapRenderer(BlockPos target, int[] mapBiomes, long seed, String key) {
        this.target = target;
        this.mapBiomes = mapBiomes;
        this.random = new Random(seed ^ 42L);
        this.texture = new DynamicTexture("cave_map_" + key, 128, 128, true);
        Identifier id = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "cave_map/" + Integer.toHexString(key.hashCode()));
        Minecraft.getInstance().getTextureManager().register(id, this.texture);
        this.renderType = RenderTypes.entityTranslucent(id);
        updateTexture();
        updateLabels();
    }

    public static CaveMapRenderer getMapFor(ItemStack item) {
        BlockPos target = CaveMapItem.getBiomeBlockPos(item);
        long seed = CaveMapItem.getSeed(item);
        String key = target.getX() + "_" + target.getY() + "_" + target.getZ() + "_" + seed;
        CaveMapRenderer r = CACHE.get(key);
        if (r == null) {
            r = new CaveMapRenderer(target, CaveMapItem.createBiomeArray(item), seed, key);
            CACHE.put(key, r);
        }
        return r;
    }

    private static int[] background(String name) {
        return BACKGROUNDS.computeIfAbsent(name, n -> {
            Identifier id = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/misc/map/" + n + "_background.png");
            try (var in = Minecraft.getInstance().getResourceManager().getResourceOrThrow(id).open(); NativeImage img = NativeImage.read(in)) {
                int w = img.getWidth(), h = img.getHeight();
                int[] px = img.getPixelsABGR();
                int[] out = new int[128 * 128];
                for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) out[y * 128 + x] = px[(y % h) * w + (x % w)];
                return out;
            } catch (Exception e) {
                int[] out = new int[128 * 128];
                Arrays.fill(out, 0xFF8B7355);
                return out;
            }
        });
    }

    private void updateTexture() {
        Registry<Biome> registry = Minecraft.getInstance().level.registryAccess().lookup(Registries.BIOME).orElse(null);
        if (registry != null && mapBiomes.length >= 128 * 128) {
            NativeImage pixels = this.texture.getPixels();
            for (int i = 0; i < 128; ++i) {
                for (int j = 0; j < 128; ++j) {
                    Holder<Biome> biome = registry.asHolderIdMap().byId(mapBiomes[j + i * 128]);
                    int color;
                    if (biome == null) color = background("default")[i * 128 + j];
                    else if (biome.is(ACTagRegistry.CAVE_MAP_BORDER_ON) && isBorderBiome(registry, biome, i, j)) color = background("border")[i * 128 + j];
                    else color = background(backgroundName(biome))[i * 128 + j];
                    double edge = Math.sqrt((i - 64) * (i - 64) + (j - 64) * (j - 64)) / 128;
                    int alpha = Math.max(255 - (int) (255F * (edge * edge)), 10);
                    pixels.setPixelABGR(j, i, (alpha << 24) | (color & 0xFFFFFF));
                }
            }
        }
        this.texture.upload();
    }

    private boolean isBorderBiome(Registry<Biome> registry, Holder<Biome> biome, int i, int j) {
        int k = j + i * 128;
        int[] around = {k - 1, k + 1, j + (i - 1) * 128, j + (i + 1) * 128};
        for (int n : around) {
            if (n >= 0 && n < mapBiomes.length && n % 128 != 127 && n % 128 != 0 && registry.asHolderIdMap().byId(mapBiomes[n]) != biome) return true;
        }
        return false;
    }

    private void updateLabels() {
        labels.clear();
        int extraBiomes = random.nextInt(3) + 3;
        Registry<Biome> registry = Minecraft.getInstance().level.registryAccess().lookup(Registries.BIOME).orElse(null);
        if (registry == null || mapBiomes.length < 128 * 128) return;
        BiomeLabel center = label(64, 64, registry, 0);
        labels.add(center);
        for (int i = 0; i < extraBiomes; i++) {
            Vec3 off = new Vec3(0, 0, random.nextInt(20) + 40).yRot((float) Math.toRadians((360 / (float) extraBiomes) * i + random.nextFloat() * 40));
            int ox = Mth.clamp(center.x() + (int) off.x, 10, 118);
            int oy = Mth.clamp(center.y() + (int) off.z, 10, 118);
            BiomeLabel built = label(ox, oy, registry, (ox - 64) / 2);
            if (labels.stream().noneMatch(l -> l.conflictsWith(built))) labels.add(built);
        }
    }

    private BiomeLabel label(int x, int y, Registry<Biome> registry, int rotation) {
        Holder<Biome> holder = registry.asHolderIdMap().byId(mapBiomes[x + y * 128]);
        ResourceKey<Biome> key = holder == null ? Biomes.PLAINS : holder.unwrapKey().orElse(Biomes.PLAINS);
        return new BiomeLabel(key, x, y, rotation);
    }

    private static String backgroundName(Holder<Biome> biome) {
        if (biome.is(ACBiomeRegistry.MAGNETIC_CAVES)) return "magnetic_caves";
        if (biome.is(ACBiomeRegistry.PRIMORDIAL_CAVES)) return "primordial_caves";
        if (biome.is(ACBiomeRegistry.TOXIC_CAVES)) return "toxic_caves";
        if (biome.is(ACBiomeRegistry.ABYSSAL_CHASM)) return "abyssal_chasm";
        if (biome.is(ACBiomeRegistry.FORLORN_HOLLOWS)) return "forlorn_hollows";
        if (biome.is(ACBiomeRegistry.CANDY_CAVITY)) return "candy_cavity";
        boolean cold = biome.value().coldEnoughToSnow(BlockPos.ZERO, 63);
        if (biome.is(BiomeTags.IS_OCEAN) && cold) return "frozen_ocean";
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) return "water";
        if (biome.is(Biomes.DESERT)) return "desert";
        if (biome.is(BiomeTags.IS_JUNGLE)) return "jungle";
        if (biome.is(BiomeTags.IS_BADLANDS)) return "badlands";
        if (biome.is(BiomeTags.IS_MOUNTAIN) && cold) return "snowy_mountain";
        if (biome.is(BiomeTags.IS_TAIGA) && cold) return "snowy_taiga";
        if (biome.is(BiomeTags.IS_MOUNTAIN)) return "mountain";
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) return "swamp";
        if (biome.is(Biomes.ICE_SPIKES)) return "ice_spikes";
        if (cold && !biome.is(BiomeTags.IS_TAIGA)) return "snowy";
        if (biome.is(BiomeTags.IS_TAIGA)) return "taiga";
        if (biome.is(Biomes.MUSHROOM_FIELDS)) return "mushroom";
        if (biome.is(Biomes.DARK_FOREST)) return "roofed_forest";
        if (biome.is(BiomeTags.IS_FOREST)) return "forest";
        if (biome.is(BiomeTags.IS_SAVANNA)) return "savanna";
        if (biome.is(Biomes.PLAINS) || biome.is(Biomes.SUNFLOWER_PLAINS) || biome.is(Biomes.MEADOW)) return "plains";
        if (biome.is(Biomes.STONY_SHORE)) return "stony_shore";
        if (biome.is(BiomeTags.IS_BEACH)) return "beach";
        if (biome.is(Biomes.DRIPSTONE_CAVES)) return "dripstone_caves";
        if (biome.is(Biomes.LUSH_CAVES)) return "lush_caves";
        if (biome.is(Biomes.DEEP_DARK)) return "deep_dark";
        return "default";
    }

    /** Draws the map on a unit square in the XY plane at z = 0.5 (item space), double sided, with labels and the player marker. */
    public void submit(PoseStack poseStack, OrderedSubmitNodeCollector collector, int light) {
        final float z = 0.5F;
        collector.submitCustomGeometry(poseStack, renderType, (pose, vc) -> {
            float[][] c = {{0, 0, 0, 1}, {1, 0, 1, 1}, {1, 1, 1, 0}, {0, 1, 0, 0}};
            for (int pass = 0; pass < 2; pass++) {
                for (int k = 0; k < 4; k++) {
                    float[] p = c[pass == 0 ? k : 3 - k];
                    vc.addVertex(pose, p[0], p[1], z).setColor(255, 255, 255, 255).setUv(p[2], p[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, 1F);
                }
            }
        });
        var mc = Minecraft.getInstance();
        Font font = mc.font;
        for (BiomeLabel label : labels) {
            poseStack.pushPose();
            Component component = Component.translatable("biome." + label.biome.identifier().toString().replace(":", "."));
            int textWidth = font.width(component.getVisualOrderText());
            float dist = (float) Math.sqrt((label.x() - 64) * (label.x() - 64) + (label.y() - 64) * (label.y() - 64));
            float textScale = Mth.clamp((128 - dist) / 128, 0.5F, 1F) * Math.min(100F / (float) textWidth, 1F);
            poseStack.translate(0F, 1F, z - 0.002F);
            poseStack.scale(1F / 128F, -1F / 128F, -1F / 128F);
            poseStack.translate(label.x() - (textWidth * textScale) / 2.0F, label.y(), 0F);
            poseStack.scale(textScale, textScale, 1F);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(label.rotation()));
            collector.submitText(poseStack, 0F, 0F, component.getVisualOrderText(), false, Font.DisplayMode.NORMAL, light, 0xFF87695B, 0, 0xFFEBDCC2);
            poseStack.popPose();
        }
        if (mc.player != null) {
            double scale = mc.player.level().dimensionType().coordinateScale();
            double px = Mth.clamp((mc.player.getX() * scale - target.getX()) / CaveMapItem.MAP_SCALE + 64.0, 0, 128);
            double pz = Mth.clamp((mc.player.getZ() * scale - target.getZ()) / CaveMapItem.MAP_SCALE + 64.0, 0, 128);
            poseStack.pushPose();
            poseStack.translate(0F, 1F, z - 0.004F);
            poseStack.scale(1F / 128F, -1F / 128F, -1F / 128F);
            poseStack.translate(px, pz, 0);
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(mc.player.getYRot() + 180));
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(PLAYER_TEXTURE), (pose, vc) -> {
                float s = 4F;
                float[][] c = {{-s, s, 0, 1}, {s, s, 1, 1}, {s, -s, 1, 0}, {-s, -s, 0, 0}};
                for (int pass = 0; pass < 2; pass++) {
                    for (int k = 0; k < 4; k++) {
                        float[] p = c[pass == 0 ? k : 3 - k];
                        vc.addVertex(pose, p[0], p[1], 0F).setColor(255, 255, 255, 255).setUv(p[2], p[3]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, 1F);
                    }
                }
            });
            poseStack.popPose();
        }
    }
}
