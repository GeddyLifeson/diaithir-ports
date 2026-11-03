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
}
