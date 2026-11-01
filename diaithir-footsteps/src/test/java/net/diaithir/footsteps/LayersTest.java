package net.diaithir.footsteps;

import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LayersTest {
    private final Layers layers = Layers.load(new InputStreamReader(getClass().getResourceAsStream("/data/diaithirfootsteps/footsteps.json")));

    @Test
    void onlyStepSoundsAreLayered() {
        assertTrue(layers.isStep("block.stone.step"));
        assertFalse(layers.isStep("block.stone.break"));
    }

    @Test
    void unarmouredGetsNoClink() {
        assertNull(layers.armorLayer(List.of("", "", "", ""), false, false));
    }

    @Test
    void commonestMaterialSetsTheSoundAndPiecesAddVolume() {
        Layers.Layer mixed = layers.armorLayer(List.of("iron", "iron", "leather", ""), false, false);
        assertEquals("minecraft:item.armor.equip_iron", mixed.sound());
        Layers.Layer one = layers.armorLayer(List.of("", "", "", "iron"), false, false);
        Layers.Layer full = layers.armorLayer(List.of("iron", "iron", "iron", "iron"), false, false);
        assertTrue(full.volume() > mixed.volume() && mixed.volume() > one.volume());
        assertEquals(0.20F, full.volume(), 1e-6);
    }

    @Test
    void sneakingIsQuieterAndSprintingLouder() {
        List<String> iron = List.of("iron", "iron", "iron", "iron");
        float walk = layers.armorLayer(iron, false, false).volume();
        assertTrue(layers.armorLayer(iron, true, false).volume() < walk);
        assertTrue(layers.armorLayer(iron, false, true).volume() > walk);
    }

    @Test
    void wetLayerNeedsRain() {
        assertNull(layers.wetLayer(false, false));
        assertNotNull(layers.wetLayer(true, false));
        assertTrue(layers.wetLayer(true, true).volume() < layers.wetLayer(true, false).volume());
    }

    @Test
    void feetAlternatePitch() {
        assertTrue(layers.stepPitch(1F, true) < layers.stepPitch(1F, false));
    }
}
