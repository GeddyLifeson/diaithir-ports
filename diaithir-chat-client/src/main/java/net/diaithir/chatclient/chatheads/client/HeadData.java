// Ported from Chat Heads (MPL-2.0) https://github.com/dzwdz/chat_heads
// Modifications: EMPTY made final; otherwise unchanged.
package net.diaithir.chatclient.chatheads.client;

import net.minecraft.client.multiplayer.PlayerInfo;

// playerInfo gives us the texture, codePointIndex the position inside the message
public record HeadData(PlayerInfo playerInfo, int codePointIndex) {
    public static final HeadData EMPTY = new HeadData(null, -1);

    public HeadData {
        if (playerInfo == null && codePointIndex >= 0) {
            throw new AssertionError();
        }
    }

    public static HeadData of(PlayerInfo playerInfo) {
        if (playerInfo == null) {
            return EMPTY;
        }
        return new HeadData(playerInfo, -1);
    }

    public boolean hasHeadPosition() {
        return codePointIndex >= 0;
    }
}
