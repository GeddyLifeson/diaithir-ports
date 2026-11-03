package net.diaithir.druidry.ars.api.spell.wrapped_caster;

import net.diaithir.druidry.ars.api.item.inv.FilterableItemHandler;
import net.diaithir.druidry.ars.api.spell.SpellContext;
import net.diaithir.druidry.ars.api.util.InvUtil;
import net.diaithir.druidry.ars.common.block.tile.RuneTile;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RuneCaster extends TileCaster{

    public RuneCaster(RuneTile tile, SpellContext.CasterType casterType) {
        super(tile, casterType);
    }

    @Override
    public @NotNull List<FilterableItemHandler> getInventory() {
        RuneTile tile1 = (RuneTile) tile;
        if(tile1.isSensitive){
            Player player = tile1.getLevel().getPlayerByUUID(tile1.uuid);
            if (player != null) {
                return InvUtil.fromPlayer(player);
            }
            return new ArrayList<>();
        }
        return super.getInventory();
    }
}
