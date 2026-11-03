package net.diaithir.druidry.ars.common.spell.validation;

import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.SpellValidationError;

import java.util.List;

/**
 * Spell validator that asserts that the spell is non-empty.
 */
public class NonEmptySpellValidator extends AbstractSpellValidator {
    @Override
    protected void validateImpl(List<AbstractSpellPart> spellRecipe, List<SpellValidationError> validationErrors) {
        if (spellRecipe == null || spellRecipe.isEmpty()) {
            validationErrors.add(new NonEmptySpellValidationError());
        }
    }

    private static class NonEmptySpellValidationError extends BaseSpellValidationError {
        public NonEmptySpellValidationError() {
            super(-1, null, "non_empty_spell");
        }
    }
}
