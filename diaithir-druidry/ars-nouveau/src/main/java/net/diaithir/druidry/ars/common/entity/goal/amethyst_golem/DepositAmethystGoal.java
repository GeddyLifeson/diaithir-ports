package net.diaithir.druidry.ars.common.entity.goal.amethyst_golem;

import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.common.entity.AmethystGolem;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Supplier;

public class DepositAmethystGoal extends Goal {
    public AmethystGolem golem;
    public Supplier<Boolean> canUse;

    int usingTicks;
    boolean isDone;

    public DepositAmethystGoal(AmethystGolem golem, Supplier<Boolean> canUse) {
        this.golem = golem;
        this.canUse = canUse;
    }

    @Override
    public boolean canContinueToUse() {
        return golem.getHome() != null && !isDone;
    }

    @Override
    public void tick() {
        super.tick();
        usingTicks--;
        if (usingTicks <= 0) {
            isDone = true;
            deposit();
            return;
        }
        if (golem.getHome() == null)
            return;

        if (BlockUtil.distanceFrom(golem.blockPosition(), golem.getHome()) <= 2) {
            isDone = true;
            deposit();
        }
        golem.getNavigation().tryMoveToBlockPos(golem.getHome(), 1);
    }

    public void deposit() {
        ResourceHandler<ItemResource> handler = golem.level().getCapability(Capabilities.Item.BLOCK, golem.getHome(), null);
        if (handler != null) {
            ItemStack held = golem.getHeldStack();
            if (held.isEmpty()) {
                return;
            }
            int inserted;
            try (Transaction tx = Transaction.openRoot()) {
                inserted = handler.insert(ItemResource.of(held), held.getCount(), tx);
                tx.commit();
            }
            if (inserted <= 0) {
                return;
            }
            ItemStack left = held.copy();
            left.setCount(held.getCount() - inserted);
            if (left.getCount() <= 0) {
                left = ItemStack.EMPTY;
            }
            golem.setHeldStack(left);
        }
    }

    @Override
    public void start() {
        this.isDone = false;
        this.usingTicks = 80;
        golem.goalState = AmethystGolem.AmethystGolemGoalState.DEPOSIT;
    }

    @Override
    public void stop() {
        super.stop();
        golem.goalState = AmethystGolem.AmethystGolemGoalState.NONE;
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean canUse() {
        if (golem.getHome() == null || golem.getHeldStack().isEmpty())
            return false;
        return canUse.get() && golem.level().getCapability(Capabilities.Item.BLOCK, golem.getHome(), null) != null;
    }
}
