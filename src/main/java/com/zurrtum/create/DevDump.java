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
}
