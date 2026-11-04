package net.diaithir.aurorian;

import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Item with Aurorian tier abilities (see Abilities). */
public class AbilityItem extends Item {
    private final String id;

    public AbilityItem(String id, Item.Properties p) {
        super(p);
        this.id = id;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return entity == null ? amount : Abilities.damage(stack, amount, entity, id);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (id.equals("umbra_greatsword")) {
            Abilities.greatswordTick(stack, entity);
        } else if (id.equals("absorption_orb")) {
            Abilities.orbTick(stack, entity);
        }
    }

    @Override
    public net.minecraft.world.InteractionResult use(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (id.equals("mirror_of_guidance")) {
            if (!level.isClientSide()) {
                MirrorGuide.open(player, hand);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        if (id.equals("crystalline_sword")) {
            player.startUsingItem(hand);
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        if (id.equals("living_divining_rod") && level instanceof ServerLevel sl) {
            Abilities.divine(sl, player, player.getItemInHand(hand), hand);
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        ItemStack r = super.finishUsingItem(stack, level, user);
        if (id.equals("strange_meat") && !level.isClientSide()) {
            Abilities.strangeMeat(user);
        }
        return r;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return id.equals("crystalline_sword") ? 72000 : super.getUseDuration(stack, user);
    }

    @Override
    public net.minecraft.world.item.ItemUseAnimation getUseAnimation(ItemStack stack) {
        return id.equals("crystalline_sword") ? net.minecraft.world.item.ItemUseAnimation.BOW : super.getUseAnimation(stack);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (!id.equals("crystalline_sword") || level.isClientSide() || getUseDuration(stack, user) - remaining < 19) {
            return false;
        }
        AurorianProjectiles.Beam beam = new AurorianProjectiles.Beam(AurorianProjectiles.BEAM.get(), level);
        beam.setOwner(user);
        beam.setPos(user.getX(), user.getEyeY() - 0.3, user.getZ());
        beam.shootFromRotation(user, user.getXRot(), user.getYRot(), 0F, AurorianProjectiles.BEAM_VELOCITY, 0F);
        level.addFreshEntity(beam);
        level.playSound(null, user.getX(), user.getY(), user.getZ(), net.minecraft.sounds.SoundEvents.GRINDSTONE_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1F, 2.5F);
        level.playSound(null, user.getX(), user.getY(), user.getZ(), net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1F, 5F);
        stack.hurtAndBreak(1, user, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        return true;
    }
}
