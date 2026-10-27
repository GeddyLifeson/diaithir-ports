package com.otectus.immersivetanning.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.otectus.immersivetanning.hide.HideSourceLoader;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Adds the hide an entity's hide source names to its death loot and, by default, removes the leather it would
 * otherwise drop (see {@link HideDrops}). Applies only to the entity's own loot table, never to chests or fishing
 * opened by an entity.
 */
public class HideDropModifier extends LootModifier {
    public static final MapCodec<HideDropModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, HideDropModifier::new));

    public HideDropModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof LivingEntity living) || !context.hasParameter(LootContextParams.DAMAGE_SOURCE)) return loot;
        if (!living.getLootTable().map(key -> key.identifier().equals(context.getQueriedLootTableId())).orElse(false)) return loot;
        HideSourceLoader.HideSource source = HideSourceLoader.forEntity(living.getType());
        if (source == null) return loot;

        HideDrops.apply(loot, living, source, context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY), 0,
                context.getRandom());
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
