package net.diaithir.portable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public final class StorageBlockEntity extends BlockEntity implements MenuProvider, PortableMenu.LockHolder {
    private Tier tier = Tier.WOOD;
    private Contents main = new Contents(Tier.WOOD.slots, this::setChanged);
    private Contents upgrades = new Contents(Tier.WOOD.upgradeSlots, this::setChanged);
    private boolean locked;
    private int color = -1;
    private int tickCount;

    public StorageBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirPortable.STORAGE_BE.get(), pos, state);
        if (state.getBlock() instanceof StorageBlock b) setTier(b.tier());
    }

    public void setTier(Tier t) {
        this.tier = t;
        this.main = new Contents(t.slots, this::setChanged);
        this.upgrades = new Contents(t.upgradeSlots, this::setChanged);
    }

    public Tier tier() {
        return tier;
    }

    public int color() {
        return color;
    }

    public void setColor(int c) {
        this.color = c;
        setChanged();
    }

    @Override
    public boolean locked() {
        return locked;
    }

    @Override
    public void setLocked(boolean b) {
        this.locked = b;
        setChanged();
    }

    public void tick(ServerLevel level) {
        tickCount++;
        var kinds = Upgrades.kinds(upgrades);
        if (kinds.isEmpty()) return;
        main.setStackLimit(Upgrades.limit(upgrades));
        Vec3 centre = Vec3.atCenterOf(worldPosition);
        if (Upgrades.collect(level, centre, main, kinds, main.getMaxStackSize())) setChanged();
        if (kinds.contains(UpgradeKind.SMELTING) && tickCount % Config.SMELT_TICKS.get() == 0) smeltOne(level);
    }

    private void smeltOne(ServerLevel level) {
        for (int i = 0; i < main.getContainerSize(); i++) {
            ItemStack s = main.getItem(i);
            if (s.isEmpty()) continue;
            var input = new net.minecraft.world.item.crafting.SingleRecipeInput(s);
            var opt = level.recipeAccess().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.SMELTING, input, level);
            if (opt.isEmpty()) continue;
            ItemStack out = opt.get().value().assemble(input);
            if (out.isEmpty()) continue;
            s.shrink(1);
            Upgrades.insert(main, out, Upgrades.kinds(upgrades), main.getMaxStackSize());
            setChanged();
            return;
        }
    }

    public Contents main() {
        return main;
    }

    public Contents upgrades() {
        return upgrades;
    }

    @Override
    public Component getDisplayName() {
        return PortableMenu.title(tier);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        main.setStackLimit(Upgrades.limit(upgrades));
        return new PortableMenu(id, inv, tier, main, upgrades, this, this::setChanged);
    }

    /** Writes this block's contents to an item stack so it survives being picked up. */
    public void writeTo(ItemStack stack) {
        main.saveInto(stack, PortableData.CONTENTS.get());
        upgrades.saveInto(stack, PortableData.UPGRADES.get());
        stack.set(PortableData.COLOR.get(), color);
        stack.set(PortableData.LOCKED.get(), locked);
    }

    public void readFrom(ItemStack stack) {
        ItemContainerContents c = stack.get(PortableData.CONTENTS.get());
        if (c != null) c.copyInto(main.list());
        ItemContainerContents u = stack.get(PortableData.UPGRADES.get());
        if (u != null) u.copyInto(upgrades.list());
        Integer col = stack.get(PortableData.COLOR.get());
        if (col != null) color = col;
        Boolean l = stack.get(PortableData.LOCKED.get());
        if (l != null) locked = l;
        setChanged();
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putInt("tier", tier.ordinal());
        out.putInt("color", color);
        out.putBoolean("locked", locked);
        out.store("main", ItemContainerContents.CODEC, ItemContainerContents.fromItems(main.list()));
        out.store("upgrades", ItemContainerContents.CODEC, ItemContainerContents.fromItems(upgrades.list()));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        setTier(Tier.byOrdinal(in.getIntOr("tier", tier.ordinal())));
        color = in.getIntOr("color", -1);
        locked = in.getBooleanOr("locked", false);
        in.read("main", ItemContainerContents.CODEC).ifPresent(c -> c.copyInto(main.list()));
        in.read("upgrades", ItemContainerContents.CODEC).ifPresent(c -> c.copyInto(upgrades.list()));
    }

    public Level lvl() {
        return level;
    }

    public HolderLookup.Provider unused() {
        return null;
    }
}
