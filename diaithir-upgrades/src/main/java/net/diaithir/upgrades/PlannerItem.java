package net.diaithir.upgrades;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Upgrade planner. Hold it in the main hand and a stack of modules in the off hand, then use it on a
 * machine: every machine within RADIUS blocks swaps each lower-tier module of the same kind and theme
 * family for the off-hand module, one module used per swap. Replaced modules are handed back.
 */
public final class PlannerItem extends Item {
    public static final int RADIUS = 8;

    public PlannerItem(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        var player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack held = player.getOffhandItem();
        if (!(held.getItem() instanceof ModuleItem mi)) {
            if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
            BlockEntity target = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
            var host = target == null ? java.util.Optional.<UpgradeHost>empty() : Upgrades.host(target);
            if (host.isEmpty()) {
                player.sendOverlayMessage(Component.literal("Not an upgradable machine. Hold a module in your other hand to upgrade."));
                return InteractionResult.FAIL;
            }
            player.sendSystemMessage(Component.literal(describe(host.get())));
            return InteractionResult.SUCCESS;
        }
        if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        int budget = player.isCreative() ? Integer.MAX_VALUE : held.getCount();
        int used = upgradeArea(ctx.getLevel(), ctx.getClickedPos(), mi.spec(), budget, old -> giveBack(ctx, player, old));
        if (!player.isCreative()) held.shrink(used);
        player.sendOverlayMessage(Component.literal("Upgraded " + used + " module" + (used == 1 ? "" : "s")));
        return InteractionResult.SUCCESS;
    }

    /**
     * Swap every lower-tier module of the same kind within RADIUS of c for `to`, at most `budget` swaps.
     * Each replaced module is passed to `returned`. Returns the number of swaps.
     */
    static int upgradeArea(net.minecraft.world.level.Level level, BlockPos c, ModuleSpec to, int budget,
                           java.util.function.Consumer<ModuleSpec> returned) {
        List<Slotted> blocks = new ArrayList<>();
        List<BlockEntity> bes = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-RADIUS, -RADIUS, -RADIUS), c.offset(RADIUS, RADIUS, RADIUS))) {
            BlockEntity be = level.getBlockEntity(p);
            if (be == null) continue;
            Upgrades.host(be).ifPresent(h -> { blocks.add(h.slots()); bes.add(be); });
        }
        int used = 0;
        for (int b = 0; b < blocks.size() && used < budget; b++) {
            Slotted s = blocks.get(b);
            boolean changed = false;
            for (int i = 0; i < s.modules().size() && used < budget; i++) {
                ModuleSpec old = s.modules().get(i).spec();
                if (old.kind() == to.kind() && old.tier() < to.tier() && s.replace(i, to, Quality.NORMAL)) {
                    returned.accept(old);
                    used++;
                    changed = true;
                }
            }
            if (changed) {
                bes.get(b).setData(Upgrades.SLOTS, SlotData.of(s));
                bes.get(b).setChanged();
            }
        }
        return used;
    }

    /** One-line readout: slots used, each module, and the resulting speed, output and power multipliers. */
    static String describe(UpgradeHost h) {
        var mods = h.slots().modules();
        StringBuilder sb = new StringBuilder("Modules " + mods.size() + "/" + h.slots().target().slots());
        for (var m : mods) sb.append(" [").append(m.spec().id()).append("]");
        Effects e = h.effects();
        sb.append(String.format(" | speed x%.2f, output x%.2f, power x%.2f, quality chance %.0f%%",
                e.speedMultiplier(), e.outputMultiplier(), e.consumptionMultiplier(), e.clamped().quality() * 100));
        return sb.toString();
    }

    private static void giveBack(UseOnContext ctx, net.minecraft.world.entity.player.Player player, ModuleSpec old) {
        Modules.ALL.stream().filter(i -> i.getId().getPath().equals(old.id())).findFirst().ifPresent(item -> {
            ItemStack out = new ItemStack(item.get());
            if (!player.getInventory().add(out)) {
                ctx.getLevel().addFreshEntity(new ItemEntity(ctx.getLevel(), player.getX(), player.getY(), player.getZ(), out));
            }
        });
    }
}
