package com.simibubi.create.foundation.block.connected;

import com.zurrtum.create.client.foundation.block.connected.CTType;
import net.minecraft.resources.Identifier;

/** Inert holder for connected-texture sprite shifts (CT rendering is not bridged yet; registration code must still run). */
public class CTSpriteShiftEntry {
    public final CTType type;
    public Identifier originalTexture, targetTexture;
    public CTSpriteShiftEntry(CTType type) { this.type = type; }
    public void set(Identifier original, Identifier target) { this.originalTexture = original; this.targetTexture = target; }
}
