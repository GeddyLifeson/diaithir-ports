package com.gregtechceu.gtceu.compat;

/** Small helpers bridging 1.21.1 style calls to the 26.1 API. */
public final class GtCompat {
    private GtCompat() {}

    public static net.minecraft.world.item.crafting.RecipeManager recipeManager(net.minecraft.world.level.Level level) {
        if (level instanceof net.minecraft.server.level.ServerLevel sl) return sl.recipeAccess();
        throw new IllegalStateException("recipe manager is only available on the server in 26.1");
    }

    public static void kill(net.minecraft.world.entity.Entity entity) {
        if (entity.level() instanceof net.minecraft.server.level.ServerLevel sl) entity.kill(sl);
    }

    public static net.minecraft.world.entity.item.ItemEntity spawnAtLocation(net.minecraft.world.entity.Entity entity, net.minecraft.world.item.ItemStack stack, float offset) {
        return entity.level() instanceof net.minecraft.server.level.ServerLevel sl ? entity.spawnAtLocation(sl, stack, offset) : null;
    }

    public static void displayMessage(net.minecraft.world.entity.player.Player player, net.minecraft.network.chat.Component message, boolean actionBar) {
        if (actionBar) player.sendOverlayMessage(message); else player.sendSystemMessage(message);
    }

    public static net.minecraft.world.item.ItemStack craftingRemainder(net.minecraft.world.item.ItemStack stack) {
        var t = stack.getCraftingRemainder();
        return t == null ? net.minecraft.world.item.ItemStack.EMPTY : t.create();
    }
}
