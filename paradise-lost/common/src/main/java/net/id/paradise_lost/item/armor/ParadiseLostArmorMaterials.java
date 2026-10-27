package net.id.paradise_lost.item.armor;

import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.util.ParadiseLostSoundEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import java.util.Map;

@SuppressWarnings("unused")
public class ParadiseLostArmorMaterials {
    private static ArmorMaterial make(String id, String repairTag, int durability, int boots, int leggings, int chest, int helmet, int body, int ench,
                                      net.minecraft.core.Holder<net.minecraft.sounds.SoundEvent> sound, float toughness, float kb) {
        return new ArmorMaterial(durability,
                Map.of(ArmorType.BOOTS, boots, ArmorType.LEGGINGS, leggings, ArmorType.CHESTPLATE, chest, ArmorType.HELMET, helmet, ArmorType.BODY, body),
                ench, sound, toughness, kb,
                TagKey.create(Registries.ITEM, ModConstants.locate("repairs_" + repairTag + "_armor")),
                ResourceKey.create(EquipmentAssets.ROOT_ID, ModConstants.locate(id)));
    }

    public static final ArmorMaterial OLVITE = make("olvite", "olvite", 15, 2, 4, 6, 2, 4, 9, ParadiseLostSoundEvents.ITEM_ARMOR_EQUIP_OLVITE, 0.0F, 0.0F);
    public static final ArmorMaterial ORNATE_OLVITE = make("ornate_olvite", "olvite", 15, 2, 4, 6, 2, 4, 9, ParadiseLostSoundEvents.ITEM_ARMOR_EQUIP_OLVITE, 0.0F, 0.0F);
    public static final ArmorMaterial GLAZED_GOLD = make("glazed_gold", "glazed_gold", 21, 1, 3, 5, 2, 3, 25, ParadiseLostSoundEvents.ITEM_ARMOR_EQUIP_GLAZED_GOLD, 0.0F, 0.0F);
    public static final ArmorMaterial SURTRUM = make("surtrum", "surtrum", 27, 2, 5, 6, 3, 5, 15, ParadiseLostSoundEvents.ITEM_ARMOR_EQUIP_SURTRUM, 0.0F, 0.0F);
    public static final ArmorMaterial RELIC = make("relic", "relic", 15, 1, 4, 5, 2, 4, 12, ParadiseLostSoundEvents.ITEM_ARMOR_EQUIP_RELIC, 0.5F, 0.0F);
    public static final ArmorMaterial FLOATY = make("floaty", "floaty", 66, 1, 2, 3, 1, 2, 10, ParadiseLostSoundEvents.ITEM_ARMOR_EQUIP_FLAXWEAVE, 0.0F, 0.0F);
}
