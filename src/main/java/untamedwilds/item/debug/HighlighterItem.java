package untamedwilds.item.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;

public class HighlighterItem extends Item {

    public HighlighterItem(Properties properties) {
        super(properties);
    }

    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (playerIn.isSteppingCarefully() && worldIn.isClientSide()) {
            Minecraft.getInstance().debugEntries.toggleStatus(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES);
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity target, InteractionHand hand) {
    if (target.level().isClientSide()) return InteractionResult.PASS;
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 999999, 0, true, false));
        return InteractionResult.SUCCESS;
    }
}