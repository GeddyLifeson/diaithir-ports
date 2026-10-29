package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.block.OreBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.item.TagPrefixItem;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Client resource pack whose item definitions, blockstates and models are generated in code when the pack is opened
 * (on every resource reload). Replaces the mixin driven GTDynamicResourcePack of the 1.21.1 mod.
 */
public class GTDynamicResourcePack implements PackResources {

    private final PackLocationInfo info;
    private final Map<Identifier, byte[]> files = new TreeMap<>();

    public GTDynamicResourcePack(PackLocationInfo info) {
        this.info = info;
        try {
            generate();
        } catch (Throwable t) {
            GTCEu.LOGGER.error("Failed to generate GTCEu dynamic client assets", t);
        }
    }

    // ---------------------------------------------------------------- helpers

    private static boolean staticExists(String path) {
        return GTDynamicResourcePack.class.getResource("/assets/gtceu/" + path) != null;
    }

    @Nullable
    private static JsonObject readStatic(String path) {
        try (InputStream in = GTDynamicResourcePack.class.getResourceAsStream("/assets/gtceu/" + path)) {
            if (in == null) return null;
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            GTCEu.LOGGER.error("Could not read {}", path, e);
            return null;
        }
    }

    private void put(String path, JsonElement json) {
        files.put(GTCEu.id(path), json.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void putModel(String modelPath, JsonElement json) {
        put("models/" + modelPath + ".json", json);
    }

    private static JsonObject obj(Object... kv) {
        JsonObject o = new JsonObject();
        for (int i = 0; i < kv.length; i += 2) {
            Object v = kv[i + 1];
            String k = (String) kv[i];
            if (v instanceof JsonElement je) o.add(k, je);
            else if (v instanceof Number n) o.addProperty(k, n);
            else if (v instanceof Boolean b) o.addProperty(k, b);
            else o.addProperty(k, String.valueOf(v));
        }
        return o;
    }

    private static JsonArray tints(Material material) {
        JsonArray arr = new JsonArray();
        for (int i = 0; i < 2; i++) {
            arr.add(obj("type", "minecraft:constant", "value", material.getLayerARGB(i)));
        }
        return arr;
    }

    private static JsonObject modelDef(String model, @Nullable JsonArray tints) {
        JsonObject m = obj("type", "minecraft:model", "model", model);
        if (tints != null) m.add("tints", tints);
        return obj("model", m);
    }

    private static JsonObject compositeDef(List<String> models, @Nullable JsonArray tints) {
        JsonArray arr = new JsonArray();
        for (String model : models) {
            JsonObject m = obj("type", "minecraft:model", "model", model);
            if (tints != null) m.add("tints", tints.deepCopy());
            arr.add(m);
        }
        return obj("model", obj("type", "minecraft:composite", "models", arr));
    }

    private static String path(Identifier id) {
        return id.getPath();
    }

    /** Resolve a material icon model, walking up the icon set parents. Returns e.g. "gtceu:item/material_sets/dull/dust". */
    @Nullable
    private static String resolveIconModel(String kind, MaterialIconSet set, MaterialIconType type) {
        MaterialIconSet s = set;
        while (s != null) {
            String p = "models/%s/material_sets/%s/%s.json".formatted(kind, s.name, type.name());
            if (staticExists(p)) return "gtceu:%s/material_sets/%s/%s".formatted(kind, s.name, type.name());
            s = s.parentIconset;
        }
        return null;
    }

    /**
     * Turns a (possibly neoforge:composite) block model template into standalone models. Returns the ids of the
     * generated models in render order.
     */
    private List<String> flattenBlockModel(String modelId, String newBase) {
        String file = "models/" + modelId.substring("gtceu:".length()) + ".json";
        JsonObject root = readStatic(file);
        List<String> out = new ArrayList<>();
        if (root == null) return out;
        if (root.has("children")) {
            JsonObject children = root.getAsJsonObject("children");
            JsonArray order = root.has("item_render_order") ? root.getAsJsonArray("item_render_order") : null;
            List<String> names = new ArrayList<>();
            if (order != null) order.forEach(e -> names.add(e.getAsString()));
            for (String k : children.keySet()) if (!names.contains(k)) names.add(k);
            for (String name : names) {
                if (!children.has(name)) continue;
                JsonObject child = children.getAsJsonObject(name).deepCopy();
                child.remove("render_type");
                child.remove("loader");
                if (!child.has("parent") && !child.has("elements")) continue; // pure base_stone placeholder
                if (!child.has("parent")) child.addProperty("parent", "minecraft:block/block");
                if (root.has("textures")) {
                    JsonObject tex = child.has("textures") ? child.getAsJsonObject("textures") : new JsonObject();
                    for (var e : root.getAsJsonObject("textures").entrySet()) {
                        if (!tex.has(e.getKey())) tex.add(e.getKey(), e.getValue());
                    }
                    child.add("textures", tex);
                }
                String id = newBase + "_" + name;
                putModel(id.substring("gtceu:".length()), child);
                out.add(id);
            }
        } else {
            root.remove("loader");
            root.remove("render_type");
            String id = newBase;
            putModel(id.substring("gtceu:".length()), root);
            out.add(id);
        }
        return out;
    }

    private static JsonObject singleVariantState(String model) {
        return obj("variants", obj("", obj("model", model)));
    }

    private static JsonObject multipartState(List<String> models) {
        JsonArray parts = new JsonArray();
        for (String m : models) parts.add(obj("apply", obj("model", m)));
        return obj("multipart", parts);
    }

    // -------------------------------------------------------------- generation

    private int nItems, nBlocks, nMachineFix, nMissingItems;

    private void generate() {
        Set<String> madeItem = new HashSet<>();
        // ---------------------------------------------------- blocks
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (!id.getNamespace().equals(GTCEu.MOD_ID)) continue;
            Block block = entry.getValue();
            String p = path(id);
            if (block instanceof OreBlock ore) {
                generateOre(ore, id, madeItem);
            } else if (block instanceof com.gregtechceu.gtceu.api.block.PipeBlock<?, ?, ?> pb &&
                    !staticExists("blockstates/" + p + ".json")) {
                generatePipe(pb, id, madeItem);
            } else if (block instanceof MaterialBlock mb && !staticExists("blockstates/" + p + ".json")) {
                generateMaterialBlock(mb, id, madeItem);
            }
        }
        // ---------------------------------------------------- items
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            Identifier id = entry.getKey().identifier();
            if (!id.getNamespace().equals(GTCEu.MOD_ID)) continue;
            String p = path(id);
            if (madeItem.contains(p)) continue;
            Item item = entry.getValue();
            if (item instanceof TagPrefixItem tpi) {
                String model = resolveIconModel("item", tpi.material.getMaterialIconSet(),
                        tpi.tagPrefix.materialIconType());
                if (model != null) {
                    put("items/" + p + ".json", modelDef(model, tints(tpi.material)));
                    nItems++;
                    continue;
                }
            }
            if (item instanceof com.gregtechceu.gtceu.api.item.IGTTool tool) {
                var type = tool.getToolType();
                String model = type.modelLocation.toString();
                if (staticExists("models/" + type.modelLocation.getPath() + ".json")) {
                    Material mat = tool.getMaterial();
                    put("items/" + p + ".json", modelDef(model, tints(mat)));
                    nItems++;
                    continue;
                }
            }
            if (staticExists("models/item/" + p + ".json")) {
                put("items/" + p + ".json", modelDef("gtceu:item/" + p, null));
                nItems++;
            } else if (item instanceof BlockItem bi && staticExists("models/block/" + p + ".json")) {
                put("items/" + p + ".json", modelDef("gtceu:block/" + p, null));
                nItems++;
            } else {
                nMissingItems++;
            }
        }
        // ---------------------------------------------------- sanitize custom-loader models
        sanitizeLoaderModels();
        GTCEu.LOGGER.info("GTCEu dynamic assets: {} item definitions, {} blockstates, {} machine models fixed, {} items without a model, {} files",
                nItems, nBlocks, nMachineFix, nMissingItems, files.size());
    }

