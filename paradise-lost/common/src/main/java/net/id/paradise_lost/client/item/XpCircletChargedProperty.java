package net.id.paradise_lost.client.item;

import com.mojang.serialization.MapCodec;
import net.id.paradise_lost.item.armor.XpCircletItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Item model condition "paradise_lost:charged", replacing the pre-26.1 ItemProperties predicate.
 */
public record XpCircletChargedProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<XpCircletChargedProperty> MAP_CODEC = MapCodec.unit(new XpCircletChargedProperty());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext context) {
        return XpCircletItem.isCharged(stack);
    }

    @Override
    public MapCodec<XpCircletChargedProperty> type() {
        return MAP_CODEC;
    }
}
