package net.diaithir.materials;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;

/** Walking on a road surface gives a speed bonus from the table's road_speed; leaving it removes the bonus. */
final class RoadSpeed {
    private static final Identifier ID = Identifier.fromNamespaceAndPath(DiaithirMaterials.ID, "road_speed");

    private RoadSpeed() {}

    static void tick(ServerPlayer p) {
        AttributeInstance speed = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        double bonus = bonusAt(p);
        AttributeModifier cur = speed.getModifier(ID);
        if (bonus <= 0) {
            if (cur != null) speed.removeModifier(ID);
        } else if (cur == null || cur.amount() != bonus) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    /** Bonus for the block under the player's feet, 0 when it is not one of our road surfaces or the player is in the air. */
    static double bonusAt(ServerPlayer p) {
        if (!p.onGround()) return 0;
        BlockState below = p.level().getBlockState(p.blockPosition().below());
        Materials.Row r = Materials.of(below.getBlock());
        return r == null ? 0 : r.roadSpeed();
    }
}
