package net.diaithir.druidry.ars.api.item;

import net.diaithir.druidry.ars.common.items.ModItem;

public abstract class ArsNouveauCurio extends ModItem {

    public ArsNouveauCurio() {
        this(new Properties().stacksTo(1));
    }


    public ArsNouveauCurio(Properties properties) {
        super(properties);
    }

}