    private void generateMaterialBlock(MaterialBlock mb, Identifier id, Set<String> madeItem) {
        String p = path(id);
        String template = resolveIconModel("block", mb.material.getMaterialIconSet(), mb.tagPrefix.materialIconType());
        if (template == null) return;
        List<String> models = flattenBlockModel(template, "gtceu:block/dyn/" + p);
        if (models.isEmpty()) return;
        put("blockstates/" + p + ".json", models.size() == 1 ? singleVariantState(models.get(0)) : multipartState(models));
        nBlocks++;
        if (BuiltInRegistries.ITEM.containsKey(id)) {
            put("items/" + p + ".json", compositeDef(models, tints(mb.material)));
            madeItem.add(p);
            nItems++;
        }
    }

    private static JsonObject face(String tex, int tint) {
        JsonObject f = obj("texture", tex);
        if (tint >= 0) f.addProperty("tintindex", tint);
        return f;
    }

    private static JsonObject cuboid(float[] from, float[] to, String sideTex, String endTex, int tint) {
        JsonObject faces = new JsonObject();
        for (String d : new String[] { "north", "south", "east", "west" }) faces.add(d, face(sideTex, tint));
        faces.add("up", face(endTex, tint));
        faces.add("down", face(endTex, tint));
        JsonArray f = new JsonArray(), t = new JsonArray();
        for (float v : from) f.add(v);
        for (float v : to) t.add(v);
        return obj("from", f, "to", t, "faces", faces);
    }

