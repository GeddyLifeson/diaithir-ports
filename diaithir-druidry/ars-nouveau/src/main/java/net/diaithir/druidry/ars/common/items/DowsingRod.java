package net.diaithir.druidry.ars.common.items;

import net.diaithir.druidry.ars.api.block.IPedestalMachine;
import net.diaithir.druidry.ars.api.scrying.TagScryer;
import net.diaithir.druidry.ars.common.datagen.BlockTagProvider;
import net.diaithir.druidry.ars.common.ritual.RitualScrying;
import net.diaithir.druidry.ars.setup.registry.ItemsRegistry;
import net.diaithir.druidry.ars.setup.registry.ModPotions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class DowsingRod extends ModItem {
    public DowsingRod(Properties properties) {
        super(properties);
        withTooltip(Component.translatable("tooltip.ars_nouveau.dowsing_rod"));
    }

    public DowsingRod() {
        this(ItemsRegistry.defaultItemProperties().durability(4));
    }


    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (pContext.getLevel() instanceof ServerLevel && pContext.getLevel().getBlockEntity(pContext.getClickedPos()) instanceof IPedestalMachine ipm){
            ipm.lightPedestal(pContext.getLevel());
            return InteractionResult.SUCCESS;
        }
        return super.useOn(pContext);
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack heldStack = pPlayer.getItemInHand(pUsedHand);
        heldStack.setDamageValue(pPlayer.getItemInHand(pUsedHand).getDamageValue() + 1);
        if (heldStack.getDamageValue() >= getMaxDamage(heldStack))
            heldStack.shrink(1);
        if (!pLevel.isClientSide()) {
            pPlayer.addEffect(new MobEffectInstance(ModPotions.MAGIC_FIND_EFFECT, 60 * 20));
            TagScryer tagScryer = new TagScryer(BlockTagProvider.DOWSING_ROD);
            RitualScrying.grantScrying((ServerPlayer) pPlayer, 60 * 20, tagScryer);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }
}
