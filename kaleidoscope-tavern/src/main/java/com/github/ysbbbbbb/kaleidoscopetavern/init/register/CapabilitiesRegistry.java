package com.github.ysbbbbbb.kaleidoscopetavern.init.register;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.fluid.BucketResourceHandler;

@EventBusSubscriber(modid = KaleidoscopeTavern.MOD_ID)
public class CapabilitiesRegistry {
    @SubscribeEvent
    public static void registerGenericItemHandlers(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlocks.PRESSING_TUB_BE.get(), (b, v) -> {
            // 仅能从侧面输入
            if (v == null || v.getAxis() == Direction.Axis.Y) {
                return null;
            }
            return b.getItems();
        });
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, ModBlocks.PRESSING_TUB_BE.get(), (b, v) -> b.getFluid());

        event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new BucketResourceHandler(access),
                ModItems.GRAPE_BUCKET,
                ModItems.ICE_GRAPE_BUCKET,
                ModItems.GOLD_GRAPE_BUCKET,
                ModItems.GREEN_GRAPE_BUCKET,
                ModItems.GLOW_BERRIES_BUCKET,
                ModItems.SWEET_BERRIES_BUCKET
        );
    }
}