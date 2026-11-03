package net.diaithir.druidry.ars.common.util;

import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketNoSpamChatMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class PortUtil {
    public static void sendMessage(Entity playerEntity, Component component) {
        if (playerEntity == null)
            return;
        playerEntity.sendSystemMessage(component);
    }

    public static void sendMessageNoSpam(Entity playerEntity, Component component) {
        if (playerEntity instanceof ServerPlayer serverPlayer) {
            Networking.sendToPlayerClient(new PacketNoSpamChatMessage(component, 0, false), serverPlayer);
        }
    }

    public static void sendMessageCenterScreen(Entity playerEntity, Component component) {
        if (playerEntity instanceof ServerPlayer serverPlayer) {
            Networking.sendToPlayerClient(new PacketNoSpamChatMessage(component, 0, true), serverPlayer);
        }
    }

    @Deprecated
    public static void sendMessage(Entity playerEntity, String message) {
        sendMessage(playerEntity, Component.literal(message));
    }

    public static net.minecraft.world.item.ItemStack remainder(net.minecraft.world.item.ItemStack stack) {
        var r = stack.getCraftingRemainder();
        return r == null ? net.minecraft.world.item.ItemStack.EMPTY : r.create();
    }

    public static boolean hasRemainder(net.minecraft.world.item.ItemStack stack) {
        return stack.getCraftingRemainder() != null;
    }

    /** Server-side recipe manager (Level#getRecipeManager was removed). */
    public static net.minecraft.world.item.crafting.RecipeManager recipeManager() {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        return server == null ? null : server.getRecipeManager();
    }

    public static <I extends net.minecraft.world.item.crafting.RecipeInput, T extends net.minecraft.world.item.crafting.Recipe<I>> java.util.List<net.minecraft.world.item.crafting.RecipeHolder<T>> recipes(net.minecraft.world.item.crafting.RecipeType<T> type) {
        var mgr = recipeManager();
        if (mgr == null) return java.util.List.of();
        return new java.util.ArrayList<>(mgr.recipeMap().byType(type));
    }

    public static java.util.List<ItemStack> armorSlots(net.minecraft.world.entity.LivingEntity entity) {
        java.util.List<ItemStack> list = new java.util.ArrayList<>();
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            if (slot.getType() == net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR) {
                list.add(entity.getItemBySlot(slot));
            }
        }
        return list;
    }
}
