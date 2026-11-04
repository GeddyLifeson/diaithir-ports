package net.diaithir.aurorian;

import java.util.function.Consumer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import org.jspecify.annotations.Nullable;

/** Sickles: shears with the tier's durability; moonstone sickles use the moonstone durability rule. */
public class AbilityShears extends ShearsItem {
    private final String id;

    public AbilityShears(String id, Properties p) {
        super(p);
        this.id = id;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return entity == null ? amount : Abilities.damage(stack, amount, entity, id);
    }
}
