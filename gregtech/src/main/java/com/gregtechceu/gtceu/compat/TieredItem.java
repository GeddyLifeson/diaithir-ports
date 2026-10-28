package com.gregtechceu.gtceu.compat;

import net.minecraft.world.item.Item;

/** Replacement for the removed 1.21 TieredItem. */
public class TieredItem extends Item {

    private final Tier tier;

    public TieredItem(Tier tier, Properties properties) {
        super(properties);
        this.tier = tier;
    }

    public Tier getTier() {
        return tier;
    }
}
