/*
 * This file ("PacketHelperClient.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.network;

import net.minecraft.core.UUIDUtil;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import de.ellpeck.actuallyadditions.mod.data.PlayerData;
import de.ellpeck.actuallyadditions.mod.data.PlayerData.PlayerSave;
import de.ellpeck.actuallyadditions.mod.network.packet.ButtonToTilePacket;
import de.ellpeck.actuallyadditions.mod.network.packet.NumberToTilePacket;
import de.ellpeck.actuallyadditions.mod.network.packet.SyncPlayerPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PacketHelperClient {

    public static void sendButtonPacket(BlockEntity tile, int buttonId) {
        BlockPos pos = tile.getBlockPos();
        ClientPacketDistributor.sendToServer(new ButtonToTilePacket(tile.getLevel().dimension().identifier(),
                pos, Minecraft.getInstance().player.getId(), buttonId));
    }

    public static void sendPlayerDataToServer(boolean log, int type) {
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("Log", log);
        compound.putInt("Type", type);

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            compound.putString("World", player.level().dimension().identifier().toString());
            compound.store("UUID", UUIDUtil.CODEC, player.getUUID());

            PlayerSave data = PlayerData.getDataFromPlayer(player);

//            if (type == 0) {
//                compound.put("Bookmarks", data.saveBookmarks());
//            } else if (type == 1) {
//                compound.putBoolean("DidBookTutorial", data.didBookTutorial);
//            } else if (type == 2) {
//                compound.put("Trials", data.saveTrials());
//
//                int total = 0;
//                for (IBookletChapter chapter : ActuallyAdditionsAPI.entryTrials.getAllChapters()) {
//                    //if (chapter instanceof BookletChapterTrials) {
//                    //    total++;
//                    //}
//                }
//
//                if (data.completedTrials.size() >= total) {
//                    compound.putBoolean("Achievement", true);
//                }
//            }

            ClientPacketDistributor.sendToServer(new SyncPlayerPacket(compound));
        }
    }


    public static void sendNumberPacket(BlockEntity tile, double number, int id) {
        ClientPacketDistributor.sendToServer(new NumberToTilePacket(
                tile.getLevel().dimension().identifier(), tile.getBlockPos(), Minecraft.getInstance().player.getId(), number, id
        ));
    }
}
