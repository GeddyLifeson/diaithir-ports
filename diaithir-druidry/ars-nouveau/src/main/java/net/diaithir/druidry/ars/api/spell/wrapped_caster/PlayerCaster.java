package net.diaithir.druidry.ars.api.spell.wrapped_caster;

import net.diaithir.druidry.ars.api.item.inv.FilterableItemHandler;
import net.diaithir.druidry.ars.api.item.inv.InventoryManager;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerCaster extends LivingCaster {
    public Player player;

    public PlayerCaster(Player livingEntity) {
        super(livingEntity);
        player = livingEntity;
    }

    @Override
    public InventoryManager getInvManager() {
        return new InventoryManager(getInventory()).extractSlotMax(9).insertSlotMax(-1);
    }

    @Override
    public @NotNull List<FilterableItemHandler> getInventory() {
        List<FilterableItemHandler> base = new ArrayList<>();
        base.add(new FilterableItemHandler(new PlayerMainInvWrapper(player.getInventory()), new ArrayList<>()));
        return base;
    }

    @Override
    public SpellContext.CasterType getCasterType() {
        return SpellContext.CasterType.PLAYER;
    }
}