    /**
     * Pipes and cables use a dynamic model keyed by block entity data that is not ported. Render a fixed plus shaped
     * pipe (core and six arms) so that they are visible and placeable.
     */
    private void generatePipe(com.gregtechceu.gtceu.api.block.PipeBlock<?, ?, ?> pb, Identifier id,
                              Set<String> madeItem) {
        String p = path(id);
        float th = pb.pipeType.getThickness() * 16f;
        float lo = (16f - th) / 2f, hi = lo + th;
        String name = pb.pipeType.toString().toLowerCase(Locale.ROOT);
        String side = "gtceu:block/pipe/pipe_side";
        String end = "gtceu:block/pipe/pipe_normal_in";
        if (pb instanceof com.gregtechceu.gtceu.common.block.CableBlock cb) {
            int lvl = cb.pipeType.insulationLevel;
            side = end = "gtceu:block/cable/insulation_" + Math.max(0, lvl);
        } else if (name.contains("tiny")) end = "gtceu:block/pipe/pipe_tiny_in";
        else if (name.contains("small")) end = "gtceu:block/pipe/pipe_small_in";
        else if (name.contains("nonuple")) end = "gtceu:block/pipe/pipe_nonuple_in";
        else if (name.contains("quadruple")) end = "gtceu:block/pipe/pipe_quadruple_in";
        else if (name.contains("huge")) end = "gtceu:block/pipe/pipe_huge_in";
        else if (name.contains("large")) end = "gtceu:block/pipe/pipe_large_in";
        else if (pb instanceof com.gregtechceu.gtceu.common.block.DuctPipeBlock) {
            side = "gtceu:block/pipe/pipe_duct_side";
            end = "gtceu:block/pipe/pipe_duct_in";
        } else if (pb instanceof com.gregtechceu.gtceu.common.block.LaserPipeBlock) {
            side = "gtceu:block/pipe/pipe_laser_side";
            end = "gtceu:block/pipe/pipe_laser_in";
        } else if (pb instanceof com.gregtechceu.gtceu.common.block.OpticalPipeBlock) {
            side = "gtceu:block/pipe/pipe_optical_side";
            end = "gtceu:block/pipe/pipe_optical_in";
        }
        JsonArray elements = new JsonArray();
        elements.add(cuboid(new float[] { lo, lo, lo }, new float[] { hi, hi, hi }, side, end, 0));
        elements.add(cuboid(new float[] { lo, lo, 0 }, new float[] { hi, hi, lo }, side, end, 0));
        elements.add(cuboid(new float[] { lo, lo, hi }, new float[] { hi, hi, 16 }, side, end, 0));
        elements.add(cuboid(new float[] { 0, lo, lo }, new float[] { lo, hi, hi }, side, end, 0));
        elements.add(cuboid(new float[] { hi, lo, lo }, new float[] { 16, hi, hi }, side, end, 0));
        elements.add(cuboid(new float[] { lo, 0, lo }, new float[] { hi, lo, hi }, side, end, 0));
        elements.add(cuboid(new float[] { lo, hi, lo }, new float[] { hi, 16, hi }, side, end, 0));
        JsonObject model = obj("parent", "minecraft:block/block", "textures", obj("particle", side), "elements", elements);
        String modelId = "gtceu:block/dyn/" + p;
        putModel("block/dyn/" + p, model);
        put("blockstates/" + p + ".json", singleVariantState(modelId));
        nBlocks++;
        if (BuiltInRegistries.ITEM.containsKey(id)) {
            Material mat = pb instanceof com.gregtechceu.gtceu.api.block.MaterialPipeBlock<?, ?, ?> mp ? mp.material : null;
            put("items/" + p + ".json", modelDef(modelId, mat == null ? null : tints(mat)));
            madeItem.add(p);
            nItems++;
        }
    }

