package net.id.paradise_lost.item.tool;

import net.id.paradise_lost.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

@SuppressWarnings("unused")
public class ParadiseLostToolMaterials {
    private static TagKey<Item> repairs(String name) {
        return TagKey.create(Registries.ITEM, ModConstants.locate(name));
    }

    public static final ToolMaterial OLVITE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 250, 4.5f, 2f, 14, repairs("repairs_olvite_tool"));
    public static final ToolMaterial SURTRUM = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 827, 7.0f, 3f, 16, repairs("repairs_surtrum_tool"));
    public static final ToolMaterial GLAZED_GOLD = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 131, 12f, 2f, 22, repairs("repairs_glazed_gold_tool"));
    public static final ToolMaterial SOUL_BLADE = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 636, 6.5f, 1.5f, 17, repairs("repairs_soul_blade"));
}
