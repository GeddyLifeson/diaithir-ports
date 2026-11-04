package io.github.tr100000.researcher.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.tr100000.researcher.PlayerResearchTracker;
import io.github.tr100000.researcher.ResearchManager;
import io.github.tr100000.researcher.api.ServerResearchTrackerGetter;
import io.github.tr100000.researcher.config.ResearcherConfigs;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements ServerResearchTrackerGetter {
    @Shadow @Final private MinecraftServer server;

    @Shadow @Final private List<ServerPlayer> players;
    @Unique
    private final Map<UUID, PlayerResearchTracker> researchTrackers = new Object2ObjectOpenHashMap<>();

    @Inject(method = "save", at = @At("TAIL"))
    private void save(ServerPlayer player, CallbackInfo ci) {
        PlayerResearchTracker researchTracker = researchTrackers.get(player.getUUID());
        if (researchTracker != null) {
            researchTracker.save(server.researcher$getServerManager());
        }
    }

    @Inject(method = "remove", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void remove(ServerPlayer player, CallbackInfo ci, @Local(name = "uuid") UUID uuid) {
        researchTrackers.remove(uuid);
        players.forEach(otherPlayer -> otherPlayer.researcher$getPlayerTracker().stopSyncWith(player.researcher$getPlayerTracker()));
    }

    @Inject(method = "reloadResources", at = @At("HEAD"))
    private void reloadResources(CallbackInfo ci) {
        researchTrackers.forEach((_, playerResearchTracker) -> playerResearchTracker.reload(server.researcher$getServerManager()));
    }

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void placeNewPlayer(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        playersToSyncWith(player)
                .map(ServerPlayer::researcher$getPlayerTracker)
                .forEach(player.researcher$getPlayerTracker()::syncWith);
    }

    @Unique
    private Stream<ServerPlayer> playersToSyncWith(ServerPlayer player) {
        return switch (ResearcherConfigs.server.researchSyncMode.get()) {
            case GLOBAL -> players.stream();
            case TEAM -> players.stream().filter(player::isAlliedTo);
            default -> Stream.empty(); // Don't sync with anyone by default
        };
    }

    @Override @Unique
    public PlayerResearchTracker researcher$getPlayerTracker(ServerPlayer player) {
        UUID uuid = player.getUUID();
        PlayerResearchTracker researchTracker = researchTrackers.computeIfAbsent(uuid, uuid2 -> createResearchTracker(player, uuid2));
        researchTracker.setOwner(player);
        return researchTracker;
    }

    @Unique
    private PlayerResearchTracker createResearchTracker(ServerPlayer player, UUID uuid) {
        Path worldDirPath = server.getWorldPath(ResearchManager.WORLD_SAVE_PATH);
        Path researchPath = worldDirPath.resolve(uuid + ".json");
        return new PlayerResearchTracker(player, server.researcher$getServerManager(), (PlayerList)(Object) this, researchPath);
    }
}
