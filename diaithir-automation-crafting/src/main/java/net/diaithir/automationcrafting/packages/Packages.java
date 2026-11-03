package net.diaithir.automationcrafting.packages;

import com.mojang.logging.LogUtils;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.diaithir.automationcrafting.DiaithirAutomationCrafting;

/** The loaded package definitions (data/&lt;ns&gt;/package_defs/*.json) and helpers to build package items. */
public final class Packages {
    private static final Logger LOG = LogUtils.getLogger();
    private static volatile Map<String, PackageDef> defs = Map.of();

    private Packages() {}

    public static PackageDef get(String id) {
        return defs.get(id);
    }

    public static List<PackageDef> all() {
        return new ArrayList<>(defs.values());
    }

    public static void onAddListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(DiaithirAutomationCrafting.ID, "package_defs"), new Loader());
    }

    private static final class Loader extends SimpleJsonResourceReloadListener<JsonElement> {
        Loader() {
            super(ExtraCodecs.JSON, FileToIdConverter.json("package_defs"));
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            Map<String, PackageDef> next = new TreeMap<>();
            for (Map.Entry<Identifier, JsonElement> f : files.entrySet()) {
                String id = f.getKey().toString();
                PackageDef d = PackageDef.parse(id, f.getValue(), m -> LOG.warn("package {}: {}", id, m));
                if (d != null && itemOf(d.result().item()) != Items.AIR) next.put(id, d);
                else if (d != null) LOG.warn("package {}: unknown result item {}", id, d.result().item());
            }
            defs = next;
            LOG.info("Loaded {} package definitions", next.size());
        }
    }

    public static Item itemOf(String id) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(Items.AIR);
    }

    public static String keyOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    /** A crate of the given package. */
    public static ItemStack crate(PackageDef d, int count) {
        ItemStack s = new ItemStack(DiaithirAutomationCrafting.PACKAGE.get(), count);
        s.set(DiaithirAutomationCrafting.PACKAGE_ID.get(), d.id());
        s.set(DataComponents.CUSTOM_NAME, Component.literal(d.name() + " package"));
        return s;
    }

    /** The package id a crate holds, or "" for anything else. */
    public static String crateId(ItemStack s) {
        String id = s.get(DiaithirAutomationCrafting.PACKAGE_ID.get());
        return id == null ? "" : id;
    }
}
