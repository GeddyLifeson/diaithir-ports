package org.diaithir.mega;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LaserDrillBlockEntity extends BlockEntity {
    public static final int WORK_TICKS = 100;     // upstream OreLaserBaseConfig.maxProgress
    public static final int SLOTS = 9;

    private final NonNullList<ItemStack> inv = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
    private final List<String> lenses = new ArrayList<>();
    private int progress;
    private Integer depthOverride;

    public LaserDrillBlockEntity(BlockPos pos, BlockState state) { super(MegaBlocks.LASER_BE.get(), pos, state); }

    public int depth() { return depthOverride != null ? depthOverride : worldPosition.getY(); }
    public void setDepthOverride(Integer y) { depthOverride = y; }

    public boolean addLens(String item) {
        if (lenses.size() >= 2) return false;
        lenses.add(item);
        setChanged();
        return true;
    }

    public int storedCount() { int n = 0; for (ItemStack s : inv) n += s.getCount(); return n; }

    public void serverTick() {
        if (level == null || level.isClientSide()) return;
        if (++progress < WORK_TICKS) return;
        progress = 0;
        String dim = level.dimension().identifier().toString();
        String out = LaserDrill.pick(LaserDrillOres.rows(), dim, List.of(), depth(), lenses, new java.util.Random(level.getRandom().nextLong()));
        if (out == null) return;
        var item = BuiltInRegistries.ITEM.getValue(Identifier.parse(out));
        ItemStack s = new ItemStack(item);
        for (int i = 0; i < SLOTS && !s.isEmpty(); i++) {
            ItemStack t = inv.get(i);
            if (t.isEmpty()) { inv.set(i, s.copy()); s.setCount(0); }
            else if (ItemStack.isSameItemSameComponents(t, s) && t.getCount() < t.getMaxStackSize()) { t.grow(1); s.shrink(1); }
        }
        setChanged();     // a full output simply loses the draw, as upstream refuses to work when full
    }

    public int give(Player p) {
        int n = 0;
        for (int i = 0; i < SLOTS; i++) {
            if (inv.get(i).isEmpty()) continue;
            ItemStack s = inv.get(i).copy();
            if (!p.getInventory().add(s)) { inv.set(i, s); continue; }
            inv.set(i, ItemStack.EMPTY); n++;
        }
        setChanged();
        return n;
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        progress = in.getIntOr("progress", 0);
        lenses.clear();
        in.getStringOr("lenses", "").lines().filter(x -> !x.isEmpty()).forEach(lenses::add);
        inv.clear();
        ContainerHelper.loadAllItems(in, inv);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("progress", progress);
        out.putString("lenses", String.join("\n", lenses));
        ContainerHelper.saveAllItems(out, inv);
    }
}
