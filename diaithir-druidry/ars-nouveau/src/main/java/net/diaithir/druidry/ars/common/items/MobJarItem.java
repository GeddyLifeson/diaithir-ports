package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.common.block.tile.MobJarTile;
import net.diaithir.druidry.ars.common.lib.EntityTags;
import net.diaithir.druidry.ars.setup.registry.DataComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MobJarItem extends BlockItem {
    public MobJarItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext pLevel, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> pTooltip, @NotNull TooltipFlag pFlag) {
        super.appendHoverText(stack, pLevel, display, pTooltip, pFlag);
        Entity entity = fromItem(stack, ArsNouveau.proxy.getClientWorld());
        if (entity == null)
            return;
        pTooltip.accept(entity.getDisplayName());
        if (entity.hasCustomName()) {
            MutableComponent name = entity.getType().getDescription().copy();
            name.withStyle(ChatFormatting.GRAY);
            pTooltip.accept(name);
        }
        if (entity.typeHolder().is(EntityTags.DRYGMY_BLACKLIST)) {
            Component blacklisted = Component.translatable("ars_nouveau.drygmy.blacklist").withStyle(ChatFormatting.DARK_RED);
            pTooltip.accept(blacklisted);
        }
    }

    public static Entity fromItem(ItemStack stack, Level level) {
        var jarData = stack.get(DataComponentRegistry.MOB_JAR);
        if (jarData == null)
            return null;
        CompoundTag entityTag = jarData.entityTag().orElse(new CompoundTag());
        if (entityTag.isEmpty())
            return null;
        return MobJarTile.loadEntityFromTag(level, entityTag);
    }
}
