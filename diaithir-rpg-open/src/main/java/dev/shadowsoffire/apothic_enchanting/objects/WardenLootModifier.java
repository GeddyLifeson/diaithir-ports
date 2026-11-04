package dev.shadowsoffire.apothic_enchanting.objects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_enchanting.Ench;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class WardenLootModifier extends LootModifier {

    public static final MapCodec<WardenLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, WardenLootModifier::new));
    public static final Identifier WARDEN_TABLE_ID = Identifier.withDefaultNamespace("entities/warden");

    public WardenLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        if (WARDEN_TABLE_ID.equals(ctx.getQueriedLootTableId())) {
            int amount = 1;
            ItemInstance tool = ctx.getOptionalParameter(LootContextParams.TOOL);
            int looting = tool != null ? tool.getEnchantmentLevel(ctx.getLevel().holderOrThrow(Enchantments.LOOTING)) : 0;
            if (ctx.getRandom().nextFloat() <= 0.10F + looting * 0.10F) {
                amount++;
            }
            loot.add(new ItemStack(Ench.Items.WARDEN_TENDRIL.value(), amount));
        }
        return loot;
    }

}
