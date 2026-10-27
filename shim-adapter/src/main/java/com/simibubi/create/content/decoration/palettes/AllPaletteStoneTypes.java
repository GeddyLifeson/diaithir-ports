package com.simibubi.create.content.decoration.palettes;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** Shim of Create's palette stone enum: base blocks resolve lazily to the port's create:<name> blocks. */
public enum AllPaletteStoneTypes {
    LIMESTONE, WEATHERED_LIMESTONE, SCORIA, SCORCHIA, ASURINE, CRIMSITE, OCHRUM, VERIDIUM, TUFF, DEEPSLATE, DRIPSTONE, CALCITE;

    public final TagKey<Item> materialTag;
    private final String id;

    AllPaletteStoneTypes() {
        this.id = name().toLowerCase();
        this.materialTag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("create", "stone_types/" + id));
    }

    public NonNullSupplier<Block> getBaseBlock() {
        return () -> {
            Identifier rl = this == TUFF || this == DEEPSLATE || this == DRIPSTONE || this == CALCITE
                ? Identifier.withDefaultNamespace(id.equals("dripstone") ? "dripstone_block" : id)
                : Identifier.fromNamespaceAndPath("create", id);
            return BuiltInRegistries.BLOCK.getValue(rl);
        };
    }
}
