package com.gregtechceu.gtceu.compat;

import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.Set;

/** Item abilities that existed in NeoForge 1.21.1 but are gone in 26.1 (digging is now driven by tool rules). */
public final class LegacyAbilities {

    public static final ItemAbility AXE_DIG = ItemAbility.get("axe_dig");
    public static final ItemAbility PICKAXE_DIG = ItemAbility.get("pickaxe_dig");
    public static final ItemAbility SHOVEL_DIG = ItemAbility.get("shovel_dig");
    public static final ItemAbility HOE_DIG = ItemAbility.get("hoe_dig");
    public static final ItemAbility SWORD_DIG = ItemAbility.get("sword_dig");
    public static final Set<ItemAbility> DEFAULT_PICKAXE_ACTIONS = Set.of(PICKAXE_DIG);
    public static final Set<ItemAbility> DEFAULT_SWORD_ACTIONS = Set.of(SWORD_DIG, ItemAbilities.SWORD_SWEEP);

    private LegacyAbilities() {}
}
