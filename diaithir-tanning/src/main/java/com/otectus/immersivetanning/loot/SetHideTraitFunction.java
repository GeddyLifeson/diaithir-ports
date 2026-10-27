package com.otectus.immersivetanning.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.otectus.immersivetanning.ImmersiveTanning;
import com.otectus.immersivetanning.leather.VillageLeatherwork;
import com.otectus.immersivetanning.trait.HideTraitLoader;
import com.otectus.immersivetanning.trait.TraitRef;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

/**
 * Loot function {@code immersive_tanning:set_hide_trait}: gives a raw or processed hide, finished leather or a finished
 * piece an innate trait, so chests, treasure bags and quest rewards can hand out trophy hides.
 * <pre>{ "function": "immersive_tanning:set_hide_trait", "trait": "immersive_tanning:insulating" }</pre>
 */
public class SetHideTraitFunction extends LootItemConditionalFunction {
    public static final MapCodec<SetHideTraitFunction> CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i)
            .and(Identifier.CODEC.fieldOf("trait").forGetter(f -> f.trait))
            .apply(i, SetHideTraitFunction::new));

    private final Identifier trait;

    SetHideTraitFunction(List<LootItemCondition> conditions, Identifier trait) {
        super(conditions);
        this.trait = trait;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        TraitRef ref = HideTraitLoader.get(trait);
        if (ref == null) {
            ImmersiveTanning.LOGGER.warn("set_hide_trait names unknown hide trait {}", trait);
            return stack;
        }
        return VillageLeatherwork.withTrait(stack, ref);
    }

    @Override
    public MapCodec<SetHideTraitFunction> codec() {
        return CODEC;
    }
}
