package net.diaithir.harbour;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/** /diaithirharbour cargotest: crate limit by office stage, hold contents survive a save/reload of the attachment, and spill on destruction. */
final class CargoTest {
    private CargoTest() {}

    static String run(ServerLevel level) {
        List<String> fails = new ArrayList<>();
        BlockPos at = new BlockPos(0, 250, 0);
        AbstractBoat boat = EntityType.OAK_BOAT.create(level, EntitySpawnReason.COMMAND);
        boat.setPos(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        level.addFreshEntity(boat);
        int none = Office.crateLimit(level, at);
        if (none != 1) fails.add("limit without an office is " + none);
        if (!Cargo.addCrate(boat, none) || Cargo.addCrate(boat, none)) fails.add("limit 1 not enforced");
        for (int stage = 0; stage <= 3; stage++) {
            level.setBlock(at.offset(5, 0, 0), DiaithirHarbour.OFFICE.get().defaultBlockState().setValue(Office.STAGE, stage), 3);
            if (Office.crateLimit(level, at) != 2 + stage) fails.add("stage " + stage + " limit " + Office.crateLimit(level, at));
        }
        while (Cargo.addCrate(boat, Office.crateLimit(level, at))) { }
        if (Cargo.crates(boat) != 5) fails.add("crates " + Cargo.crates(boat));
        Container hold = Cargo.hold(boat);
        if (hold.getContainerSize() != 45) fails.add("hold size " + hold.getContainerSize());
        hold.setItem(0, new ItemStack(Items.COAL, 12));
        hold.setItem(44, new ItemStack(Items.IRON_INGOT, 3));
        hold.setChanged();
        Container again = Cargo.hold(boat);
        if (again.getItem(0).getCount() != 12 || !again.getItem(44).is(Items.IRON_INGOT)) fails.add("contents not kept");
        level.setBlock(at.offset(5, 0, 0), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
        Cargo.spill(level, boat);
        int coal = 0, crates = 0;
        for (Entity e : level.getEntities((Entity) null, new AABB(at).inflate(3))) {
            if (e instanceof ItemEntity ie) {
                if (ie.getItem().is(Items.COAL)) coal += ie.getItem().getCount();
                if (ie.getItem().is(DiaithirHarbour.CARGO_CRATE.get())) crates += ie.getItem().getCount();
                ie.discard();
            }
        }
        if (coal != 12 || crates != 5) fails.add("spill coal " + coal + " crates " + crates);
        boat.discard();
        return fails.isEmpty() ? "Cargo self test PASS" : "Cargo self test FAIL: " + String.join("; ", fails);
    }
}
