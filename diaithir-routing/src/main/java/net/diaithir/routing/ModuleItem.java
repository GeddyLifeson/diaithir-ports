package net.diaithir.routing;

import net.minecraft.world.item.Item;

/** A router module. The behaviour lives in Transfers and RouterBlockEntity; the item only carries its kind. */
public final class ModuleItem extends Item {
    private final ModuleKind kind;

    public ModuleItem(Properties properties, ModuleKind kind) {
        super(properties);
        this.kind = kind;
    }

    public ModuleKind kind() {
        return kind;
    }
}
