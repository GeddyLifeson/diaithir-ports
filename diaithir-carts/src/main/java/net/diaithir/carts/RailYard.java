package net.diaithir.carts;

import net.diaithir.yards.PartDef;
import net.diaithir.yards.YardDef;
import net.diaithir.yards.YardRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** The rail assembly yards: data (yards and parts) registered with the shared yards API, and the rollout that sets a coupled train on the rails. */
public final class RailYard {
    private RailYard() {}

    public static final String FREIGHT = "diaithircarts:freight_works", BULK = "diaithircarts:bulk_works";
    public static final int CARS = 3;

    public static void register() {
        List<YardDef.Stage> stages = List.of(
                new YardDef.Stage("chassis", List.of(new YardDef.Slot("chassis", 1)), 60, "frame"),
                new YardDef.Stage("running_gear", List.of(new YardDef.Slot("wheelset", 2)), 60, "gear"),
                new YardDef.Stage("body", List.of(new YardDef.Slot("body", 1)), 60, "body"));
        YardRegistry.register(new YardDef(FREIGHT, YardDef.Tier.WORKSHOP, YardDef.Site.GROUND, List.of(), "freight_train", stages, 1.0, new YardDef.Footprint(3, 6, 3)));
        YardRegistry.register(new YardDef(BULK, YardDef.Tier.WORKSHOP, YardDef.Site.GROUND, List.of(), "bulk_train", stages, 1.0, new YardDef.Footprint(3, 6, 3)));
        part("cart_chassis", "chassis", 180, 60, 0);
        part("wheelset", "wheelset", 90, 45, 0);
        part("freight_body", "body", 140, 40, 1620);
        part("bulk_body", "body", 140, 40, 540);
        YardRegistry.rollout(FREIGHT, (w, p, d, s, b) -> train((ServerLevel) w, (BlockPos) p, EntityType.CHEST_MINECART));
        YardRegistry.rollout(BULK, (w, p, d, s, b) -> train((ServerLevel) w, (BlockPos) p, EntityType.HOPPER_MINECART));
    }

    private static void part(String name, String kind, double kg, double hp, double litres) {
        YardRegistry.register(new PartDef(DiaithirCarts.ID + ":" + name, kind, kg, hp, 0, 0, 0, litres, 0, 3, Map.of()));
    }

    /** A rail within 3 blocks of the controller, or null. */
    public static BlockPos railNear(ServerLevel level, BlockPos pos) {
        for (int r = 1; r <= 3; r++)
            for (Direction d : Direction.Plane.HORIZONTAL) for (int dy = 0; dy <= 1; dy++) {
                BlockPos p = pos.relative(d, r).above(dy);
                if (level.getBlockState(p).getBlock() instanceof BaseRailBlock) return p;
            }
        return null;
    }

    /** Spawns {@link #CARS} carts on the nearest rail, each following the one in front. Returns the carts, or null if there is no rail. */
    static List<AbstractMinecart> train(ServerLevel level, BlockPos yard, EntityType<? extends AbstractMinecart> type) {
        BlockPos rail = railNear(level, yard);
        if (rail == null) return null;
        List<AbstractMinecart> out = new ArrayList<>();
        for (int i = 0; i < CARS; i++) {
            AbstractMinecart c = type.create(level, EntitySpawnReason.COMMAND);
            c.setPos(rail.getX() + 0.5, rail.getY() + 0.1, rail.getZ() + 0.5);
            level.addFreshEntity(c);
            if (i > 0) Coupling.link(level, out.get(i - 1), c);
            out.add(c);
        }
        return out;
    }
}
