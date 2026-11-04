package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Territory, replayed against tools/economy/golden_claims.js. */
class EconClaimsTest {
    @Test
    void claimsMatch() {
        JsonObject g = JsonParser.parseReader(new InputStreamReader(EconClaimsTest.class.getResourceAsStream("/econ_claims_golden.json"))).getAsJsonObject();
        List<EconClaims.Claim> got = EconClaims.fromSim(g.getAsJsonObject("sim"));
        JsonArray want = g.getAsJsonArray("claims");
        assertEquals(want.size(), got.size());
        for (int i = 0; i < want.size(); i++) {
            JsonObject w = want.get(i).getAsJsonObject();
            EconClaims.Claim c = got.get(i);
            assertEquals(w.get("x").getAsDouble(), c.x());
            assertEquals(w.get("z").getAsDouble(), c.z());
            assertEquals(w.get("r").getAsLong(), c.r(), "radius of " + c.town());
            assertEquals(w.get("culture").getAsString(), c.culture());
            assertEquals(w.get("realm").getAsString(), c.realm());
            assertEquals(w.get("town").getAsString(), c.town());
            assertEquals(w.get("name").getAsString(), c.name());
        }
        for (JsonElement e : g.getAsJsonArray("at")) {
            JsonArray a = e.getAsJsonArray();
            EconClaims.Claim c = EconClaims.at(got, a.get(0).getAsJsonArray().get(0).getAsDouble(), a.get(0).getAsJsonArray().get(1).getAsDouble());
            assertEquals(a.get(1).isJsonNull() ? null : a.get(1).getAsString(), c == null ? null : c.town());
        }
        String[] in = {"Old Gael", "", null, "  A-b__C  ", "Ünï!"};
        for (int i = 0; i < in.length; i++) assertEquals(g.getAsJsonArray("slugs").get(i).getAsString(), EconClaims.slug(in[i]));
    }
}
