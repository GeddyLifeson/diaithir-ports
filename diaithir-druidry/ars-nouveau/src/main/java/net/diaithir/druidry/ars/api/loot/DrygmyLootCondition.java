package net.diaithir.druidry.ars.api.loot;

import com.mojang.serialization.MapCodec;
import net.diaithir.druidry.ars.api.ANFakePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class DrygmyLootCondition implements LootItemCondition {
    public static final MapCodec<DrygmyLootCondition> CODEC = MapCodec.unit(DrygmyLootCondition::new);

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY);

        return entity instanceof ANFakePlayer;
    }

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

    public static DrygmyLootCondition.Builder drygmy() {
        return new Builder();
    }

    public static class Builder implements LootItemCondition.Builder {
        public DrygmyLootCondition build() {
            return new DrygmyLootCondition();
        }
    }
}
