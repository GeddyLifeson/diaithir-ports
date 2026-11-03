package rearth.belts.foundry;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class FoundryTiers {
    
    public static final int MIN_TIER = 1;
    public static final int MAX_TIER = 4;
    
    private static final float[] ITEMS_PER_SECOND = {7.5f, 15f, 22.5f, 30f};
    private static final int[] MAX_TUNNEL_DISTANCE = {4, 6, 8, 10};
    
    private FoundryTiers() {
    }
    
    public static int clamp(int tier) {
        return Math.clamp(tier, MIN_TIER, MAX_TIER);
    }
    
    public static float itemsPerSecond(int tier) {
        return ITEMS_PER_SECOND[clamp(tier) - 1];
    }
    
    public static int maxTunnelDistance(int tier) {
        return MAX_TUNNEL_DISTANCE[clamp(tier) - 1];
    }
    
    public static boolean isUpgradeItem(ItemStack stack) {
        return stack.is(Items.REDSTONE);
    }
    
    public static int upgrade(int tier, Player player, ItemStack stack) {
        if (tier >= MAX_TIER) {
            player.sendSystemMessage(Component.translatable("message.belts.foundry_tier_max"));
            return tier;
        }
        
        if (!player.isCreative())
            stack.shrink(1);
        
        var upgraded = tier + 1;
        player.sendSystemMessage(Component.translatable("message.belts.foundry_tier", upgraded));
        return upgraded;
    }
}
