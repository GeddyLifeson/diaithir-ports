package net.diaithir.butchery.nutrition.nutrients;

import net.minecraft.client.resources.language.I18n;

public class Nutrient {
    public String name;

    public Nutrient(String name)
    {
        this.name=name;
    }

    public String getLocalizedName()
    {
        return I18n.get("nutritionalbalance.nutrient."+this.name);
    }

}
