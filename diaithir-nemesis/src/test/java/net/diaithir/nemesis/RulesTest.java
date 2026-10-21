package net.diaithir.nemesis;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class RulesTest {
    private final Rules rules = Rules.load();
    private final Random r = new Random(7);

    @Test
    void promotionNamesRanksAndHoldsTheGrudge() {
        Nemesis n = rules.promote(1, "minecraft:zombie", "I", "p1", "Clan Odhar", r);
        assertEquals(1, n.rank);
        assertEquals("Prowler", rules.title(n));
        assertEquals(1, n.traits.size());
        assertTrue(n.players.contains("p1") && n.houses.contains("Clan Odhar"));
    }

    @Test
    void rankCapsAtTheAgesTopAndTraitsNeverRepeat() {
        Nemesis n = rules.promote(1, "minecraft:zombie", "I", "p1", "", r);
        for (int i = 0; i < 20; i++) rules.rankUp(n, "p" + i, "", r);
        assertEquals(5, n.rank);
        assertEquals("Bane of the Hearths", rules.title(n));
        assertEquals(n.traits.size(), n.traits.stream().distinct().count());
        assertEquals(rules.age("I").traits().size(), n.traits.size());
        assertTrue(n.houses.isEmpty());
    }

    @Test
    void heirKeepsGrudgeAndEpithetLosesARankAndHalfTheTraits() {
        Nemesis sire = rules.promote(1, "minecraft:skeleton", "I", "p1", "Clan Odhar", r);
        for (int i = 0; i < 3; i++) rules.rankUp(sire, "p1", "Clan Odhar", r);
        Nemesis heir = rules.heir(sire, 2, "II", r);
        assertEquals(sire.epithet, heir.epithet);
        assertNotEquals(sire.name, heir.name);
        assertEquals(sire.rank - 1, heir.rank);
        assertEquals((sire.traits.size() + 1) / 2, heir.traits.size());
        assertEquals(sire.players, heir.players);
        assertEquals(sire.houses, heir.houses);
        assertEquals(sire.fullName(), heir.sire);
        assertEquals(2, heir.generation);
        assertEquals("II", heir.age);
    }
}
