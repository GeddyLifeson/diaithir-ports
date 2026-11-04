package net.diaithir.bosses.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.diaithir.bosses.DiaithirBosses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

/** Builds and caches each planet boss's model from assets/diaithirbosses/looks/&lt;id&gt;.json (cuboids per named part). */
final class LookStore {
    private static final Map<String, LookModel> MODELS = new HashMap<>();
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();
    private static final String FALLBACK = "{\"tex\":64,\"parts\":[{\"n\":\"body\",\"o\":[0,12,0],\"r\":[0,0,0],\"c\":[[-6,0,-4,12,12,8,0,0]]}]}";

    static LookModel model(String id) {
        return MODELS.computeIfAbsent(id, LookStore::load);
    }

    static Identifier texture(String id) {
        return TEXTURES.computeIfAbsent(id, k -> Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "textures/entity/planet/" + k + ".png"));
    }

    private static LookModel load(String id) {
        JsonObject o;
        try {
            var res = Minecraft.getInstance().getResourceManager().getResource(Identifier.fromNamespaceAndPath(DiaithirBosses.ID, "looks/" + id + ".json"));
            if (res.isPresent()) {
                try (Reader r = new InputStreamReader(res.get().open(), StandardCharsets.UTF_8)) {
                    o = JsonParser.parseReader(r).getAsJsonObject();
                }
            } else {
                o = JsonParser.parseString(FALLBACK).getAsJsonObject();
            }
        } catch (Exception e) {
            o = JsonParser.parseString(FALLBACK).getAsJsonObject();
        }
        int tex = o.get("tex").getAsInt();
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        List<String> names = new ArrayList<>();
        for (JsonElement pe : o.getAsJsonArray("parts")) {
            JsonObject p = pe.getAsJsonObject();
            CubeListBuilder cb = CubeListBuilder.create();
            for (JsonElement ce : p.getAsJsonArray("c")) {
                JsonArray c = ce.getAsJsonArray();
                cb.texOffs(c.get(6).getAsInt(), c.get(7).getAsInt())
                    .addBox(c.get(0).getAsFloat(), c.get(1).getAsFloat(), c.get(2).getAsFloat(), c.get(3).getAsFloat(), c.get(4).getAsFloat(), c.get(5).getAsFloat());
            }
            JsonArray po = p.getAsJsonArray("o"), pr = p.getAsJsonArray("r");
            String n = p.get("n").getAsString();
            root.addOrReplaceChild(n, cb, PartPose.offsetAndRotation(po.get(0).getAsFloat(), po.get(1).getAsFloat(), po.get(2).getAsFloat(),
                pr.get(0).getAsFloat(), pr.get(1).getAsFloat(), pr.get(2).getAsFloat()));
            names.add(n);
        }
        ModelPart baked = LayerDefinition.create(mesh, tex, tex).bakeRoot();
        return new LookModel(baked, names);
    }

    /** Forget cached models (resource reload). */
    static void clear() {
        MODELS.clear();
    }

    private LookStore() {}
}
