package net.diaithir.portable;

import net.minecraft.world.item.Item;

public final class UpgradeItem extends Item {
    private final UpgradeKind kind;

    public UpgradeItem(Properties p, UpgradeKind kind) {
        super(p.stacksTo(16));
        this.kind = kind;
    }

    public UpgradeKind kind() {
        return kind;
    }
}
