package com.diaithir.addons.newage.content.electricity.wire;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import com.diaithir.addons.newage.CNAItems;
import com.diaithir.addons.newage.CreateNewAge;
import com.diaithir.addons.newage.config.CNAConfig;

public enum WireType {
    COPPER(1024, CNAItems.COPPER_WIRE::toStack),
    OVERCHARGED_IRON(2048, CNAItems.OVERCHARGED_IRON_WIRE::toStack),
    OVERCHARGED_GOLD(4096, CNAItems.OVERCHARGED_GOLDEN_WIRE::toStack),
    OVERCHARGED_DIAMOND(8192, CNAItems.OVERCHARGED_DIAMOND_WIRE::toStack);

    private final long conductivity;
    private final IRegistrateIsAFuckingShitNeverUseIt dropProvider;

    WireType(int conductivity, IRegistrateIsAFuckingShitNeverUseIt dropProvider) {
        this.conductivity = conductivity;
        this.dropProvider = dropProvider;
    }

    public Identifier getTextureLocation() {
        return Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, "textures/wire/" + name().toLowerCase() + ".png");
    }

    public long getConductivity() {
        return (long) (conductivity * CNAConfig.getServer().conductivityMultiplier.get());
    }

    public ItemStack getDroppedItem() {
        return dropProvider.getDroppedItem();
    }
}
