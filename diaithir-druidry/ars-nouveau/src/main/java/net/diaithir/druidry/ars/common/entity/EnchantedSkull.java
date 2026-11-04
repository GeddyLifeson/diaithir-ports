package net.diaithir.druidry.ars.common.entity;

import net.diaithir.druidry.ars.setup.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnchantedSkull extends EnchantedFallingBlock {
    public EnchantedSkull(EntityType<? extends ColoredProjectile> p_31950_, Level p_31951_) {
        super(p_31950_, p_31951_);
    }

    public EnchantedSkull(Level world, double v, double y, double v1, BlockState blockState) {
        super(world, v, y, v1, blockState);
    }

    public EnchantedSkull(Level world, BlockPos pos, BlockState blockState) {
        super(world, pos, blockState);
    }

    @Override
    public @NotNull EntityType<?> getType() {
        return ModEntities.ENCHANTED_HEAD_BLOCK.get();
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ServerLevel level, ItemStack pStack) {
        applyProfile(pStack);
        return this.spawnAtLocation(level, pStack, 0.0F);
    }

    private void applyProfile(ItemStack stack) {
        if (blockData != null && blockData.get("profile") != null) {
            ResolvableProfile.CODEC
                    .parse(NbtOps.INSTANCE, blockData.get("profile"))
                    .resultOrPartial().ifPresent(profile -> stack.set(DataComponents.PROFILE, profile));
        }
    }

    public ItemStack getStack() {
        Item item = getBlockState().getBlock().asItem();
        ItemStack stack = item.getDefaultInstance();
        applyProfile(stack);
        return stack;
    }
}
