package net.diaithir.furnishings;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/** Reached only from the logical client, so the dedicated server never loads the screen class. */
final class ClientHooks {
    private ClientHooks() {
    }

    static void openEditor(BlockPos pos, int slot) {
        Minecraft.getInstance().setScreen(new SignpostEditScreen(pos, slot));
    }
}
