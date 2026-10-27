package com.diaithir.addons.newage.content.electricity.generation.magnet;

import com.diaithir.addons.newage.content.TooltipBlock;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ImplementedMagnetBlock extends Block implements IMagneticBlock , TooltipBlock{
    private final int strength;

    public ImplementedMagnetBlock(Properties properties, int strength) {
        super(properties.strength(5.0F, 7.0F));
        this.strength = strength;
    }

    public void appendTooltip(List<Component> tooltipComponents) {
        tooltipComponents.add(CreateLang.translate("tooltip.create_new_age.magnetic_force").style(ChatFormatting.GRAY).component());
        tooltipComponents.add(CreateLang.text(" " + strength).style(ChatFormatting.AQUA)
                .component());
    }

    @Override
    public float getStrength() {
        return strength;
    }

    public static java.util.function.Function<Properties, ImplementedMagnetBlock> simple(int level) {
        return (p) -> new ImplementedMagnetBlock(p, level);
    }
}
