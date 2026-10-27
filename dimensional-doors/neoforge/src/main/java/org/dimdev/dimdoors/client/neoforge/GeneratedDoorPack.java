package org.dimdev.dimdoors.client.neoforge;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.flag.FeatureFlagSet;
import org.dimdev.dimdoors.DimensionalDoors;
import org.dimdev.dimdoors.block.door.DimensionalTrapDoorBlock;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Client resource pack that generates the blockstate and item definitions of the auto generated dimensional
 * doors at runtime. Each generated door reuses the models of the door it was made from, using the standard
 * door/trapdoor blockstate layout.
 */
public class GeneratedDoorPack extends AbstractPackResources {
    private static final String BLOCK_PREFIX = "block_ag_dim_";
    private static final String ITEM_PREFIX = "item_ag_dim_";
    private static final String[] FACINGS = {"east", "south", "west", "north"};

    private Map<String, String> files;

    public GeneratedDoorPack(PackLocationInfo location) {
        super(location);
    }

    public static Pack create() {
        PackLocationInfo info = new PackLocationInfo(DimensionalDoors.MOD_ID + ":generated_doors", Component.literal("Dimensional Doors generated doors"), PackSource.BUILT_IN, java.util.Optional.empty());
        Pack.ResourcesSupplier supplier = new Pack.ResourcesSupplier() {
            @Override
            public net.minecraft.server.packs.PackResources openPrimary(PackLocationInfo location) {
                return new GeneratedDoorPack(location);
            }

            @Override
            public net.minecraft.server.packs.PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return new GeneratedDoorPack(location);
            }
        };
        return new Pack(info, supplier,
                new Pack.Metadata(Component.literal("Generated dimensional door models"), PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of(), true),
                new PackSelectionConfig(true, Pack.Position.TOP, false));
    }

    private Map<String, String> files() {
        if (files == null) {
            Map<String, String> map = new HashMap<>();
            DimensionalDoors.getDimensionalDoorBlockRegistrar().getMappedDoorBlocks().forEach((generated, original) -> {
                boolean trapdoor = BuiltInRegistries.BLOCK.getValue(generated) instanceof DimensionalTrapDoorBlock;
                String genPath = generated.getPath();
                map.put("blockstates/" + genPath + ".json", trapdoor ? trapdoorState(original) : doorState(original));
                if (genPath.startsWith(BLOCK_PREFIX)) {
                    String itemPath = ITEM_PREFIX + genPath.substring(BLOCK_PREFIX.length());
                    String model = trapdoor
                            ? original.getNamespace() + ":block/" + original.getPath() + "_bottom"
                            : original.getNamespace() + ":item/" + original.getPath();
                    map.put("items/" + itemPath + ".json", "{\"model\":{\"type\":\"minecraft:model\",\"model\":\"" + model + "\"}}");
                }
            });
            files = map;
        }
        return files;
    }

    private static void variant(JsonObject variants, String key, String model, int y) {
        JsonObject v = new JsonObject();
        v.addProperty("model", model);
        y = ((y % 360) + 360) % 360;
        if (y != 0) v.addProperty("y", y);
        variants.add(key, v);
    }

    static String doorState(Identifier original) {
        String base = original.getNamespace() + ":block/" + original.getPath();
        JsonObject variants = new JsonObject();
        for (int f = 0; f < 4; f++) {
            for (String half : new String[]{"lower", "upper"}) {
                for (String hinge : new String[]{"left", "right"}) {
                    for (boolean open : new boolean[]{false, true}) {
                        String model = base + "_" + (half.equals("lower") ? "bottom" : "top") + "_" + hinge + (open ? "_open" : "");
                        int y = f * 90 + (open ? (hinge.equals("left") ? 90 : 270) : 0);
                        variant(variants, "facing=" + FACINGS[f] + ",half=" + half + ",hinge=" + hinge + ",open=" + open, model, y);
                    }
                }
            }
        }
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root.toString();
    }

    static String trapdoorState(Identifier original) {
        String base = original.getNamespace() + ":block/" + original.getPath();
        JsonObject variants = new JsonObject();
        // vanilla: north is the unrotated open orientation
        int[] openY = {90, 180, 270, 0};
        for (int f = 0; f < 4; f++) {
            for (String half : new String[]{"bottom", "top"}) {
                variant(variants, "facing=" + FACINGS[f] + ",half=" + half + ",open=false", base + "_" + half, 0);
                variant(variants, "facing=" + FACINGS[f] + ",half=" + half + ",open=true", base + "_open", openY[f]);
            }
        }
        JsonObject root = new JsonObject();
        root.add("variants", variants);
        return root.toString();
    }

    private static IoSupplier<InputStream> bytes(String s) {
        return () -> new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        if (type != PackType.CLIENT_RESOURCES || !location.getNamespace().equals(DimensionalDoors.MOD_ID)) return null;
        String content = files().get(location.getPath());
        return content == null ? null : bytes(content);
    }

    @Override
    public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !namespace.equals(DimensionalDoors.MOD_ID)) return;
        files().forEach((path, content) -> {
            if (path.startsWith(directory + "/")) {
                output.accept(DimensionalDoors.id(path), bytes(content));
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(DimensionalDoors.MOD_ID) : Set.of();
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> serializer) {
        return null;
    }

    @Override
    public void close() {
    }
}
