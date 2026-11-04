package net.diaithir.druidry.ars.api.registry;

import net.diaithir.druidry.ars.api.imbuement_chamber.IImbuementRecipe;
import net.diaithir.druidry.ars.common.block.tile.ImbuementTile;

public class ImbuementRecipeRegistry extends MultiRecipeRegistry<ImbuementTile, IImbuementRecipe> {
    public static ImbuementRecipeRegistry INSTANCE = new ImbuementRecipeRegistry();
}
