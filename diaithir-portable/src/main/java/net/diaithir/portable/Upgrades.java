package net.diaithir.portable;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/** Reads upgrades from a container of upgrade items and applies tick behaviour. */
public final class Upgrades {
    private Upgrades() {
    }

    public static Set<UpgradeKind> kinds(Container upgrades) {
        Set<UpgradeKind> out = EnumSet.noneOf(UpgradeKind.class);
        for (int i = 0; i < upgrades.getContainerSize(); i++) {
            ItemStack s = upgrades.getItem(i);
            if (s.getItem() instanceof UpgradeItem u) out.add(u.kind());
        }
        return out;
    }

    public static int count(Container upgrades, UpgradeKind k) {
        int n = 0;
        for (int i = 0; i < upgrades.getContainerSize(); i++) {
            if (upgrades.getItem(i).getItem() instanceof UpgradeItem u && u.kind() == k) n++;
        }
        return n;
    }

    public static int limit(Container upgrades) {
        return Rules.stackSize(64, count(upgrades, UpgradeKind.STACK), Config.STACK_MULTIPLIER.get());
    }

    /** Try to put a stack into the container, honouring void. Returns what is left. */
    public static ItemStack insert(Container into, ItemStack stack, Set<UpgradeKind> kinds, int limit) {
        ItemStack rest = stack.copy();
        for (int pass = 0; pass < 2 && !rest.isEmpty(); pass++) {
            for (int i = 0; i < into.getContainerSize() && !rest.isEmpty(); i++) {
                ItemStack slot = into.getItem(i);
                int cap = Math.min(limit, Math.max(rest.getMaxStackSize(), 1) * Math.max(1, limit / 64));
                if (pass == 0 && !slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, rest)) {
                    int add = Math.min(cap - slot.getCount(), rest.getCount());
                    if (add > 0) {
                        slot.grow(add);
                        rest.shrink(add);
                    }
                } else if (pass == 1 && slot.isEmpty()) {
                    int add = Math.min(cap, rest.getCount());
                    ItemStack put = rest.copyWithCount(add);
                    into.setItem(i, put);
                    rest.shrink(add);
                }
            }
        }
        if (!rest.isEmpty() && kinds.contains(UpgradeKind.VOID)) {
            boolean holds = false;
            for (int i = 0; i < into.getContainerSize(); i++) if (ItemStack.isSameItemSameComponents(into.getItem(i), rest)) holds = true;
            if (holds) return ItemStack.EMPTY;
        }
        return rest;
    }

    /** Magnet and pickup: pull nearby item entities into the container. Returns true if anything moved. */
    public static boolean collect(ServerLevel level, Vec3 centre, Container into, Set<UpgradeKind> kinds, int limit) {
        if (!kinds.contains(UpgradeKind.MAGNET) && !kinds.contains(UpgradeKind.PICKUP)) return false;
        double r = Config.MAGNET_RANGE.get();
        List<ItemEntity> near = level.getEntitiesOfClass(ItemEntity.class, new AABB(centre, centre).inflate(r), e -> e.isAlive() && !e.hasPickUpDelay());
        boolean moved = false;
        int budget = Config.ITEMS_PER_TICK.get();
        for (ItemEntity e : near) {
            if (budget-- <= 0) break;
            if (!kinds.contains(UpgradeKind.PICKUP) && e.position().distanceToSqr(centre) > 1.5) {
                if (kinds.contains(UpgradeKind.MAGNET)) e.setDeltaMovement(centre.subtract(e.position()).normalize().scale(0.25));
                continue;
            }
            ItemStack left = insert(into, e.getItem(), kinds, limit);
            if (left.getCount() != e.getItem().getCount()) moved = true;
            if (left.isEmpty()) e.discard();
            else e.setItem(left);
        }
        return moved;
    }

    /** Feeding: eat the best food from the container when the player is hungry. */
    public static void feed(Player player, Container from) {
        if (!player.getFoodData().needsFood()) return;
        for (int i = 0; i < from.getContainerSize(); i++) {
            ItemStack s = from.getItem(i);
            FoodProperties food = s.get(net.minecraft.core.component.DataComponents.FOOD);
            if (food == null) continue;
            player.getFoodData().eat(food);
            s.shrink(1);
            from.setChanged();
            return;
        }
    }

    public static DataComponentType<ItemContainerContents> slotsComponent() {
        return PortableData.UPGRADES.get();
    }
}
