package net.diaithir.druidry.ars.api.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IRadialProvider {

    /**
     * The key press this radial provider responds to
     *
     * @return the key code
     */
    @OnlyIn(Dist.CLIENT)
    default int forKey() {
        return com.mojang.blaze3d.platform.InputConstants.KEY_G;
    }

    /**
     * When the key is clicked to open the menu
     */
    @OnlyIn(Dist.CLIENT)
    void onRadialKeyPressed(ItemStack stack, Player player);
}
