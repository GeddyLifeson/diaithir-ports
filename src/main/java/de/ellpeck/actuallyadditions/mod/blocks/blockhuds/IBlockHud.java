package de.ellpeck.actuallyadditions.mod.blocks.blockhuds;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public interface IBlockHud {
    @OnlyIn(Dist.CLIENT)
    void displayHud(GuiGraphicsExtractor guiGraphics, Minecraft minecraft, Player player, ItemStack stack, HitResult rayCast, Window resolution);
}
