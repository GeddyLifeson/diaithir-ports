package net.diaithir.portable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;
import java.util.function.Consumer;

/**
 * Stores one item type up to two billion. Placement mode (use on a block) and pickup mode (what to vacuum) are selectable by sneak-use.
 * Place modes: 0 place one, 1 place stack-worth, 2 off. Pickup modes: 0 off, 1 only the stored type, 2 stored type and void everything else nearby.
 */
public final class DeepNullItem extends Item {
    public static final int PLACE_MODES = 3;
    public static final int PICKUP_MODES = 3;

    public DeepNullItem(Properties p) {
        super(p.stacksTo(1));
    }

    private static boolean same(ItemStack a, ItemStack b) {
        return ItemStack.isSameItemSameComponents(a, b);
    }

    public static ItemStack stored(ItemStack host) {
        ItemStack s = host.get(PortableData.DEEP_ITEM.get());
        return s == null ? ItemStack.EMPTY : s;
    }

    public static long count(ItemStack host) {
        Long c = host.get(PortableData.DEEP_COUNT.get());
        return c == null ? 0 : c;
    }

    private static int mode(ItemStack host, boolean place) {
        Integer m = host.get(place ? PortableData.PLACE_MODE.get() : PortableData.PICKUP_MODE.get());
        return m == null ? 0 : m;
    }

    /** Puts a stack in; if empty and a type is unset, adopts the stack's type. Returns the remainder. */
    public static ItemStack insert(ItemStack host, ItemStack in) {
        if (in.isEmpty()) return in;
        ItemStack have = stored(host);
        if (have.isEmpty()) {
            host.set(PortableData.DEEP_ITEM.get(), in.copyWithCount(1));
            host.set(PortableData.DEEP_COUNT.get(), 0L);
        } else if (!same(have, in)) {
            return in;
        }
        long add = Rules.deepAdd(count(host), in.getCount());
        host.set(PortableData.DEEP_COUNT.get(), count(host) + add);
        return in.copyWithCount((int) (in.getCount() - add));
    }

    public static ItemStack extract(ItemStack host, int want) {
        ItemStack have = stored(host);
        if (have.isEmpty()) return ItemStack.EMPTY;
        long take = Rules.deepTake(count(host), Math.min(want, have.getMaxStackSize()));
        host.set(PortableData.DEEP_COUNT.get(), count(host) - take);
        return have.copyWithCount((int) take);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack host = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                boolean second = hand == InteractionHand.OFF_HAND;
                var type = second ? PortableData.PICKUP_MODE.get() : PortableData.PLACE_MODE.get();
                int next = Rules.cycle(mode(host, !second), second ? PICKUP_MODES : PLACE_MODES);
                host.set(type, next);
                player.sendSystemMessage(Component.translatable(second ? "message.diaithirportable.pickup_mode" : "message.diaithirportable.place_mode", next));
            }
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            ItemStack in = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!in.isEmpty()) {
                ItemStack left = insert(host, in);
                in.setCount(left.getCount());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        ItemStack host = ctx.getItemInHand();
        Player player = ctx.getPlayer();
        if (player == null || player.isShiftKeyDown()) return InteractionResult.PASS;
        int pm = mode(host, true);
        ItemStack have = stored(host);
        if (pm == 2 || have.isEmpty() || count(host) <= 0 || !(have.getItem() instanceof BlockItem bi)) return InteractionResult.PASS;
        if (ctx.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        int times = pm == 1 ? Math.min(have.getMaxStackSize(), (int) Math.min(count(host), Integer.MAX_VALUE)) : 1;
        BlockHitResult hit = new BlockHitResult(ctx.getClickLocation(), ctx.getClickedFace(), ctx.getClickedPos(), ctx.isInside());
        int placed = 0;
        for (int i = 0; i < times; i++) {
            ItemStack one = extract(host, 1);
            if (one.isEmpty()) break;
            UseOnContext inner = new UseOnContext(ctx.getLevel(), player, ctx.getHand(), one, hit);
            InteractionResult r = bi.place(new BlockPlaceContext(inner));
            if (!r.consumesAction()) {
                insert(host, one);
                break;
            }
            placed++;
            break;
        }
        return placed > 0 ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack host, ServerLevel level, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(host, level, entity, slot);
        int pk = mode(host, false);
        if (pk == 0 || !(entity instanceof Player player) || level.getGameTime() % 4 != 0) return;
        ItemStack have = stored(host);
        double r = Config.MAGNET_RANGE.get();
        List<ItemEntity> near = level.getEntitiesOfClass(ItemEntity.class, new AABB(player.position(), player.position()).inflate(r), e -> e.isAlive() && !e.hasPickUpDelay());
        int budget = Config.ITEMS_PER_TICK.get();
        for (ItemEntity e : near) {
            if (budget-- <= 0) break;
            ItemStack s = e.getItem();
            boolean matches = have.isEmpty() || same(have, s);
            if (matches) {
                ItemStack left = insert(host, s);
                if (left.isEmpty()) e.discard();
                else e.setItem(left);
            } else if (pk == 2) {
                e.discard();
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> out, TooltipFlag flag) {
        ItemStack have = stored(stack);
        out.accept(have.isEmpty() ? Component.translatable("tooltip.diaithirportable.empty")
                : Component.translatable("tooltip.diaithirportable.stored", have.getHoverName(), count(stack)));
        out.accept(Component.translatable("tooltip.diaithirportable.modes", mode(stack, true), mode(stack, false)));
    }
}
