package com.iafenvoy.jupiter.test;

import com.iafenvoy.jupiter.Jupiter;
import com.iafenvoy.jupiter.config.container.FileConfigContainer;
import com.iafenvoy.jupiter.config.entry.*;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public class TestConfig extends FileConfigContainer {
    public static final TestConfig INSTANCE = new TestConfig();

    public TestConfig() {
        super(Identifier.fromNamespaceAndPath(Jupiter.MOD_ID, "test"), "jupiter.test", "./config/jupiter.json");
    }

    @Override
    public void init() {
        this.createTab("tab1", "jupiter.tab1")
                .addEntry(BooleanEntry.builder("this is boolean", false).build())
                .addEntry(IntegerEntry.builder("this is int", 0).build())
                .addEntry(IntegerEntry.builder("this is int with range", 0).min(-10).max(10).build())
                .addEntry(StringEntry.builder("this is string", "").build())
                .addEntry(ListStringEntry.builder("this is string list", List.of("1", "2", "3", "4", "5")).build())
                .addEntry(MapStringEntry.builder("this is string map", Map.of("1", "1", "2", "2")).build())
                .addEntry(SeparatorEntry.builder().build())
                .addEntry(IntegerEntry.builder("this is int", 0).restartRequired().build())
                .addEntry(IntegerEntry.builder("this is int", 0).build())
                .addEntry(IntegerEntry.builder("this is int", 0).build())
                .addEntry(IntegerEntry.builder("this is int", 0).build())
                .addEntry(IntegerEntry.builder("this is int", 0).build())
                .addEntry(IntegerEntry.builder("this is int", 0).build());
        this.createTab("tab2", "jupiter.tab2");
        this.createTab("tab3", "jupiter.tab3");
        this.createTab("tab4", "jupiter.tab4");
        this.createTab("tab5", "jupiter.tab5");
        this.createTab("tab6", "jupiter.tab6");
        this.createTab("tab7", "jupiter.tab7");
        this.createTab("tab8", "jupiter.tab8");
        this.createTab("tab9", "jupiter.tab9");
    }
}