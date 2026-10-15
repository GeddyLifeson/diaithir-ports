/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.proxy.client;

import com.buuz135.industrial.block.transportstorage.tile.ConveyorTile;
import com.buuz135.industrial.item.MobImprisonmentToolItem;
import com.buuz135.industrial.item.infinity.InfinityTier;
import com.buuz135.industrial.item.infinity.ItemInfinity;
import com.buuz135.industrial.module.ModuleTool;
import com.buuz135.industrial.utils.Caps;
import com.buuz135.industrial.utils.FluidUtils;
import com.buuz135.industrial.utils.IFAttachments;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The tints and item model properties that the 1.21 item colour handlers and ItemProperties used to supply;
 * the items/*.json definitions pick them by the ids registered in {@link ClientProxy}.
 */
public class IFItemModelParts {

    public record ConveyorTint() implements BlockTintSource {

        @Override
        public int color(BlockState state) {
            return -1;
        }

        @Override
        public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return level.getBlockEntity(pos) instanceof ConveyorTile conveyor ? ARGB.opaque(conveyor.getColor()) : -1;
        }
    }

    public record MobImprisonmentBlacklistedTint() implements ItemTintSource {

        public static final MapCodec<MobImprisonmentBlacklistedTint> MAP_CODEC = MapCodec.unit(new MobImprisonmentBlacklistedTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            if (stack.has(IFAttachments.MOB_IMPRISONMENT_TOOL)) {
                Identifier id = Identifier.tryParse(stack.get(IFAttachments.MOB_IMPRISONMENT_TOOL).getStringOr("entity", ""));
                EntityType<?> type = id == null ? null : BuiltInRegistries.ENTITY_TYPE.get(id).map(Holder::value).orElse(null);
                if (type != null && ((MobImprisonmentToolItem) ModuleTool.MOB_IMPRISONMENT_TOOL.get()).isBlacklisted(type)) return 0xFFDB201A;
            }
            return 0xFF636363;
        }

        @Override
        public MapCodec<MobImprisonmentBlacklistedTint> type() {
            return MAP_CODEC;
        }
    }

    public record InfinityTierTint() implements ItemTintSource {

        public static final MapCodec<InfinityTierTint> MAP_CODEC = MapCodec.unit(new InfinityTierTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            return ARGB.opaque(InfinityTier.getTierBraquet(ItemInfinity.getPowerFromStack(stack)).getLeft().getTextureColor());
        }

        @Override
        public MapCodec<InfinityTierTint> type() {
            return MAP_CODEC;
        }
    }

    public record FluidContentTint() implements ItemTintSource {

        public static final MapCodec<FluidContentTint> MAP_CODEC = MapCodec.unit(new FluidContentTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
            var fluidHandlerItem = Caps.fluidItem(stack);
            if (fluidHandlerItem != null && fluidHandlerItem.getFluidInTank(0).getAmount() > 0) {
                int color = FluidUtils.getFluidColor(fluidHandlerItem.getFluidInTank(0));
                if (color != -1) return ARGB.opaque(color);
            }
            return -1;
        }

        @Override
        public MapCodec<FluidContentTint> type() {
            return MAP_CODEC;
        }
    }

    public record OnCooldown() implements ConditionalItemModelProperty {

        public static final MapCodec<OnCooldown> MAP_CODEC = MapCodec.unit(new OnCooldown());

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
            return owner instanceof Player player && player.getCooldowns().isOnCooldown(stack);
        }

        @Override
        public MapCodec<OnCooldown> type() {
            return MAP_CODEC;
        }
    }
}
