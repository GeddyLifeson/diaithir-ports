package com.otectus.immersivetanning.registry;

import com.mojang.serialization.MapCodec;
import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.loot.BindingLeatherLootModifier;
import com.otectus.immersivetanning.loot.HideDropModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ImmersiveTanning.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<HideDropModifier>> HIDE_DROPS =
            LOOT_MODIFIERS.register("hide_drops", () -> HideDropModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<BindingLeatherLootModifier>> BINDING_LEATHER =
            LOOT_MODIFIERS.register("binding_leather", () -> BindingLeatherLootModifier.CODEC);

    private ModLootModifiers() {}
}