    private void generateOre(OreBlock ore, Identifier id, Set<String> madeItem) {
        String p = path(id);
        TagPrefix tagPrefix = ore.tagPrefix;
        var oreType = TagPrefix.ORES.get(tagPrefix);
        MaterialIconType iconType = tagPrefix.getMaterialIconType(ore.material);
        String template = resolveIconModel("block", ore.material.getMaterialIconSet(), iconType);
        if (template == null || oreType == null) return;
        List<String> overlay = flattenBlockModel(template, "gtceu:block/dyn/" + p);
        List<String> models = new ArrayList<>();
        models.add(oreType.baseModelLocation().toString());
        models.addAll(overlay);
        put("blockstates/" + p + ".json", multipartState(models));
        nBlocks++;
        if (BuiltInRegistries.ITEM.containsKey(id)) {
            put("items/" + p + ".json", compositeDef(models, tints(ore.material)));
            madeItem.add(p);
            nItems++;
        }
    }

    private static List<String> listStaticModels() {
        List<String> out = new ArrayList<>();
        try {
            var file = net.neoforged.fml.ModList.get().getModFileById(GTCEu.MOD_ID).getFile();
            for (java.nio.file.Path contentRoot : file.getContents().getContentRoots()) {
                java.nio.file.Path root = contentRoot.resolve("assets/gtceu/models");
                if (!java.nio.file.Files.isDirectory(root)) continue;
                try (var walk = java.nio.file.Files.walk(root)) {
                    walk.filter(f -> f.toString().endsWith(".json")).forEach(
                            f -> out.add("models/" + root.relativize(f).toString().replace('\\', '/')));
                }
            }
            GTCEu.LOGGER.info("GTCEu: found {} static models", out.size());
        } catch (Exception e) {
            GTCEu.LOGGER.error("Could not list static models", e);
        }
        return out;
    }

    private static void listFrom(java.net.URL url, List<String> out) {
        try {
            if (url.getProtocol().equals("file")) {
                java.nio.file.Path root = java.nio.file.Path.of(url.toURI());
                try (var walk = java.nio.file.Files.walk(root)) {
                    walk.filter(f -> f.toString().endsWith(".json")).forEach(
                            f -> out.add("models/" + root.relativize(f).toString().replace('\\', '/')));
                }
            } else if (url.getProtocol().equals("jar")) {
                var conn = (java.net.JarURLConnection) url.openConnection();
                try (var jar = conn.getJarFile()) {
                    jar.stream().map(java.util.zip.ZipEntry::getName)
                            .filter(n -> n.startsWith("assets/gtceu/models/") && n.endsWith(".json"))
                            .forEach(n -> out.add(n.substring("assets/gtceu/".length())));
                }
            }
        } catch (Exception e) {
            GTCEu.LOGGER.error("Could not list static models from {}", url, e);
        }
    }

