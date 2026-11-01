package rpggods.util;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.minecraft.core.registries.BuiltInRegistries;
import rpggods.RPGGods;
import rpggods.deity.DeityHelper;
import rpggods.RGEvents;
import rpggods.favor.IFavor;
import rpggods.perk.Perk;
import rpggods.perk.PerkAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AutosmeltOrCobbleModifier extends LootModifier {

    public static final MapCodec<AutosmeltOrCobbleModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst)
                    .and(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stone").forGetter(AutosmeltOrCobbleModifier::getStone))
                    .and(TagKey.codec(Registries.BLOCK).fieldOf("ores").forGetter(AutosmeltOrCobbleModifier::getOres))
                    .apply(inst, AutosmeltOrCobbleModifier::new));

    private final Block stone;
    private final TagKey<Block> ores;

    protected AutosmeltOrCobbleModifier(final LootItemCondition[] conditions, final int priority, final Block stone, final TagKey<Block> ores) {
        super(conditions, priority);
        this.stone = stone;
        this.ores = ores;
    }

    public Block getStone() {
        return stone;
    }

    public TagKey<Block> getOres() {
        return ores;
    }

    @Override
    public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        net.minecraft.world.item.ItemInstance toolInstance = context.getOptionalParameter(LootContextParams.TOOL);
        ItemStack itemStack = toolInstance instanceof ItemStack tool ? tool : null;
        BlockState block = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
        // do not apply when missing entity or item or breaking non-ore block
        if(entity == null || itemStack == null || block == null || !block.is(ores)) {
            return generatedLoot;
        }
        // determine which of the mining effects can activate
        List<Identifier> autosmelt = new ArrayList<>();
        List<Identifier> unsmelt = new ArrayList<>();
        for (DeityHelper deity : RPGGods.DEITY_HELPER.values()) {
            autosmelt.addAll(deity.perkByTypeMap.getOrDefault(PerkAction.Type.AUTOSMELT, ImmutableList.of()));
            unsmelt.addAll(deity.perkByTypeMap.getOrDefault(PerkAction.Type.UNSMELT, ImmutableList.of()));
        }
        // make sure this is an ore mined by a non-creative player
        if (entity instanceof Player && !entity.isSpectator() && !((Player) entity).isCreative()
                && (!autosmelt.isEmpty() || !unsmelt.isEmpty())) {
            final Player player = (Player) entity;
            final java.util.Optional<IFavor> favor = RPGGods.getFavor(player);
            // determine results using player favor
            if(favor.isPresent() && favor.get().isEnabled()) {
                IFavor f = favor.get();
                ObjectArrayList<ItemStack> replacement = new ObjectArrayList<>();
                // attempt to autosmelt
                Collections.shuffle(autosmelt);
                Perk perk;
                for(Identifier id : autosmelt) {
                    perk = RPGGods.PERK_MAP.get(id);
                    if(RGEvents.runPerk(perk, player, f)) {
                        generatedLoot.forEach((stack) -> replacement.add(smelt(stack, context)));
                        return replacement;
                    }
                }
                // if no autosmelt occurred, attempt to unsmelt
                Collections.shuffle(unsmelt);
                for(Identifier id : unsmelt) {
                    perk = RPGGods.PERK_MAP.get(id);
                    if(RGEvents.runPerk(perk, player, f)) {
                        replacement.add(new ItemStack(stone.asItem()));
                        return replacement;
                    }
                }

            }
        }
        return generatedLoot;
    }

    /**
     * @param stack   the item to smelt
     * @param context the loot context
     * @return the item that would normally result from smelting the given item
     */
    private static ItemStack smelt(ItemStack stack, LootContext context) {
        net.minecraft.world.item.crafting.SingleRecipeInput input = new net.minecraft.world.item.crafting.SingleRecipeInput(stack);
        return context.getLevel().recipeAccess().getRecipeFor(RecipeType.SMELTING, input, context.getLevel())
                .map(holder -> holder.value().assemble(input))
                .filter(itemStack -> !itemStack.isEmpty())
                .map(itemStack -> itemStack.copyWithCount(stack.getCount() * itemStack.getCount()))
                .orElse(stack);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
