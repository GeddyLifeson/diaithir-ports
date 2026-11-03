package net.diaithir.druidry.ars.common.spell.validation;

import net.diaithir.druidry.ars.api.spell.AbstractSpellPart;
import net.diaithir.druidry.ars.api.spell.ISpellValidator;
import net.diaithir.druidry.ars.api.spell.SpellValidationError;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Combined spell validator that runs the validations of all its children.
 */
public class CombinedSpellValidator implements ISpellValidator {
    public final List<ISpellValidator> validators;

    /**
     * Create a combined spell validator from the given list of validators.
     */
    public CombinedSpellValidator(List<ISpellValidator> validators) {
        this.validators = validators;
    }

    /**
     * Create a combined spell validator from the given list of validators.
     */
    public CombinedSpellValidator(ISpellValidator... validators) {
        this.validators = Arrays.asList(validators);
    }

    @Override
    public List<SpellValidationError> validate(List<AbstractSpellPart> spellRecipe) {
        return validators.stream()
                .flatMap(v -> v.validate(spellRecipe).stream())
                .collect(Collectors.toList());
    }
}
