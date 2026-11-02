package net.diaithir.foundry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Holds molten metal, a queue of items to melt, and the selected cast. The multiblock is the controller plus a 3x3x3 shell of bricks with a drain and a tank. */
public final class FoundryControllerBlockEntity extends BlockEntity {
    private String metal = "";
    private int units;
    private Shape cast = Shape.INGOT;
    private final List<ItemStack> queue = new ArrayList<>();
    private int cooldown;

    public FoundryControllerBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirFoundry.CONTROLLER_BE.get(), pos, state);
    }

    public int capacityUnits() {
        return Config.TANK_BUCKETS.get() * 1000;
    }

    /** Validates a 3x3x3 cube with the controller in the middle of the front face's bottom row, bricks elsewhere, one drain and one tank. */
    public boolean formed(ServerLevel level) {
        int bricks = 0, drains = 0, tanks = 0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = 0; dy <= 2; dy++)
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = worldPosition.offset(dx, dy, dz);
                    if (p.equals(worldPosition)) continue;
                    var b = level.getBlockState(p).getBlock();
                    if (b == DiaithirFoundry.BRICKS.get()) bricks++;
                    else if (b == DiaithirFoundry.DRAIN.get()) drains++;
                    else if (b == DiaithirFoundry.TANK.get()) tanks++;
                    else return false;
                }
        return drains == 1 && tanks >= 1;
    }

    public String status(ServerLevel level) {
        return (formed(level) ? "Formed" : "Not formed") + " | " + (metal.isEmpty() ? "empty" : metal + " " + units + "/" + capacityUnits()) + " | cast " + cast.id();
    }

    public boolean interact(ServerLevel level, Player player, ItemStack held) {
        if (!formed(level)) {
            player.sendOverlayMessage(net.minecraft.network.chat.Component.literal("Foundry is not formed"));
            return true;
        }
        Shape s = Shape.forCast(held.getItem());
        if (s != null) {
            cast = s;
            player.sendOverlayMessage(net.minecraft.network.chat.Component.literal("Cast: " + s.id()));
            return true;
        }
        Optional<MoltenMetal.Melt> m = MoltenMetal.melt(held);
        if (m.isPresent()) {
            queue.add(held.copyWithCount(1));
            held.shrink(1);
            setChanged();
            return true;
        }
        return false;
    }

    /** Pure core for tests: can this metal join the tank, and how many units fit. */
    public static int accept(String current, int currentUnits, String incoming, int incomingUnits, int capacity) {
        if (!current.isEmpty() && currentUnits > 0 && !current.equals(incoming)) return 0;
        return Math.max(0, Math.min(incomingUnits, capacity - currentUnits));
    }

    public void serverTick(ServerLevel level) {
        if (--cooldown > 0) return;
        cooldown = Config.TICKS_PER_RUN.get();
        if (!formed(level)) return;
        if (!queue.isEmpty()) {
            ItemStack next = queue.get(0);
            Optional<MoltenMetal.Melt> m = MoltenMetal.melt(next);
            if (m.isEmpty()) {
                queue.remove(0);
            } else {
                int fits = accept(metal, units, m.get().metal(), m.get().units(), capacityUnits());
                if (fits >= m.get().units()) {
                    metal = m.get().metal();
                    units += m.get().units();
                    queue.remove(0);
                    setChanged();
                }
            }
        }
        if (!metal.isEmpty() && units >= cast.cost()) {
            ItemStack out = MoltenMetal.cast(metal, cast);
            if (!out.isEmpty()) {
                units -= cast.cost();
                if (units == 0) metal = "";
                net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5, out);
                setChanged();
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("metal", metal);
        out.putInt("units", units);
        out.putInt("cast", cast.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        metal = in.getStringOr("metal", "");
        units = in.getIntOr("units", 0);
        cast = Shape.values()[Math.floorMod(in.getIntOr("cast", 0), Shape.values().length)];
    }
}
