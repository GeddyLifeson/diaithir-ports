package net.diaithir.druidry.ars.api;

import net.diaithir.druidry.ars.api.spell.ISpellValidator;
import net.diaithir.druidry.ars.common.spell.validation.StandardSpellValidator;

/** Port stub of the Ars Nouveau API entry point (only what the ported code needs). */
public class ArsNouveauAPI {
    private static final ArsNouveauAPI INSTANCE = new ArsNouveauAPI();

    public static ArsNouveauAPI getInstance() {
        return INSTANCE;
    }

    public ISpellValidator getSpellCastingSpellValidator() {
        return new StandardSpellValidator(true).combinedValidator;
    }

    public ISpellValidator getSpellCraftingSpellValidator() {
        return new StandardSpellValidator(false).combinedValidator;
    }

    private final java.util.concurrent.ConcurrentHashMap<net.minecraft.resources.Identifier, net.diaithir.druidry.ars.api.scrying.IScryer> scryerMap = new java.util.concurrent.ConcurrentHashMap<>();

    public net.diaithir.druidry.ars.api.scrying.IScryer getScryer(net.minecraft.resources.Identifier id) {
        return id == null ? null : this.scryerMap.get(id);
    }

    public boolean registerScryer(net.diaithir.druidry.ars.api.scrying.IScryer scryer) {
        this.scryerMap.put(scryer.getRegistryName(), scryer);
        return true;
    }
}
