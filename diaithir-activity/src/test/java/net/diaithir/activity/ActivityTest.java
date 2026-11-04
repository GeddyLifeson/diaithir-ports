package net.diaithir.activity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ActivityTest {
    static JsonObject bundled() throws IOException {
        try (var in = ActivityTest.class.getResourceAsStream("/data/diaithiractivity/activity.json")) {
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    static ActivityData data() throws IOException {
        return ActivityData.merge(List.of(bundled()));
    }

    static String decide(ActivityData d, ActivityLogic.Snapshot s) {
        return ActivityLogic.decide(s, d.defs(), Set.of(), 2400);
    }

    static ActivityLogic.Snapshot snap(long now, boolean sleep, boolean ride, boolean fish, String menu, Map<String, Long> last, long idle) {
        return new ActivityLogic.Snapshot(now, sleep, ride, fish, menu, last, idle);
    }

    @Test
    void nothingHappeningShowsNothing() throws IOException {
        assertEquals("", decide(data(), snap(1000, false, false, false, null, Map.of(), 10)));
    }

    @Test
    void recentActionShowsThenExpiresAfterItsHold() throws IOException {
        var d = data();
        assertEquals("mining", decide(d, snap(1000, false, false, false, null, Map.of("mining", 950L), 5)));
        assertEquals("", decide(d, snap(1000, false, false, false, null, Map.of("mining", 800L), 5)));
    }

    @Test
    void priorityPicksFightingOverMiningAndSleepingOverAll() throws IOException {
        var d = data();
        Map<String, Long> last = Map.of("mining", 990L, "fighting", 990L);
        assertEquals("fighting", decide(d, snap(1000, false, false, false, null, last, 5)));
        assertEquals("sleeping", decide(d, snap(1000, true, true, true, "brewing", last, 99999)));
    }

    @Test
    void stationMenuBeatsRidingAndMining() throws IOException {
        var d = data();
        assertEquals("brewing", decide(d, snap(1000, false, true, false, "brewing", Map.of("mining", 999L), 5)));
        assertEquals("mining", decide(d, snap(1000, false, true, false, null, Map.of("mining", 999L), 5)), "working beats merely riding");
    }

    @Test
    void afkOnlyAfterTheThresholdAndLosesToEverythingElse() throws IOException {
        var d = data();
        assertEquals("", decide(d, snap(5000, false, false, false, null, Map.of(), 2399)));
        assertEquals("afk", decide(d, snap(5000, false, false, false, null, Map.of(), 2400)));
        assertEquals("riding", decide(d, snap(5000, false, true, false, null, Map.of(), 9000)));
    }

    @Test
    void disabledActivityFallsThroughToTheNextOne() throws IOException {
        var d = data();
        var s = snap(1000, false, false, false, null, Map.of("fighting", 990L, "mining", 990L), 5);
        assertEquals("mining", ActivityLogic.decide(s, d.defs(), Set.of("fighting"), 2400));
    }

    @Test
    void unknownMenuActivityFromAnotherModIsIgnoredUnlessDefined() throws IOException {
        var d = data();
        assertEquals("", decide(d, snap(1000, false, false, false, "mymod_press", Map.of(), 5)));
    }

    @Test
    void bundledRulesResolveVanillaThings() throws IOException {
        var d = data();
        assertEquals("farming", d.resolve("breaks", Set.of("minecraft:wheat", "#minecraft:crops")));
        assertEquals("chopping", d.resolve("breaks", Set.of("minecraft:oak_log", "#minecraft:logs")));
        assertEquals("mining", d.resolve("tools", Set.of("minecraft:iron_pickaxe", "#minecraft:pickaxes")));
        assertEquals("brewing", d.resolve("menus", Set.of("minecraft:brewing_stand")));
        assertEquals("reading", d.resolve("uses", Set.of("minecraft:written_book")));
        assertNull(d.resolve("tools", Set.of("minecraft:stick")));
    }

    @Test
    void everyActivityHasALabelKeyAndEveryRuleTargetsARealActivity() throws IOException {
        var d = data();
        for (String section : ActivityData.SECTIONS) {
            for (var r : d.rules(section)) assertTrue(d.defs().containsKey(r.activity()), section + " -> " + r.activity());
        }
        String lang = new String(ActivityTest.class.getResourceAsStream("/assets/diaithiractivity/lang/en_us.json").readAllBytes(), StandardCharsets.UTF_8);
        for (var def : d.defs().values()) {
            assertTrue(lang.contains("\"" + def.label() + "\""), def.label());
            assertFalse(lang.contains("—"), "no em dashes");
        }
    }

    @Test
    void anotherModCanAddAStationAndOverrideARule() throws IOException {
        JsonObject extra = JsonParser.parseString("""
            {"activities": {"pressing": {"sprite": "mymod:item/press", "priority": 80}, "afk": null},
             "menus": [{"match": "mymod:press", "activity": "pressing"}],
             "tools": [{"match": "#minecraft:pickaxes", "activity": "digging"}]}""").getAsJsonObject();
        var d = ActivityData.merge(List.of(bundled(), extra));
        assertEquals("pressing", d.resolve("menus", Set.of("mymod:press")));
        assertEquals("digging", d.resolve("tools", Set.of("#minecraft:pickaxes")), "later document wins");
        assertFalse(d.defs().containsKey("afk"), "null removes an activity");
        assertEquals("pressing", decide(d, snap(10, false, false, false, "pressing", Map.of(), 1)));
        assertEquals("mymod:item/press", d.defs().get("pressing").sprite());
    }

    @Test
    void ruleToAnUndefinedActivityIsSkipped() {
        JsonObject doc = JsonParser.parseString("""
            {"activities": {"a": {"sprite": "x:y"}}, "tools": [{"match": "x:t", "activity": "gone"}, {"match": "x:t", "activity": "a"}]}""").getAsJsonObject();
        assertEquals("a", ActivityData.merge(List.of(doc)).resolve("tools", Set.of("x:t")));
    }

    @Test
    void optOutPersists() throws IOException {
        Path f = Files.createTempDirectory("optout").resolve("data").resolve("o.json");
        UUID u = UUID.randomUUID();
        OptOut a = new OptOut(f);
        assertFalse(a.isHidden(u));
        assertTrue(a.set(u, true));
        assertFalse(a.set(u, true));
        assertTrue(new OptOut(f).isHidden(u));
        a.set(u, false);
        assertFalse(new OptOut(f).isHidden(u));
    }
}
