package net.diaithir.druidry.ars.client.particle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Port stub: a block position with a highlight colour. */
public class ColorPos {
    public BlockPos pos;
    public ParticleColor.IntWrapper color;

    public ColorPos(BlockPos pos, ParticleColor.IntWrapper color) {
        this.pos = pos;
        this.color = color;
    }

    public static ColorPos centered(BlockPos pos, ParticleColor.IntWrapper color) {
        return new ColorPos(pos, color);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("pos", pos.asLong());
        tag.put("color", color.serialize());
        return tag;
    }

    public static ColorPos fromTag(CompoundTag tag) {
        return new ColorPos(BlockPos.of(tag.getLongOr("pos", 0L)), ParticleColor.IntWrapper.deserialize(tag.getCompoundOrEmpty("color")));
    }
}
