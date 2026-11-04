package net.diaithir.aurorian;

import java.util.function.Consumer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** HoeItem with Aurorian tier abilities (see Abilities). */
public class AbilityHoe extends HoeItem {
    private final String id;

    public AbilityHoe(String id, net.minecraft.world.item.ToolMaterial m, float d, float s, Item.Properties p) {
        super(m,d,s,p);
        this.id = id;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, @Nullable T entity, Consumer<Item> onBroken) {
        return entity == null ? amount : Abilities.damage(stack, amount, entity, id);
    }
}
