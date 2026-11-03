package net.diaithir.druidry.ars.common.capability;

import net.diaithir.druidry.ars.api.registry.GlyphRegistry;
import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ANPlayerData implements ValueIOSerializable {
    public Set<AbstractSpellPart> glyphs = new HashSet<>();

    public Set<FamiliarData> familiars = new HashSet<>();

    public Identifier lastSummonedFamiliar;

    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        CompoundTag glyphsTag = new CompoundTag();
        List<AbstractSpellPart> glyphsList = glyphs.stream().toList();
        for (int i = 0; i < glyphsList.size(); i++) {
            glyphsTag.putString("glyph" + i, glyphsList.get(i).getRegistryName().toString());
        }
        glyphsTag.putInt("size", glyphsList.size());
        tag.put("glyphs", glyphsTag);

        CompoundTag familiarsTag = new CompoundTag();
        List<FamiliarData> familiarsList = familiars.stream().toList();
        for (int i = 0; i < familiarsList.size(); i++) {
            familiarsTag.put("familiar" + i, familiarsList.get(i).toTag());
        }
        familiarsTag.putInt("size", familiarsList.size());
        tag.put("familiars", familiarsTag);
        if (lastSummonedFamiliar != null) {
            tag.putString("lastSummonedFamiliar", lastSummonedFamiliar.toString());
        }
        return tag;
    }

    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        glyphs = new HashSet<>();
        familiars = new HashSet<>();

        CompoundTag glyphsTag = nbt.getCompoundOrEmpty("glyphs");
        for (int i = 0; i < glyphsTag.getIntOr("size", 0); i++) {
            Identifier id = Identifier.parse(glyphsTag.getStringOr("glyph" + i, ""));
            AbstractSpellPart part = GlyphRegistry.getSpellPart(id);
            if (part != null)
                glyphs.add(part);
        }

        CompoundTag familiarsTag = nbt.getCompoundOrEmpty("familiars");
        for (int i = 0; i < familiarsTag.getIntOr("size", 0); i++) {
            familiars.add(new FamiliarData(familiarsTag.getCompoundOrEmpty("familiar" + i)));
        }
        if (nbt.contains("lastSummonedFamiliar")) {
            lastSummonedFamiliar = Identifier.parse(nbt.getStringOr("lastSummonedFamiliar", ""));
        }
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
