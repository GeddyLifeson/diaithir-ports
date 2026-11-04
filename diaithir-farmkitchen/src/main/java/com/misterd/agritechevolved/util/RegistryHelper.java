package com.misterd.agritechevolved.util;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;

public class RegistryHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static String getItemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static String getItemId(ItemStack stack) {
        return getItemId(stack.getItem());
    }

    public static String getBlockId(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    public static Item getItem(String id) {
        try {
            Identifier identifier = Identifier.parse(id);
            return BuiltInRegistries.ITEM.get(identifier)
                    .map(ref -> ref.value())
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.error("Invalid item ID in config: {}", id, e);
            return null;
        }
    }

    public static Block getBlock(String id) {
        try {
            Identifier identifier = Identifier.parse(id);
            return BuiltInRegistries.BLOCK.get(identifier)
                    .map(ref -> ref.value())
                    .orElse(null);
        } catch (Exception e) {
            LOGGER.error("Invalid block ID in config: {}", id, e);
            return null;
        }
    }
}