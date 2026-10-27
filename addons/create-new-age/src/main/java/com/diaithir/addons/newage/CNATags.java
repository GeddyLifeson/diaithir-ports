package com.diaithir.addons.newage;

import com.zurrtum.create.AllItems;


import com.zurrtum.create.client.foundation.utility.CreateLang;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CNATags {
    public enum Block {
        MAGNET(true),
        STOPS_RADIATION(false);

        public final boolean hasItemTag;
        public final TagKey<net.minecraft.world.level.block.Block> blockTag;
        public final TagKey<net.minecraft.world.item.Item> itemTag;
        
        Block(boolean hasItemTag) {
            Identifier location = Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, CreateLang.asId(name()));
            this.hasItemTag = hasItemTag;
            this.blockTag = TagKey.create(Registries.BLOCK, location);
            this.itemTag = hasItemTag ? TagKey.create(Registries.ITEM, location) : null;
        }
        
        public static void load() {  }
    }
    
    public enum Item {
        NUCLEAR_FUEL("nuclear/fuel"),
        HAZMAT_SUIT("hazmat_suit");
        
        public final TagKey<net.minecraft.world.item.Item> tag;
        
        Item(String id) {
            tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, id));
        }
        
        public static void load() {  }
    }
    
    
    public static TagKey<net.minecraft.world.item.Item> createNuclearEnergyTag(int energy) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, "nuclear/energy_" + energy));
    }

    public static TagKey<net.minecraft.world.level.block.Block> createMagneticForgeTag(int force) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, "magnet/force_" + force));
    }
    
    static void load() {
        Block.load();
        Item.load();
    }
}
