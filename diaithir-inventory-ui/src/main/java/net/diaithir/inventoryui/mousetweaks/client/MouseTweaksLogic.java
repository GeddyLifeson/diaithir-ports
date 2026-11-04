// Ported from Mouse Tweaks (BSD-3-Clause) https://github.com/YaLTeR/MouseTweaks
// Modifications: handler/accessor mixins removed; slots are hit-tested directly and clicks go through MultiPlayerGameMode; RMB drag takes over the press instead of disabling vanilla quick-crafting; creative inventory screen is not handled (its slot clicks cannot be issued without a mixin).
package net.diaithir.inventoryui.mousetweaks.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

final class MouseTweaksLogic {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private static Screen openScreen = null;
    private static AbstractContainerScreen<?> screen = null;
    private static Slot oldSelectedSlot = null;
    private static double accumulatedScrollDelta = 0;
    private static boolean canDoLMBDrag = false;
    private static boolean canDoRMBDrag = false;
    private static boolean rmbTweakLeftOriginalSlot = false;

    private MouseTweaksLogic() {}

    static void reset() {
        openScreen = null;
        screen = null;
        oldSelectedSlot = null;
        accumulatedScrollDelta = 0;
        canDoLMBDrag = false;
        canDoRMBDrag = false;
        rmbTweakLeftOriginalSlot = false;
    }

    private static void debug(String msg) {
        if (MouseTweaksConfig.DEBUG.get()) {
            LOGGER.info("[Mouse Tweaks] {}", msg);
        }
    }

