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
        }
    }
}
