package net.diaithir.worldevents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.Optional;

/** A wandering trader arrives near a player for ten minutes with rare goods for emeralds. */
final class WanderingCaravan implements WorldEvent {
    @Override public String id() { return "caravan"; }
    @Override public String title() { return "Wandering Caravan"; }
    @Override public int weight() { return 20; }
    @Override public boolean possible(ServerLevel level) { return !level.players().isEmpty(); }

    @Override
    public String trigger(ServerLevel level, List<ServerPlayer> players) {
        if (players.isEmpty()) return "No player in the overworld.";
        ServerPlayer target = players.get(WorldEvents.RANDOM.nextInt(players.size()));
        BlockPos p = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                target.blockPosition().offset(WorldEvents.RANDOM.nextInt(-20, 21), 0, WorldEvents.RANDOM.nextInt(-20, 21)));
        WanderingTrader trader = EntityType.WANDERING_TRADER.create(level, EntitySpawnReason.EVENT);
        if (trader == null) return "Could not create a trader.";
        trader.snapTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0F, 0F);
        trader.setDespawnDelay(12000);
        trader.addTag("diaithir_caravan");
        goods(trader);
        level.addFreshEntity(trader);
        WorldEvents.announce(level.getServer(), "A wandering caravan has arrived near " + target.getName().getString() + ".", ChatFormatting.AQUA);
        return "caravan at " + p.toShortString();
    }

    private static void goods(WanderingTrader t) {
        t.getOffers().add(offer(64, Items.NETHERITE_INGOT, 1));
        t.getOffers().add(offer(48, Items.ELYTRA, 1));
        t.getOffers().add(offer(32, Items.NETHER_STAR, 1));
        t.getOffers().add(offer(20, Items.ENCHANTED_GOLDEN_APPLE, 1));
        t.getOffers().add(offer(25, Items.HEART_OF_THE_SEA, 1));
        t.getOffers().add(offer(10, Items.TOTEM_OF_UNDYING, 1));
        t.getOffers().add(offer(8, Items.SHULKER_SHELL, 2));
    }

    private static MerchantOffer offer(int emeralds, Item item, int count) {
        return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), Optional.empty(), new ItemStack(item, count), 1, 10, 0.05F);
    }
}
