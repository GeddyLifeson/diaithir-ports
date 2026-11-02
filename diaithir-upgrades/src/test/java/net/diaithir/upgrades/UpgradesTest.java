package net.diaithir.upgrades;

import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class UpgradesTest {
    static final ModuleSpec SPEED1 = new ModuleSpec("speed_1", Kind.SPEED, 1, Theme.TECH, 3);
    static final ModuleSpec SPEED2 = new ModuleSpec("ogham_speed_2", Kind.SPEED, 2, Theme.MAGIC, 5);
    static final ModuleSpec PROD1 = new ModuleSpec("spice_prod_1", Kind.PRODUCTIVITY, 1, Theme.FOOD, 3);
    static final Target OVEN = new Target("oven", 2, EnumSet.of(Kind.SPEED, Kind.PRODUCTIVITY), 4);
    static final Target BEACON = new Target("beacon", 2, EnumSet.of(Kind.SPEED, Kind.EFFICIENCY), 4);
    static final Target PLAIN = new Target("press", 1, EnumSet.of(Kind.SPEED), 4);

    @Test void slotsAndKindsEnforced() {
        Slotted s = new Slotted(PLAIN);
        assertFalse(s.install(PROD1, Quality.NORMAL));
        assertTrue(s.install(SPEED1, Quality.NORMAL));
        assertFalse(s.install(SPEED1, Quality.NORMAL));
    }

    @Test void qualityScalesBenefitNotPenalty() {
        Effects n = PROD1.effects(Quality.NORMAL), l = PROD1.effects(Quality.LEGENDARY);
        assertEquals(n.productivity() * 2.5, l.productivity(), 1e-9);
        assertEquals(n.speed(), l.speed(), 1e-9);
    }

    @Test void beaconsStackWithDiminishingReturn() {
        Beacon a = new Beacon(BEACON, 3), b = new Beacon(BEACON, 3);
        a.slots().install(SPEED2, Quality.NORMAL);
        b.slots().install(SPEED2, Quality.NORMAL);
        double one = Beacon.combine(List.of(a)).speed();
        double two = Beacon.combine(List.of(a, b)).speed();
        assertTrue(two > one && two < 2 * one);
        assertEquals(0.2, one, 1e-9);
    }

    @Test void beaconNeverSharesProductivity() {
        Beacon a = new Beacon(new Target("b", 1, EnumSet.of(Kind.PRODUCTIVITY), 4), 3);
        a.slots().install(PROD1, Quality.NORMAL);
        assertEquals(0, Beacon.combine(List.of(a)).productivity());
    }

    @Test void clampsAndRoll() {
        Effects wild = new Effects(-5, 9, -5, 5).clamped();
        assertEquals(-0.8, wild.speed());
        assertEquals(3.0, wild.productivity());
        assertEquals(Quality.RARE, Resolver.rollQuality(Quality.UNCOMMON, new Effects(0, 0, 0, 0.5), 0.1, 4));
        assertEquals(Quality.UNCOMMON, Resolver.rollQuality(Quality.UNCOMMON, new Effects(0, 0, 0, 0.5), 0.9, 4));
    }

    @Test void plannerSwapsInBulkWithinBudget() {
        List<Slotted> blocks = List.of(new Slotted(OVEN), new Slotted(OVEN), new Slotted(PLAIN));
        blocks.forEach(s -> s.install(SPEED1, Quality.NORMAL));
        blocks.get(0).install(SPEED1, Quality.NORMAL);
        var plan = Planner.planModuleSwap(blocks, SPEED1, SPEED2, Quality.RARE, 3);
        assertEquals(3, plan.size());
        assertEquals(3, Planner.apply(blocks, plan));
        assertEquals(SPEED2, blocks.get(0).modules().get(0).spec());
        assertEquals(SPEED1, blocks.get(2).modules().get(0).spec());
    }

    @Test void crossModRulesWildcardTagAndShimPriority() {
        TargetRegistry reg = new TargetRegistry();
        var all = EnumSet.allOf(Kind.class);
        reg.add(new TargetRegistry.Rule("thirdmod:*", 100, 2, all, 2));
        reg.add(new TargetRegistry.Rule("#diaithirupgrades:machines", 0, 3, all, 4));
        assertEquals(2, reg.find("thirdmod:furnace", java.util.Set.of()).get().slots());
        assertEquals(3, reg.find("thirdmod:furnace", java.util.Set.of("#diaithirupgrades:machines")).get().slots());
        assertTrue(reg.find("other:rock", java.util.Set.of()).isEmpty());
    }

    @Test void realUnitsWattsAndSeconds() {
        Effects e = new Effects(0.5, 0, 0.25, 0);
        assertEquals(1250.0, Resolver.powerWatts(1000, e), 1e-9);
        assertEquals(20.0, Resolver.jobSeconds(30, e), 1e-9);
    }

    @Test void ruleLoaderParsesAndSkipsBadRows() {
        var json = com.google.gson.JsonParser.parseString(
                "{\"rules\":[{\"pattern\":\"a:*\",\"slots\":2,\"allowed\":[\"speed\"]},"
              + "{\"pattern\":\"b:x\",\"slots\":2,\"allowed\":[\"nope\"]}]}").getAsJsonObject();
        var rules = RuleLoader.parse(json);
        assertEquals(1, rules.size());
        assertEquals(4, rules.get(0).maxQuality());
    }
}