    private static boolean eligible(Screen s) {
        if (!(s instanceof AbstractContainerScreen<?> acs)) {
            return false;
        }
        if (s instanceof CreativeModeInventoryScreen) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || mc.player.containerMenu != acs.getMenu()) {
            return false;
        }
        String simple = s.getClass().getSimpleName();
        String full = s.getClass().getName();
        for (String d : MouseTweaksConfig.DISABLED_SCREENS.get()) {
            if (d.equals(simple) || d.equals(full)) {
                return false;
            }
        }
        return true;
    }

    private static void updateScreen(Screen newScreen) {
        if (newScreen == openScreen) {
            return;
        }
        reset();
        openScreen = newScreen;
        if (newScreen != null) {
            debug("Opened " + newScreen.getClass().getName());
            if (eligible(newScreen)) {
                screen = (AbstractContainerScreen<?>) newScreen;
            } else {
                debug("Screen not handled; Mouse Tweaks is disabled for it.");
            }
        }
    }

    private static Slot slotAt(double mx, double my) {
        double px = mx - screen.getGuiLeft();
        double py = my - screen.getGuiTop();
        for (Slot slot : screen.getMenu().slots) {
            if (slot.isActive() && px >= slot.x - 1 && px < slot.x + 17 && py >= slot.y - 1 && py < slot.y + 17) {
                return slot;
            }
        }
        return null;
    }

    private static void click(Slot slot, int button, boolean shift) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || mc.player == null) {
            return;
        }
        mc.gameMode.handleContainerInput(screen.getMenu().containerId, slot.index, button,
                shift ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP, mc.player);
    }

    private static boolean isCraftingOutput(Slot slot) {
        return slot instanceof ResultSlot || slot instanceof FurnaceResultSlot || slot instanceof MerchantResultSlot;
    }

    private static boolean shiftDown() {
        Minecraft mc = Minecraft.getInstance();
        return InputConstants.isKeyDown(mc.getWindow(), InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(mc.getWindow(), InputConstants.KEY_RSHIFT);
    }

    private static ItemStack carried() {
        return Minecraft.getInstance().player.containerMenu.getCarried();
    }

    private static boolean areStacksCompatible(ItemStack a, ItemStack b) {
        return a.isEmpty() || b.isEmpty() || (ItemStack.isSameItem(a, b) && ItemStack.isSameItemSameComponents(a, b));
    }

    /** @return true if the vanilla press must be cancelled. */
    static boolean onMouseClicked(Screen s, double x, double y, int button) {
        updateScreen(s);
        if (screen == null) {
            return false;
        }
        oldSelectedSlot = slotAt(x, y);
        ItemStack onMouse = carried();

        if (button == LEFT) {
            // With an item on the cursor vanilla LMB-dragging starts; don't interfere with it.
            if (onMouse.isEmpty()) {
                canDoLMBDrag = true;
            }
        } else if (button == RIGHT) {
            if (onMouse.isEmpty() || !MouseTweaksConfig.RMB_TWEAK.get()) {
                return false;
            }
            if (oldSelectedSlot == null) {
                return false;
            }
            // Take over the press so vanilla quick-crafting (RMB drag) never starts.
            canDoRMBDrag = true;
            rmbTweakLeftOriginalSlot = false;
            return true;
        }
        return false;
    }

    private static void rmbTweakMaybeClickSlot(Slot slot, ItemStack stackOnMouse) {
        if (slot == null || stackOnMouse.isEmpty() || isCraftingOutput(slot)) {
            return;
        }
        // Skip the checks for bundles so RMB-dragging with a bundle fills it.
        if (!(stackOnMouse.getItem() instanceof BundleItem)) {
            ItemStack slotStack = slot.getItem();
            if (!areStacksCompatible(slotStack, stackOnMouse)) {
                return;
            }
            if (slotStack.getCount() >= slot.getMaxStackSize(slotStack)) {
                return;
            }
        }
        click(slot, RIGHT, false);
    }

    /** @return true if the vanilla release must be cancelled. */
    static boolean onMouseReleased(Screen s, double x, double y, int button) {
        updateScreen(s);
        if (screen == null) {
            return false;
        }
        if (button == LEFT) {
            canDoLMBDrag = false;
        } else if (button == RIGHT && canDoRMBDrag) {
            canDoRMBDrag = false;
            if (!rmbTweakLeftOriginalSlot && oldSelectedSlot != null && !carried().isEmpty()) {
                // Plain right click on the original slot (what vanilla does for a one-slot "drag").
                click(oldSelectedSlot, RIGHT, false);
            }
            return true;
        }
        return false;
    }

    static void onMouseDrag(Screen s, double x, double y, int button) {
        updateScreen(s);
        if (screen == null) {
            return;
        }
        Slot selected = slotAt(x, y);
        if (selected == oldSelectedSlot) {
            return;
        }
        ItemStack onMouse = carried();

        if (canDoRMBDrag && button == RIGHT && !rmbTweakLeftOriginalSlot) {
            rmbTweakLeftOriginalSlot = true;
            rmbTweakMaybeClickSlot(oldSelectedSlot, onMouse);
        }
        oldSelectedSlot = selected;
        if (selected == null) {
            return;
        }

        if (button == LEFT) {
            if (!canDoLMBDrag) {
                return;
            }
            ItemStack selectedStack = selected.getItem();
            if (selectedStack.isEmpty()) {
                return;
            }
            boolean shift = shiftDown();
            if (onMouse.isEmpty()) {
                if (!MouseTweaksConfig.LMB_TWEAK_WITHOUT_ITEM.get() || !shift) {
                    return;
                }
                click(selected, LEFT, true);
            } else {
                if (!MouseTweaksConfig.LMB_TWEAK_WITH_ITEM.get()) {
                    return;
                }
                if (!areStacksCompatible(selectedStack, onMouse)) {
                    return;
                }
                if (shift) {
                    click(selected, LEFT, true);
                } else {
                    // Don't merge if the slot stack wouldn't fully fit on the cursor.
                    if (onMouse.getCount() + selectedStack.getCount() > onMouse.getMaxStackSize()) {
                        return;
                    }
                    click(selected, LEFT, false);
                    if (!isCraftingOutput(selected)) {
                        click(selected, LEFT, false);
                    }
                }
            }
        } else if (button == RIGHT) {
            if (!canDoRMBDrag) {
                return;
            }
            rmbTweakMaybeClickSlot(selected, onMouse);
        }
    }

    static void onMouseScrolled(Screen s, double x, double y, double scrollDelta) {
        updateScreen(s);
        if (screen == null || !MouseTweaksConfig.WHEEL_TWEAK.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Slot selectedSlot = slotAt(x, y);
        if (selectedSlot == null) {
            return;
        }
        ItemStack selectedSlotStack = selectedSlot.getItem();

        double scaledDelta = MouseTweaksConfig.SCROLL_ITEM_SCALING.get().scale(scrollDelta);
        if (accumulatedScrollDelta != 0 && Math.signum(scaledDelta) != Math.signum(accumulatedScrollDelta)) {
            accumulatedScrollDelta = 0;
        }
        accumulatedScrollDelta += scaledDelta;
        int delta = (int) accumulatedScrollDelta;
        accumulatedScrollDelta -= delta;
        if (delta == 0) {
            return;
        }

        List<Slot> slots = screen.getMenu().slots;
        int numItemsToMove = Math.abs(delta);
        boolean pushItems = delta < 0;
        MouseTweaksConfig.ScrollDirection dir = MouseTweaksConfig.WHEEL_SCROLL_DIRECTION.get();
        if (dir.isPositionAware() && otherInventoryIsAbove(selectedSlot, slots)) {
            pushItems = !pushItems;
        }
        if (dir.isInverted()) {
            pushItems = !pushItems;
        }
        if (selectedSlotStack.isEmpty()) {
            return;
        }
        ItemStack stackOnMouse = carried();

        if (isCraftingOutput(selectedSlot)) {
            if (!areStacksCompatible(selectedSlotStack, stackOnMouse)) {
                return;
            }
            if (stackOnMouse.isEmpty()) {
                if (!pushItems) {
                    return;
                }
                while (numItemsToMove-- > 0) {
                    List<Slot> targets = findPushSlots(slots, selectedSlot, selectedSlot.getItem().getCount(), true);
                    if (targets == null) {
                        break;
                    }
                    click(selectedSlot, LEFT, false);
                    for (int i = 0; i < targets.size(); i++) {
                        Slot slot = targets.get(i);
                        if (i == targets.size() - 1) {
                            click(slot, LEFT, false);
                        } else {
                            int times = slot.getMaxStackSize(slot.getItem()) - slot.getItem().getCount();
                            while (times-- > 0) {
                                click(slot, RIGHT, false);
                            }
                        }
                    }
                }
            } else {
                while (numItemsToMove-- > 0) {
                    click(selectedSlot, LEFT, false);
                }
            }
            return;
        }

        if (!stackOnMouse.isEmpty() && areStacksCompatible(selectedSlotStack, stackOnMouse)) {
            return;
        }

        if (pushItems) {
            if (!stackOnMouse.isEmpty() && !selectedSlot.mayPlace(stackOnMouse)) {
                return;
            }
            numItemsToMove = Math.min(numItemsToMove, selectedSlotStack.getCount());
            List<Slot> targets = findPushSlots(slots, selectedSlot, numItemsToMove, false);
            if (targets == null || targets.isEmpty()) {
                return;
            }
            boolean hadItemOnMouse = !stackOnMouse.isEmpty();
            // Right click picks up part of a stack (keeps furnace progress, swaps with bundles); left click only
            // when we want the whole (small) stack.
            int pickUp = RIGHT;
            if (stackOnMouse.isEmpty() && selectedSlotStack.getCount() <= numItemsToMove) {
                pickUp = LEFT;
            }
            click(selectedSlot, pickUp, false);

            ItemStack pickedUp = carried();
            numItemsToMove = Math.min(numItemsToMove, pickedUp.getCount());

            for (Slot slot : targets) {
                int times = slot.getMaxStackSize(pickedUp) - slot.getItem().getCount();
                times = Math.min(times, numItemsToMove);
                numItemsToMove -= times;
                while (times-- > 0) {
                    click(slot, RIGHT, false);
                }
            }

            boolean hasLeftover = !carried().isEmpty();
            if (hadItemOnMouse || hasLeftover) {
                int putDown = LEFT;
                if (hadItemOnMouse && hasLeftover) {
                    putDown = RIGHT;
                }
                click(selectedSlot, putDown, false);
            }
            return;
        }

        // Pull items.
        int maxItemsToMove = selectedSlot.getMaxStackSize(selectedSlotStack) - selectedSlotStack.getCount();
        numItemsToMove = Math.min(numItemsToMove, maxItemsToMove);

        while (numItemsToMove > 0) {
            Slot target = findPullSlot(slots, selectedSlot);
            if (target == null) {
                break;
            }
            ItemStack targetStack = target.getItem();
            int inTarget = targetStack.getCount();

            if (isCraftingOutput(target)) {
                if (maxItemsToMove < inTarget) {
                    break;
                }
                maxItemsToMove -= inTarget;
                numItemsToMove = Math.min(numItemsToMove - 1, maxItemsToMove);
                if (!stackOnMouse.isEmpty() && !selectedSlot.mayPlace(stackOnMouse)) {
                    break;
                }
                click(selectedSlot, LEFT, false);
                click(target, LEFT, false);
                click(selectedSlot, LEFT, false);
                continue;
            }

            boolean hadItemOnMouse = !stackOnMouse.isEmpty();
            if (hadItemOnMouse && !target.mayPlace(stackOnMouse)) {
                break;
            }
            int pickUp = RIGHT;
            if (stackOnMouse.isEmpty() && targetStack.getCount() == 1) {
                pickUp = LEFT;
            }
            click(target, pickUp, false);

            int numPickedUp = carried().getCount();
            int toMove = Math.min(numPickedUp, numItemsToMove);
            if (toMove == numPickedUp) {
                click(selectedSlot, LEFT, false);
            } else {
                for (int i = 0; i < toMove; i++) {
                    click(selectedSlot, RIGHT, false);
                }
            }
            maxItemsToMove -= toMove;
            numItemsToMove -= toMove;

            boolean hasLeftover = !carried().isEmpty();
            if (hadItemOnMouse || hasLeftover) {
                int putDown = LEFT;
                if (hadItemOnMouse && hasLeftover) {
                    putDown = RIGHT;
                }
                click(target, putDown, false);
            }
        }
    }

    private static boolean otherInventoryIsAbove(Slot selectedSlot, List<Slot> slots) {
        Minecraft mc = Minecraft.getInstance();
        boolean selectedInPlayer = selectedSlot.container == mc.player.getInventory();
        int below = 0;
        int above = 0;
        for (Slot slot : slots) {
            if ((slot.container == mc.player.getInventory()) != selectedInPlayer) {
                if (slot.y < selectedSlot.y) {
                    above++;
                } else {
                    below++;
                }
            }
        }
        return above > below;
    }

    private static Slot findPullSlot(List<Slot> slots, Slot selectedSlot) {
        Minecraft mc = Minecraft.getInstance();
        int start;
        int end;
        int step;
        if (MouseTweaksConfig.WHEEL_SEARCH_ORDER.get() == MouseTweaksConfig.SearchOrder.FIRST_TO_LAST) {
            start = 0;
            end = slots.size();
            step = 1;
        } else {
            start = slots.size() - 1;
            end = -1;
            step = -1;
        }
        ItemStack selectedStack = selectedSlot.getItem();
        boolean findInPlayer = selectedSlot.container != mc.player.getInventory();
        for (int i = start; i != end; i += step) {
            Slot slot = slots.get(i);
            if (findInPlayer != (slot.container == mc.player.getInventory())) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                continue;
            }
            if (!areStacksCompatible(selectedStack, stack)) {
                continue;
            }
            return slot;
        }
        return null;
    }

    private static List<Slot> findPushSlots(List<Slot> slots, Slot selectedSlot, int itemCount, boolean mustDistributeAll) {
        Minecraft mc = Minecraft.getInstance();
        ItemStack selectedStack = selectedSlot.getItem();
        boolean findInPlayer = selectedSlot.container != mc.player.getInventory();

        List<Slot> result = new ArrayList<>();
        List<Slot> goodEmpty = new ArrayList<>();

        for (int i = 0; i != slots.size() && itemCount > 0; i++) {
            Slot slot = slots.get(i);
            if (findInPlayer != (slot.container == mc.player.getInventory())) {
                continue;
            }
            if (isCraftingOutput(slot)) {
                continue;
            }
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                if (slot.mayPlace(selectedStack)) {
                    goodEmpty.add(slot);
                }
            } else if (areStacksCompatible(selectedStack, stack) && stack.getCount() < slot.getMaxStackSize(stack)) {
                result.add(slot);
                itemCount -= Math.min(itemCount, slot.getMaxStackSize(stack) - stack.getCount());
            }
        }

        for (int i = 0; i != goodEmpty.size() && itemCount > 0; i++) {
            Slot slot = goodEmpty.get(i);
            result.add(slot);
            itemCount -= Math.min(itemCount, slot.getMaxStackSize(selectedStack));
        }

        if (mustDistributeAll && itemCount > 0) {
            return null;
        }
        return result;
    }
}