    /**
     * Machine, pipe, fluid container and composite models use custom model loaders that do not exist on 26.1. Replace
     * the ones that do not parse with a plain model.
     */
    private void sanitizeLoaderModels() {
        for (String modelPath : listStaticModels()) {
            if (files.containsKey(GTCEu.id(modelPath))) continue;
            String text = readStaticText(modelPath);
            if (text != null && NEG_TINT.matcher(text).find() && !text.contains("\"loader\"")) {
                files.put(GTCEu.id(modelPath), NEG_TINT.matcher(text).replaceAll("\"tintindex\": -1")
                        .getBytes(StandardCharsets.UTF_8));
                continue;
            }
            JsonObject root = readStatic(modelPath);
            if (root == null || !root.has("loader")) continue;
            String loader = root.get("loader").getAsString();
            JsonObject replacement = null;
            switch (loader) {
                case "gtceu:machine" -> {
                    JsonElement first = null;
                    if (root.has("variants")) {
                        JsonObject variants = root.getAsJsonObject("variants");
                        for (String key : variants.keySet()) {
                            first = variants.get(key);
                            break;
                        }
                    } else if (root.has("multipart")) {
                        for (JsonElement part : root.getAsJsonArray("multipart")) {
                            if (part.isJsonObject() && part.getAsJsonObject().has("apply")) {
                                first = part.getAsJsonObject().get("apply");
                                break;
                            }
                        }
                    }
                    if (first != null && first.isJsonArray() && !first.getAsJsonArray().isEmpty()) {
                        first = first.getAsJsonArray().get(0);
                    }
                    if (first != null && first.isJsonObject() && first.getAsJsonObject().has("model")) {
                        JsonElement m = first.getAsJsonObject().get("model");
                        if (m.isJsonObject()) replacement = m.getAsJsonObject().deepCopy();
                        else if (m.isJsonPrimitive()) replacement = obj("parent", m.getAsString());
                    }
                    if (replacement != null) nMachineFix++;
                }
                case "gtceu:pipe", "gtceu:facade", "neoforge:fluid_container" -> {
                    replacement = root.deepCopy();
                    replacement.remove("loader");
                    replacement.remove("parts");
                }
                case "neoforge:composite" -> {
                    JsonObject children = root.has("children") ? root.getAsJsonObject("children") : null;
                    replacement = new JsonObject();
                    replacement.addProperty("parent", "minecraft:block/block");
                    if (root.has("textures")) replacement.add("textures", root.get("textures").deepCopy());
                    if (children != null) {
                        for (var e : children.entrySet()) {
                            if (e.getValue().isJsonObject() && e.getValue().getAsJsonObject().has("elements")) {
                                var c = e.getValue().getAsJsonObject();
                                replacement.add("elements", c.get("elements").deepCopy());
                                if (c.has("textures")) {
                                    JsonObject tex = replacement.has("textures") ?
                                            replacement.getAsJsonObject("textures") : new JsonObject();
                                    c.getAsJsonObject("textures").entrySet().forEach(t -> tex.add(t.getKey(), t.getValue()));
                                    replacement.add("textures", tex);
                                }
                                break;
                            }
                        }
                    }
                }
                default -> {}
            }
            if (replacement == null) continue;
            if (!replacement.has("parent") && !replacement.has("elements")) replacement.addProperty("parent", "minecraft:block/cube");
            files.put(GTCEu.id(modelPath), NEG_TINT.matcher(replacement.toString()).replaceAll("\"tintindex\":-1")
                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    private static final java.util.regex.Pattern NEG_TINT = java.util.regex.Pattern
            .compile("\"tintindex\"\\s*:\\s*-(?!1\\b)\\d+");

    @Nullable
    private static String readStaticText(String path) {
        try (InputStream in = GTDynamicResourcePack.class.getResourceAsStream("/assets/gtceu/" + path)) {
            return in == null ? null : new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    // --------------------------------------------------------------- PackResources

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        if (type != PackType.CLIENT_RESOURCES) return null;
        byte[] data = files.get(location);
        return data == null ? null : () -> new ByteArrayInputStream(data);
    }

    @Override
    public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !namespace.equals(GTCEu.MOD_ID)) return;
        String prefix = directory.isEmpty() ? "" : directory + "/";
        for (var e : files.entrySet()) {
            if (e.getKey().getPath().startsWith(prefix)) {
                byte[] data = e.getValue();
                output.accept(e.getKey(), () -> new ByteArrayInputStream(data));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(GTCEu.MOD_ID) : Set.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> T getMetadataSection(MetadataSectionType<T> type) {
        if (type.equals(PackMetadataSection.CLIENT_TYPE) || type.equals(PackMetadataSection.FALLBACK_TYPE)) {
            var fmt = SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES);
            return (T) new PackMetadataSection(Component.literal("GTCEu dynamic assets"), new InclusiveRange<>(fmt));
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return info;
    }

    @Override
    public void close() {}
}
