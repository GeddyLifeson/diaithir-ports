package net.diaithir.druidry.ars.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;

import java.util.UUID;

/** Client-only skin lookup for EntityDummy (kept out of the entity so the server never loads client classes). */
public final class DummyClientSkin {
    private DummyClientSkin() {
    }

    private static PlayerInfo info(UUID owner) {
        if (owner == null || Minecraft.getInstance().getConnection() == null) {
            return null;
        }
        return Minecraft.getInstance().getConnection().getPlayerInfo(owner);
    }

    public static Identifier texture(UUID owner) {
        PlayerInfo info = info(owner);
        return info == null ? DefaultPlayerSkin.getDefaultTexture() : info.getSkin().body().texturePath();
    }

    public static boolean isSlim(UUID owner) {
        PlayerInfo info = info(owner);
        return info != null && info.getSkin().model() == PlayerModelType.SLIM;
    }
}
