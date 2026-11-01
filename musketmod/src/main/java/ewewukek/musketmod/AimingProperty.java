package ewewukek.musketmod;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record AimingProperty() implements ConditionalItemModelProperty {
    public static final MapCodec<AimingProperty> MAP_CODEC = MapCodec.unit(new AimingProperty());

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext context) {
        return entity != null && stack.getItem() instanceof GunItem && ClientUtilities.shouldAim(entity, stack);
    }

    @Override
    public MapCodec<AimingProperty> type() {
        return MAP_CODEC;
    }
}
