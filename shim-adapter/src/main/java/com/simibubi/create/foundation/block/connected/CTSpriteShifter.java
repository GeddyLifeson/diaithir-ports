package com.simibubi.create.foundation.block.connected;

import com.zurrtum.create.client.foundation.block.connected.CTType;
import net.minecraft.resources.Identifier;

/** Server-safe CT sprite-shift factory returning inert entries. */
public class CTSpriteShifter {
    public static CTSpriteShiftEntry getCT(CTType type, Identifier blockTexture, Identifier connectedTexture) {
        CTSpriteShiftEntry e = new CTSpriteShiftEntry(type);
        e.set(blockTexture, connectedTexture);
        return e;
    }
}
