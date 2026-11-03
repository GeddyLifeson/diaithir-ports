package net.diaithir.aurorian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Scrapper (crystal, input, output) and moonlight forge (input, catalyst, output). Both use the five-slot hopper screen; slots 3 and 4
 * stay empty. Upstream's custom screens are not ported.
 */
public class MachineBlockEntity extends BaseContainerBlockEntity {
    public static final int CRAFT_SCRAPPER = 240;
    private static final float CRYSTAL_SPEEDUP = 0.25F;
    private static final double CRYSTAL_BREAK = 0.25;
    private final boolean forge;
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int progress;

    public MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, boolean forge) {
        super(type, pos, state);
        this.forge = forge;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(forge ? "block.theaurorian.moonlight_forge" : "block.theaurorian.scrapper");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot > 1) {
            return false;
        }
        return forge || slot == 1 || stack.getItem() == DiaithirAurorian.item("crystal");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new HopperMenu(id, inv, this);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        items = NonNullList.withSize(5, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(in, items);
        progress = in.getIntOr("progress", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        ContainerHelper.saveAllItems(out, items);
        out.putInt("progress", progress);
    }

    private boolean crystalAbove(Level level) {
        return level.getBlockState(worldPosition.above()).is(DiaithirAurorian.CRYSTAL.get());
    }

    private boolean canSeeMoon(Level level) {
        return level.canSeeSky(worldPosition.above()) && (level.dimension().equals(AurorianPortalBlock.AURORIAN) || !level.isBrightOutside());
    }

    private int craftTime(Level level) {
        if (forge) {
            float k = Math.max(0, Math.min(300, worldPosition.getY())) / 300F * 4F + 1F;
            return (int) (800 / k);
        }
        return (int) (CRAFT_SCRAPPER * (crystalAbove(level) ? CRYSTAL_SPEEDUP : 1F));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MachineBlockEntity be) {
        ItemStack in = be.items.get(forge(be) ? 0 : 1);
        ItemStack cat = be.items.get(1);
        ItemStack out = be.items.get(2);
        MachineRecipes.Recipe r = in.isEmpty() ? null : MachineRecipes.find(be.forge ? "moonlight_forge" : "scrapper", in, cat);
        boolean ok = r != null && (be.forge ? be.canSeeMoon(level) : be.items.get(0).getItem() == DiaithirAurorian.item("crystal"));
        ItemStack res = ok ? r.result() : ItemStack.EMPTY;
        ok = ok && !res.isEmpty() && (out.isEmpty() || (ItemStack.isSameItemSameComponents(out, res) && out.getCount() + res.getCount() <= out.getMaxStackSize()));
        if (!ok) {
            if (be.progress != 0) {
                be.progress = 0;
                be.setChanged();
            }
            return;
        }
        if (++be.progress < be.craftTime(level)) {
            return;
        }
        be.progress = 0;
        be.finish(level, in, cat, res);
        be.setChanged();
    }

    private static boolean forge(MachineBlockEntity be) {
        return be.forge;
    }

    private void finish(Level level, ItemStack in, ItemStack cat, ItemStack res) {
        boolean keep = true;
        if (forge) {
            in.shrink(1);
            cat.shrink(1);
        } else {
            float damage = in.isDamageableItem() ? (float) in.getDamageValue() / in.getMaxDamage() : 0F;
            keep = damage < 0.25F || level.getRandom().nextFloat() >= damage;
            items.get(0).shrink(1);
            in.shrink(1);
            if (crystalAbove(level) && level.getRandom().nextDouble() < CRYSTAL_BREAK) {
                level.destroyBlock(worldPosition.above(), false);
            }
        }
        if (keep) {
            ItemStack out = items.get(2);
            if (out.isEmpty()) {
                items.set(2, res);
            } else {
                out.grow(res.getCount());
            }
        }
    }
}
