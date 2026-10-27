package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Map;

/** Armour set data in the 26.1 form: a vanilla ArmorMaterial record plus an equipment asset key. */
public class ACArmorMaterial {
    private final String name;
    private final ArmorMaterial material;
    public final float knockbackResistance;

    public ACArmorMaterial(String name, int durability, int[] damageReduction, int enchantability, Holder<SoundEvent> sound, float toughness) {
        this(name, durability, damageReduction, enchantability, sound, toughness, 0F);
    }

    @SuppressWarnings("unchecked")
    public ACArmorMaterial(String name, int durability, int[] damageReduction, int enchantability, Holder<SoundEvent> sound, float toughness, float knockbackResist) {
        this.name = name;
        this.knockbackResistance = knockbackResist;
        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        defense.put(ArmorType.BOOTS, damageReduction[3]);
        defense.put(ArmorType.LEGGINGS, damageReduction[2]);
        defense.put(ArmorType.CHESTPLATE, damageReduction[1]);
        defense.put(ArmorType.HELMET, damageReduction[0]);
        defense.put(ArmorType.BODY, damageReduction[1]);
        this.material = new ArmorMaterial(durability, defense, enchantability, sound, toughness, knockbackResist,
                TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "repairs_" + name + "_armor")),
                ResourceKey.create((ResourceKey) EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(AlexsCaves.MODID, name)));
    }

    public ArmorMaterial material() { return material; }

    public int getDefenseForType(ArmorType type) { return material.defense().getOrDefault(type, 0); }

    public int getDefenseForType(ArmorType type) { return material.defense().getOrDefault(type, 0); }

    public String getName() { return name; }

    public float getToughness() { return material.toughness(); }

    public float getKnockbackResistance() { return knockbackResistance; }


    public Holder<SoundEvent> getEquipSound() { return material.equipSound(); }
}
