package mcjty.rftoolsbuilder.modules.builder.items;

import mcjty.lib.setup.RegistryIds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class SuperHarvestingTool extends Item {

    public SuperHarvestingTool() {
        super(RegistryIds.withId(new Item.Properties()).pickaxe(ToolMaterial.NETHERITE, 1.0f, -2.8f));
    }
}
