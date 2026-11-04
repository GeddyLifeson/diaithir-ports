package net.diaithir.druidry.ars.common.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class ManaData implements ValueIOSerializable {

    private double mana;

    private int maxMana;

    private int glyphBonus;

    private int bookTier;

    private float reservedMana;


    public double getMana() {
        return mana;
    }

    public void setMana(double mana) {
        this.mana = mana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public int getGlyphBonus() {
        return glyphBonus;
    }

    public void setGlyphBonus(int glyphBonus) {
        this.glyphBonus = glyphBonus;
    }

    public int getBookTier() {
        return bookTier;
    }

    public void setBookTier(int bookTier) {
        this.bookTier = bookTier;
    }

    public float getReservedMana() {
        return reservedMana;
    }

    public void setReservedMana(float reservedMana) {
        this.reservedMana = reservedMana;
    }

    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("current", getMana());
        tag.putInt("max", getMaxMana());
        tag.putInt("glyph", getGlyphBonus());
        tag.putInt("book_tier", getBookTier());
        tag.putFloat("reserved", getReservedMana());
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        setMaxMana(tag.getIntOr("max", 0));
        setMana(tag.getDoubleOr("current", 0));
        setBookTier(tag.getIntOr("book_tier", 0));
        setGlyphBonus(tag.getIntOr("glyph", 0));
        setReservedMana(tag.getFloatOr("reserved", 0f));
    }

    @Override
    public void serialize(net.minecraft.world.level.storage.ValueOutput output) {
        output.store("data", CompoundTag.CODEC, serializeNBT(null));
    }

    @Override
    public void deserialize(net.minecraft.world.level.storage.ValueInput input) {
        input.read("data", CompoundTag.CODEC).ifPresent(t -> deserializeNBT(null, t));
    }
}
