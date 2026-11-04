package com.otectus.immersivetanning.registry;

import com.mojang.serialization.MapCodec;
import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.loot.SetHideTraitFunction;
import com.otectus.immersivetanning.loot.SetLeatherworkFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModLootFunctions {
    public static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, ImmersiveTanning.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<SetHideTraitFunction>> SET_HIDE_TRAIT =
            LOOT_FUNCTIONS.register("set_hide_trait", () -> SetHideTraitFunction.CODEC);

    public static final DeferredHolder<MapCodec<? extends LootItemFunction>, MapCodec<SetLeatherworkFunction>> SET_LEATHERWORK =
            LOOT_FUNCTIONS.register("set_leatherwork", () -> SetLeatherworkFunction.CODEC);

    private ModLootFunctions() {}
}
