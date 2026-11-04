package net.diaithir.druidry.ars.common.spell.validation;

import net.diaithir.druidry.ars.api.registry.GlyphRegistry;
import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.SpellValidationError;
import net.diaithir.druidry.ars.common.capability.IPlayerCap;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GlyphKnownValidator extends ScanningSpellValidator<Unit> {

    @Nullable IPlayerCap playerDataCap;

    public GlyphKnownValidator(@Nullable IPlayerCap player) {
        this.playerDataCap = player;
    }


    @Override
    protected Unit initContext() {
        return Unit.INSTANCE;
    }

    @Override
    protected void digestSpellPart(Unit context, int position, AbstractSpellPart spellPart, List<SpellValidationError> validationErrors) {
        if (playerDataCap != null) {
            if (!playerDataCap.knowsGlyph(spellPart) && !GlyphRegistry.getDefaultStartingSpells().contains(spellPart)) {
                validationErrors.add(new GlyphUnknownValidationError(position, spellPart, "glyph_not_known"));
            }
        }
    }


    static class GlyphUnknownValidationError extends BaseSpellValidationError {
        public GlyphUnknownValidationError(int position, AbstractSpellPart spellPart, String localizationCode) {
            super(position, spellPart, localizationCode, spellPart);
        }
    }

}
