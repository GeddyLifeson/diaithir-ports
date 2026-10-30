package rpggods.util;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import rpggods.RPGGods;
import rpggods.deity.DeityHelper;
import rpggods.RGEvents;
import rpggods.favor.IFavor;
import rpggods.perk.Perk;
import rpggods.perk.PerkAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CropMultiplierModifier extends LootModifier {

    public static final MapCodec<CropMultiplierModifier> CODEC = RecordCodecBuilder.mapCodec(inst ->
            codecStart(inst)
                    .and(TagKey.codec(Registries.BLOCK).fieldOf("crops").forGetter(CropMultiplierModifier::getCrops))
                    .apply(inst, CropMultiplierModifier::new));

    private final TagKey<Block> crops;

    protected CropMultiplierModifier(final LootItemCondition[] conditionsIn, final int priority, final TagKey<Block> crops) {
        super(conditionsIn, priority);
        this.crops = crops;
    }

    public TagKey<Block> getCrops() {
        return crops;
    }

    @Override
    public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        BlockState block = context.getOptionalParameter(LootContextParams.BLOCK_STATE);
        // do not apply when entity is null or breaking non-crops
        if(entity == null || block == null || !block.is(crops)) {
            return generatedLoot;
        }
        // determine which of the mining effects can activate
        List<Identifier> cropHarvest = new ArrayList<>();
        for (DeityHelper deity : RPGGods.DEITY_HELPER.values()) {
            cropHarvest.addAll(deity.perkByTypeMap.getOrDefault(PerkAction.Type.CROP_HARVEST, ImmutableList.of()));
        }
        // make sure this is an ore mined by a non-creative player
        if (entity instanceof Player && !entity.isSpectator() && !((Player) entity).isCreative()
                && !cropHarvest.isEmpty()) {
            final Player player = (Player) entity;
            final java.util.Optional<IFavor> favor = RPGGods.getFavor(player);
            // determine results using player favor
            if (favor.isPresent() && favor.get().isEnabled()) {
                IFavor f = favor.get();
                // attempt to add or remove crops
                Collections.shuffle(cropHarvest);
                Perk perk;
                for (Identifier id : cropHarvest) {
                    perk = RPGGods.PERK_MAP.get(id);
                    if (RGEvents.runPerk(perk, player, f)) {
                        float multiplier = 0;
                        for(PerkAction action : perk.getActions()) {
                            if(action.getType() == PerkAction.Type.CROP_HARVEST && action.getMultiplier().isPresent()) {
                                multiplier += action.getMultiplier().get();
                            }
                        }
                        int amount = Math.round(multiplier);
                        if(amount != 0) {
                            generatedLoot.forEach(i -> i.grow(amount));
                        }
                    }
                }
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
