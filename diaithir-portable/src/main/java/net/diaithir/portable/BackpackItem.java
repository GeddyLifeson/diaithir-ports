package net.diaithir.portable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A backpack: an item form of a tiered storage. Use in air opens it; use on a block places the matching storage block with contents kept. Ticks while in inventory. */
public final class BackpackItem extends BlockItem {
    private final Tier tier;

    public BackpackItem(StorageBlock block, Properties p) {
        super(block, p.stacksTo(1));
        this.tier = block.tier();
    }

    public Tier tier() {
        return tier;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (player instanceof ServerPlayer sp) openFor(sp, stack, hand == InteractionHand.MAIN_HAND ? sp.getInventory().getSelectedSlot() : 40);
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown()) return super.useOn(ctx);
        return use(ctx.getLevel(), ctx.getPlayer(), ctx.getHand());
    }

    public static void openFor(ServerPlayer sp, ItemStack stack, int invSlot) {
        BackpackItem item = (BackpackItem) stack.getItem();
        Tier t = item.tier;
        Contents main = Contents.load(stack, PortableData.CONTENTS.get(), t.slots, () -> {
        });
        Contents ups = Contents.load(stack, PortableData.UPGRADES.get(), t.upgradeSlots, () -> {
        });
        Runnable save = () -> {
            main.saveInto(stack, PortableData.CONTENTS.get());
            ups.saveInto(stack, PortableData.UPGRADES.get());
        };
        Contents m = Contents.load(stack, PortableData.CONTENTS.get(), t.slots, save);
        Contents u = Contents.load(stack, PortableData.UPGRADES.get(), t.upgradeSlots, save);
        m.setStackLimit(Upgrades.limit(u));
        PortableMenu.LockHolder lock = new PortableMenu.LockHolder() {
            public boolean locked() {
                return Boolean.TRUE.equals(stack.get(PortableData.LOCKED.get()));
            }

            public void setLocked(boolean b) {
                stack.set(PortableData.LOCKED.get(), b);
            }
        };
        sp.openMenu(new SimpleMenuProvider((id, inv, p) -> new PortableMenu(id, inv, t, m, u, lock, save), PortableMenu.title(t)), buf -> {
            buf.writeVarInt(t.ordinal());
            buf.writeBoolean(lock.locked());
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof Player player) || level.getGameTime() % 5 != 0) return;
        Contents ups = Contents.load(stack, PortableData.UPGRADES.get(), tier.upgradeSlots, () -> {
        });
        var kinds = Upgrades.kinds(ups);
        if (kinds.isEmpty()) return;
        Runnable[] saver = new Runnable[1];
        Contents main = Contents.load(stack, PortableData.CONTENTS.get(), tier.slots, () -> saver[0].run());
        saver[0] = () -> main.saveInto(stack, PortableData.CONTENTS.get());
        int limit = Upgrades.limit(ups);
        main.setStackLimit(limit);
        boolean changed = Upgrades.collect(level, player.position().add(0, 0.5, 0), main, kinds, limit);
        if (kinds.contains(UpgradeKind.FEEDING) && level.getGameTime() % Config.FEED_INTERVAL.get() == 0) Upgrades.feed(player, main);
        if (changed) saver[0].run();
    }

    public static Vec3 centre(BlockPos p) {
        return Vec3.atCenterOf(p);
    }

    public static Component name(Tier t) {
        return PortableMenu.title(t);
    }
}
