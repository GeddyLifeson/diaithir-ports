package net.diaithir.worldsys.structurecompass;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Item that delegates its use action to the structure search logic. */
public final class WayfinderCompassItem extends Item {
    public WayfinderCompassItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return StructureCompassFeature.use(level, player, hand);
    }
}
