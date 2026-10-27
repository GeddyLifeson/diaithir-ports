package com.github.ysbbbbbb.kaleidoscopetavern.network.proxy;

import com.github.ysbbbbbb.kaleidoscopetavern.blockentity.deco.TextBlockEntity;
import com.github.ysbbbbbb.kaleidoscopetavern.client.gui.block.TextScreen;
import com.github.ysbbbbbb.kaleidoscopetavern.network.message.TextOpenS2CMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TextOpenS2CProxy {
    public static void onHandle(TextOpenS2CMessage message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Player player = mc.player;
        if (player == null) {
            return;
        }
        if (!mc.level.isLoaded(message.pos())) {
            return;
        }
        Level level = mc.level;
        BlockPos pos = message.pos();
        if (level.getBlockEntity(pos) instanceof TextBlockEntity textBlock) {
            if (textBlock.isWaxed()) {
                return;
            }
            if (textBlock.playerIsTooFarAwayToEdit(player.getUUID())) {
                return;
            }
            mc.setScreen(new TextScreen(textBlock));
        }
    }
}
