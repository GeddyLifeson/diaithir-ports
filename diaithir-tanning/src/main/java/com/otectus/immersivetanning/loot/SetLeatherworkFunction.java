package com.otectus.immersivetanning.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.otectus.immersivetanning.leather.Finish;
import com.otectus.immersivetanning.leather.VillageLeatherwork;
import com.otectus.immersivetanning.trait.HideTraitLoader;
import com.otectus.immersivetanning.trait.TraitRef;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Loot function {@code immersive_tanning:set_leatherwork}: makes an item a finished piece from someone else's bench.
 * <pre>
 * { "function": "immersive_tanning:set_leatherwork",
 *   "finish": "harness",                                  // optional; a random finish without it
 *   "integrity": {"type": "minecraft:uniform", "min": 60, "max": 90},   // optional number providers;
 *   "workmanship": 75,                                                  // village quality without them
 *   "trait": "immersive_tanning:tough" }                  // optional
 * </pre>
 */
public class SetLeatherworkFunction extends LootItemConditionalFunction {
    private static final Codec<Finish> FINISH_CODEC = Codec.STRING.comapFlatMap(
            s -> {
                Finish f = Finish.byName(s);
                return f == null ? com.mojang.serialization.DataResult.error(() -> "finish must be supple, harness or hardened") : com.mojang.serialization.DataResult.success(f);
            }, Finish::getSerializedName);

    public static final MapCodec<SetLeatherworkFunction> CODEC = RecordCodecBuilder.mapCodec(i -> commonFields(i).and(i.group(
            FINISH_CODEC.optionalFieldOf("finish").forGetter(f -> Optional.ofNullable(f.finish)),
            NumberProviders.CODEC.optionalFieldOf("integrity").forGetter(f -> Optional.ofNullable(f.integrity)),
            NumberProviders.CODEC.optionalFieldOf("workmanship").forGetter(f -> Optional.ofNullable(f.workmanship)),
            Identifier.CODEC.optionalFieldOf("trait").forGetter(f -> Optional.ofNullable(f.trait))))
            .apply(i, (conditions, finish, integrity, workmanship, trait) ->
                    new SetLeatherworkFunction(conditions, finish.orElse(null), integrity.orElse(null), workmanship.orElse(null), trait.orElse(null))));

    @Nullable private final Finish finish;
    @Nullable private final NumberProvider integrity;
    @Nullable private final NumberProvider workmanship;
    @Nullable private final Identifier trait;

    SetLeatherworkFunction(List<LootItemCondition> conditions, @Nullable Finish finish, @Nullable NumberProvider integrity,
                           @Nullable NumberProvider workmanship, @Nullable Identifier trait) {
        super(conditions);
        this.finish = finish;
        this.integrity = integrity;
        this.workmanship = workmanship;
        this.trait = trait;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        Finish chosen = finish != null ? finish : VillageLeatherwork.finish(context.getRandom());
        int i = integrity != null ? Mth.clamp(integrity.getInt(context), 0, 100) : VillageLeatherwork.score(context.getRandom());
        int w = workmanship != null ? Mth.clamp(workmanship.getInt(context), 0, 100) : VillageLeatherwork.score(context.getRandom());
        TraitRef ref = trait == null ? null : HideTraitLoader.get(trait);
        return VillageLeatherwork.make(stack, chosen, i, w, ref, null);
    }

    @Override
    public MapCodec<SetLeatherworkFunction> codec() {
        return CODEC;
    }
}
