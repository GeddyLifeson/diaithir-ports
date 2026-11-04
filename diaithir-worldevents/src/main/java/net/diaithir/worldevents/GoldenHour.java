package net.diaithir.worldevents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.Stream;

/** Ore drops are doubled for five minutes. */
final class GoldenHour implements WorldEvent {
    static final GoldenHour INSTANCE = new GoldenHour();
    private static final int DURATION = 6000;
    private static final int WARNING = 1200;
    private static final List<TagKey<Block>> ORE_TAGS = Stream.of("coal_ores", "copper_ores", "iron_ores", "gold_ores",
            "redstone_ores", "lapis_ores", "diamond_ores", "emerald_ores")
            .map(n -> TagKey.create(Registries.BLOCK, Identifier.withDefaultNamespace(n))).toList();
    private static int remaining;

    static boolean active() {
        return remaining > 0;
    }

    static int remaining() {
        return remaining;
    }

    static void reset() {
        remaining = 0;
    }

    static boolean isOre(BlockState state) {
        for (TagKey<Block> t : ORE_TAGS) if (state.is(t)) return true;
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().equals("nether_quartz_ore");
    }

    static void tick(MinecraftServer server) {
        if (remaining <= 0) return;
        remaining--;
        if (remaining == WARNING) WorldEvents.announce(server, "The Golden Hour ends in 1 minute.", ChatFormatting.YELLOW);
        if (remaining == 0) WorldEvents.announce(server, "The Golden Hour has ended. Back to normal mining.", ChatFormatting.GRAY);
    }

    @Override public String id() { return "golden_hour"; }
    @Override public String title() { return "Golden Hour"; }
    @Override public int weight() { return 25; }
    @Override public boolean possible(ServerLevel level) { return !active(); }

    @Override
    public String trigger(ServerLevel level, List<ServerPlayer> players) {
        remaining = DURATION;
        WorldEvents.announce(level.getServer(), "A Golden Hour has begun. All ore drops are doubled for 5 minutes.", ChatFormatting.GOLD);
        return "Golden Hour started.";
    }
}
