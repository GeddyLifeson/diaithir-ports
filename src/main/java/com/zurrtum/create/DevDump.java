package com.zurrtum.create;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.nio.file.Files;
import java.nio.file.Path;

/** Developer aid: -Dcreate.dumpRegistries=path writes a JSON summary of everything registered under the create namespace. */
public class DevDump {
    @SubscribeEvent
    public void onStarted(ServerStartedEvent event) {
        if (System.getProperty("create.loadMixinTargets") != null) {
            loadMixinTargets();
        }
        String target = System.getProperty("create.dumpRegistries");
        if (target == null) {
            return;
        }
        JsonObject root = new JsonObject();
        JsonArray blocks = new JsonArray();
        for (Block block : BuiltInRegistries.BLOCK) {
            var key = BuiltInRegistries.BLOCK.getKey(block);
            if (!key.getNamespace().equals(Create.MOD_ID)) {
                continue;
            }
            JsonObject o = new JsonObject();
            o.addProperty("id", key.getPath());
            o.addProperty("class", block.getClass().getName());
            o.addProperty("hasBE", block.defaultBlockState().hasBlockEntity());
            o.addProperty("item", block.asItem() != net.minecraft.world.item.Items.AIR);
            JsonObject props = new JsonObject();
            for (Property<?> p : block.getStateDefinition().getProperties()) {
                JsonArray vals = new JsonArray();
                for (Object v : p.getPossibleValues()) {
                    vals.add(((Comparable<?>) v) instanceof net.minecraft.util.StringRepresentable sr ? sr.getSerializedName() : String.valueOf(v));
                }
                props.add(p.getName(), vals);
            }
            o.add("props", props);
            o.addProperty("shape", block.defaultBlockState().getRenderShape().name());
            blocks.add(o);
        }
        root.add("blocks", blocks);
        JsonArray items = new JsonArray();
        for (Item item : BuiltInRegistries.ITEM) {
            var key = BuiltInRegistries.ITEM.getKey(item);
            if (!key.getNamespace().equals(Create.MOD_ID)) {
                continue;
            }
            JsonObject o = new JsonObject();
            o.addProperty("id", key.getPath());
            o.addProperty("class", item.getClass().getName());
            o.addProperty("blockItem", item instanceof BlockItem);
            items.add(o);
        }
        root.add("items", items);
        root.add("fluids", names(BuiltInRegistries.FLUID));
        root.add("entities", names(BuiltInRegistries.ENTITY_TYPE));
        root.add("block_entities", names(BuiltInRegistries.BLOCK_ENTITY_TYPE));
        root.add("sounds", names(BuiltInRegistries.SOUND_EVENT));
        root.add("particles", names(BuiltInRegistries.PARTICLE_TYPE));
        root.add("menus", names(BuiltInRegistries.MENU));
        root.add("mob_effects", names(BuiltInRegistries.MOB_EFFECT));
        root.add("creative_tabs", names(BuiltInRegistries.CREATIVE_MODE_TAB));
        root.add("data_components", names(BuiltInRegistries.DATA_COMPONENT_TYPE));
        root.add("advancements_note", new JsonArray());
        try {
            Files.writeString(Path.of(target), Create.GSON.toJson(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonArray names(net.minecraft.core.Registry<?> registry) {
        JsonArray a = new JsonArray();
        for (var key : registry.keySet()) {
            if (key.getNamespace().equals(Create.MOD_ID)) {
                a.add(key.getPath());
            }
        }
        return a;
    }

    /** Developer aid: force-applies every Create mixin by loading its target classes, printing failures as MIXINFAIL lines. */
    private static void loadMixinTargets() {
        ClassLoader cl = DevDump.class.getClassLoader();
        int count = 0;
        for (String cfgName : new String[]{"create.mixins.json", "create.client.mixins.json"}) {
            try (var in = DevDump.class.getClassLoader().getResourceAsStream(cfgName)) {
                if (in == null) {
                    continue;
                }
                JsonObject cfg = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in)).getAsJsonObject();
                String pkg = cfg.get("package").getAsString();
                java.util.List<String> names = new java.util.ArrayList<>();
                try (var jar = new java.util.jar.JarFile(new java.io.File(DevDump.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
                    String prefix = pkg.replace('.', '/') + "/";
                    jar.stream().map(java.util.zip.ZipEntry::getName).filter(n -> n.startsWith(prefix) && n.endsWith(".class") && n.indexOf('/', prefix.length()) < 0)
                        .forEach(n -> names.add(n.substring(prefix.length(), n.length() - 6)));
                }
                for (String n : names) {
                    String res = (pkg + "." + n).replace('.', '/') + ".class";
                    try (var cin = DevDump.class.getClassLoader().getResourceAsStream(res)) {
                        if (cin == null) {
                            continue;
                        }
                        var reader = new org.objectweb.asm.ClassReader(cin);
                        java.util.List<String> targets = new java.util.ArrayList<>();
                        reader.accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9) {
                            @Override
                            public org.objectweb.asm.AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                                if (!desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
                                    return null;
                                }
                                return new org.objectweb.asm.AnnotationVisitor(org.objectweb.asm.Opcodes.ASM9) {
                                    @Override
                                    public org.objectweb.asm.AnnotationVisitor visitArray(String name) {
                                        return new org.objectweb.asm.AnnotationVisitor(org.objectweb.asm.Opcodes.ASM9) {
                                            @Override
                                            public void visit(String n2, Object value) {
                                                if (value instanceof org.objectweb.asm.Type t) {
                                                    targets.add(t.getClassName());
                                                } else if (value instanceof String str) {
                                                    targets.add(str);
                                                }
                                            }
                                        };
                                    }
                                };
                            }
                        }, org.objectweb.asm.ClassReader.SKIP_CODE);
                        for (String t : targets) {
                            count++;
                            if (System.getProperty("create.loadMixinTargets").equals("v")) System.out.println("MIXINLOAD " + t);
                            try {
                                Class.forName(t, false, cl);
                            } catch (Throwable e) {
                                Throwable c = e;
                                while (c.getCause() != null) {
                                    c = c.getCause();
                                }
                                System.out.println("MIXINFAIL " + n + " " + t + " :: " + e.getClass().getSimpleName() + " :: " + String.valueOf(c.getMessage()).replace('\n', ' '));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("MIXINFAIL cfg " + cfgName + " " + e);
            }
        }
        System.out.println("MIXINFAIL-DONE " + count);
    }
}
