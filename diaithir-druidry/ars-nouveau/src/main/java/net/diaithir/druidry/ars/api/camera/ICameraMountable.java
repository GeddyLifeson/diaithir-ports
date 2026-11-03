package net.diaithir.druidry.ars.api.camera;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.common.entity.ScryerCamera;
import net.diaithir.druidry.ars.common.network.Networking;
import net.diaithir.druidry.ars.common.network.PacketSetCameraView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ICameraMountable {

    default void mountCamera(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            ServerPlayer serverPlayer = (ServerPlayer) player;
            SectionPos chunkPos = SectionPos.of(pos);
            int viewDistance = Mth.clamp(serverPlayer.requestedViewDistance(), 2, serverPlayer.server.getPlayerList().getViewDistance());

            Entity var10 = serverPlayer.getCamera();
            ScryerCamera dummyEntity;
            if (var10 instanceof ScryerCamera cam) {
                dummyEntity = new ScryerCamera(level, pos, cam);
            } else {
                dummyEntity = new ScryerCamera(level, pos);
            }


            level.addFreshEntity(dummyEntity);
            dummyEntity.setChunkLoadingDistance(viewDistance);
            for (int x = chunkPos.getX() - viewDistance; x <= chunkPos.getX() + viewDistance; x++) {
                for (int z = chunkPos.getZ() - viewDistance; z <= chunkPos.getZ() + viewDistance; z++) {
                    ArsNouveau.ticketController.forceChunk(serverLevel, dummyEntity, x, z, true, false);
                }
            }


            serverPlayer.camera = dummyEntity;
            Networking.sendToPlayerClient(new PacketSetCameraView(dummyEntity), serverPlayer);
            startViewing();
        }
    }

    void startViewing();

    void stopViewing();
}
