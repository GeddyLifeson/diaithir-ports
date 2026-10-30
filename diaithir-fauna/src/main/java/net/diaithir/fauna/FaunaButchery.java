package net.diaithir.fauna;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Link to diaithir-butchery (optional). tools/gen_butchery.py maps every species to a carcass class
 * (butchery_species.json); the classes themselves are in data/diaithirfauna/butchery/fauna.json, which the butchery
 * mod reads. No compile dependency: the carcass item and its components are looked up in the registries.
 */
public final class FaunaButchery {
    private static final Map<String, String> CLASS_OF = new HashMap<>();

    static {
        try (Reader r = new InputStreamReader(FaunaButchery.class.getResourceAsStream("/data/diaithirfauna/butchery_species.json"), StandardCharsets.UTF_8)) {
            for (var e : new Gson().fromJson(r, JsonObject.class).entrySet()) CLASS_OF.put(e.getKey(), e.getValue().getAsString());
        } catch (Exception ex) {
            // no mapping: vanilla drops only
        }
    }

    private FaunaButchery() {
    }

    /** Carcass class of a species, or "" if it is too small to butcher. */
    public static String classOf(Species s) {
        return CLASS_OF.getOrDefault(s.id, "");
    }

    /** A butchery carcass for this species killed at gameTime, or null when diaithir-butchery is not loaded or the species is not butchered. */
    @SuppressWarnings("unchecked")
    public static ItemStack carcass(Species s, FaunaMob mob, long gameTime) {
        String cls = classOf(s);
        if (cls.isEmpty()) return null;
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse("diaithirbutchery:carcass"));
        if (item == null || item == Items.AIR) return null;
        DataComponentType<String> species = (DataComponentType<String>) BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.parse("diaithirbutchery:species"));
        DataComponentType<String> body = (DataComponentType<String>) BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.parse("diaithirbutchery:body"));
        DataComponentType<Long> killed = (DataComponentType<Long>) BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(Identifier.parse("diaithirbutchery:killed"));
        if (species == null || body == null || killed == null) return null;
        ItemStack st = new ItemStack(item);
        st.set(species, "diaithirfauna:" + s.id);
        st.set(body, cls);
        st.set(killed, gameTime);
        st.set(DataComponents.ITEM_NAME, Component.translatable("item.diaithirbutchery.carcass_of", Component.translatable("species.diaithirfauna." + s.id)));
        return st;
    }
}
