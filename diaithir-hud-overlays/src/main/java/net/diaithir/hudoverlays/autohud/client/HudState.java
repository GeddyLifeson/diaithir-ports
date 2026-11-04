// Ported from AutoHUD (LGPL-3.0-only) https://github.com/Crendgrim/AutoHUD
// Modifications: Replaces Hud/Components/State/EventHandler classes: global dynamic flag, component registry and per tick state tracking.
package net.diaithir.hudoverlays.autohud.client;

import net.diaithir.hudoverlays.autohud.config.AutoHudConfig;
import net.diaithir.hudoverlays.autohud.config.AutoHudConfig.EventPolicy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class HudState {
    private static boolean dynamic = false;
    private static boolean wasPeeking = false;

    public static final HudComponent ARMOR = new HudComponent("armor", () -> true, () -> true,
            AutoHudConfig.ARMOR_POLICY::get, AutoHudConfig.DIR_ARMOR::get, null, true);
    public static final HudComponent AIR = new HudComponent("air", () -> true, () -> true,
            AutoHudConfig.AIR_POLICY::get, AutoHudConfig.DIR_AIR::get, null, true);
    public static final HudComponent HEALTH = new HudComponent("health", () -> true, () -> true,
            AutoHudConfig.HEALTH_POLICY::get, AutoHudConfig.DIR_HEALTH::get, null, true).stack(ARMOR);
    public static final HudComponent HUNGER = new HudComponent("hunger", () -> true, () -> true,
            AutoHudConfig.HUNGER_POLICY::get, AutoHudConfig.DIR_HUNGER::get, null, true).stack(AIR);
    public static final HudComponent MOUNT_HEALTH = new HudComponent("mount_health", () -> true, () -> true,
            AutoHudConfig.MOUNT_HEALTH_POLICY::get, AutoHudConfig.DIR_MOUNT_HEALTH::get, null, true).stack(AIR);
    public static final HudComponent MOUNT_JUMP = new HudComponent("mount_jump_bar", AutoHudConfig.MOUNT_JUMP_ACTIVE::get,
            AutoHudConfig.MOUNT_JUMP_ON_CHANGE::get, null, AutoHudConfig.DIR_MOUNT_JUMP::get, null, true);
    public static final HudComponent LOCATOR_BAR = new HudComponent("locator_bar", AutoHudConfig.LOCATOR_BAR_ACTIVE::get,
            AutoHudConfig.LOCATOR_BAR_ON_CHANGE::get, null, AutoHudConfig.DIR_LOCATOR::get, null, true);
    public static final HudComponent XP_LEVEL = new HudComponent("experience_level", AutoHudConfig.EXPERIENCE_ACTIVE::get,
            AutoHudConfig.EXPERIENCE_ON_CHANGE::get, null, AutoHudConfig.DIR_EXPERIENCE::get, null, true);
    public static final HudComponent XP_BAR = new HudComponent("experience_bar", AutoHudConfig.EXPERIENCE_BAR_ACTIVE::get,
            AutoHudConfig.EXPERIENCE_BAR_ON_CHANGE::get, null, AutoHudConfig.DIR_EXPERIENCE::get, null, true)
            .stack(HEALTH, HUNGER, MOUNT_HEALTH, XP_LEVEL);
    public static final HudComponent HOTBAR = new HudComponent("hotbar", AutoHudConfig.HOTBAR_ACTIVE::get,
            AutoHudConfig.HOTBAR_ON_CHANGE::get, null, AutoHudConfig.DIR_HOTBAR::get, null, true).stack(XP_BAR);
    public static final HudComponent TOOLTIP = new HudComponent("tooltip",
            () -> AutoHudConfig.HIDE_TOOLTIP.get() || AutoHudConfig.HOTBAR_ACTIVE.get(),
            () -> !AutoHudConfig.HIDE_TOOLTIP.get() && AutoHudConfig.HOTBAR_ON_CHANGE.get(),
            null, AutoHudConfig.DIR_HOTBAR::get, null, true);
    public static final HudComponent SCOREBOARD = new HudComponent("scoreboard", AutoHudConfig.SCOREBOARD_ACTIVE::get,
            AutoHudConfig.SCOREBOARD_ON_CHANGE::get, null, AutoHudConfig.DIR_SCOREBOARD::get, null, false);
    public static final HudComponent CROSSHAIR = new HudComponent("crosshair", AutoHudConfig.CROSSHAIR_ACTIVE::get,
            AutoHudConfig.CROSSHAIR_ON_CHANGE::get, null, () -> AutoHudConfig.Direction.UP,
            AutoHudConfig.CROSSHAIR_MAX_FADE::get, false);
    public static final HudComponent CHAT = new HudComponent("chat", AutoHudConfig.CHAT_ACTIVE::get, () -> false, null,
            AutoHudConfig.DIR_CHAT::get, null, false);
    public static final HudComponent CHAT_INDICATOR = new HudComponent("chat_indicator", AutoHudConfig.CHAT_INDICATOR::get,
            () -> false, null, AutoHudConfig.DIR_CHAT::get, null, false);
    public static final HudComponent ACTION_BAR = new HudComponent("action_bar", AutoHudConfig.ACTION_BAR_ACTIVE::get,
            () -> false, null, AutoHudConfig.DIR_ACTION_BAR::get, null, false);
    public static final HudComponent BOSS_BAR = new HudComponent("boss_bar", AutoHudConfig.BOSS_BAR_ACTIVE::get,
            () -> false, null, AutoHudConfig.DIR_BOSS_BAR::get, null, false);
    public static final HudComponent STATUS_EFFECTS = new HudComponent("status_effects", AutoHudConfig.STATUS_EFFECTS_ACTIVE::get,
            AutoHudConfig.STATUS_EFFECTS_ON_CHANGE::get, null, AutoHudConfig.DIR_STATUS_EFFECTS::get, null, false);

    static {
        // ChatIndicator is not part of the global reveal sets
        HudComponent.ALL.remove(CHAT_INDICATOR);
    }

    // per tick tracking
    private static ItemStack prevMain = ItemStack.EMPTY, prevOff = ItemStack.EMPTY;
    private static int prevSlot = -1;
    private static int prevHealth = -1, prevFood = -1, prevAir = -1, prevLevel = -1, prevArmor = -1;
    private static float prevXpProgress = -1;
    private static double lastX, lastY, lastZ;
    private static int standingTicks = 0;
    private static boolean wasSneaking, wasFlying, wasOnScreen, wasPaused;
    private static boolean initialized = false;
    private static boolean hadMount = false;
    private static int mountHealthPrev = -1;
    private static int prevEffectCount = -1;
    private static boolean prevCrosshairTarget = false;

    private HudState() {}

    public static boolean dynamic() { return dynamic || wasPeeking; }
    public static boolean rawDynamic() { return dynamic; }

    public static void enableDynamic() { dynamic = true; HudComponent.hideAll(); }
    public static void disableDynamic() { dynamic = false; HudComponent.revealAll(); CHAT_INDICATOR.hideNow(); }
    public static void toggle() { if (dynamic) disableDynamic(); else enableDynamic(); }

    public static void peek(boolean doPeek) {
        if (dynamic == doPeek) {
            HudComponent.revealAll();
            CHAT_INDICATOR.hideNow();
        } else if (doPeek != wasPeeking) {
            HudComponent.hideAll();
        }
        wasPeeking = doPeek;
    }

    public static void reset(LocalPlayer p) {
        initialized = false;
        wasPeeking = false;
        prevMain = ItemStack.EMPTY;
        prevOff = ItemStack.EMPTY;
        prevSlot = -1;
        prevHealth = prevFood = prevAir = prevLevel = prevArmor = -1;
        prevXpProgress = -1;
        hadMount = false;
        mountHealthPrev = -1;
        prevEffectCount = -1;
        HudComponent.ALL.forEach(c -> { c.alpha = 1; c.offset = 0; c.alphaDelta = 0; c.offsetDelta = 0; c.scheduleUpdate(); });
        CHAT_INDICATOR.hideNow();
        if (AutoHudConfig.DYNAMIC_ON_LOAD.get()) {
            dynamic = true;
            HudComponent.hideAll();
        } else {
            dynamic = false;
        }
        initialized = true;
    }

    public static void unregister() {
        initialized = false;
    }

    private static boolean policyHelp(ItemStack stack) {
        return AutoHudConfig.REVEAL_BARS_WITH_CONSUMABLE.get();
    }

    private static boolean isFood(ItemStack s) {
        return s.has(net.minecraft.core.component.DataComponents.CONSUMABLE) && s.has(net.minecraft.core.component.DataComponents.FOOD);
    }

    private static boolean healsFromItem(ItemStack s) {
        if (s.isEmpty()) return false;
        var cons = s.get(net.minecraft.core.component.DataComponents.CONSUMABLE);
        if (cons == null) return false;
        for (var eff : cons.onConsumeEffects()) {
            if (eff instanceof net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect apply) {
                for (var inst : apply.effects()) {
                    var type = inst.getEffect();
                    if (type.is(net.minecraft.world.effect.MobEffects.REGENERATION)
                            || type.is(net.minecraft.world.effect.MobEffects.INSTANT_HEALTH)
                            || type.is(net.minecraft.world.effect.MobEffects.HEALTH_BOOST)
                            || type.is(net.minecraft.world.effect.MobEffects.ABSORPTION)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void revealHotbarOnChange(ItemStack newS, ItemStack oldS) {
        if (ItemStack.isSameItemSameComponents(newS, oldS)) {
            if (newS.getCount() != oldS.getCount() && AutoHudConfig.HOTBAR_ON_STACK_CHANGE.get()) HOTBAR.revealCombined();
            if (newS.getDamageValue() != oldS.getDamageValue() && AutoHudConfig.HOTBAR_ON_DAMAGE_CHANGE.get()) HOTBAR.revealCombined();
            return;
        }
        if (!ItemStack.isSameItem(newS, oldS)) {
            if (HOTBAR.onChange()) HOTBAR.revealCombined();
        } else {
            if (newS.getCount() != oldS.getCount() && AutoHudConfig.HOTBAR_ON_STACK_CHANGE.get()) HOTBAR.revealCombined();
            else if (newS.getDamageValue() != oldS.getDamageValue() && AutoHudConfig.HOTBAR_ON_DAMAGE_CHANGE.get()) HOTBAR.revealCombined();
        }
    }

    private static boolean durabilityLow(ItemStack s) {
        if (!s.isDamageableItem()) return false;
        int max = s.getMaxDamage();
        int dmg = s.getDamageValue();
        return dmg >= (100 - AutoHudConfig.HOTBAR_DURABILITY_PERCENT.get()) / 100.0 * max
                && (max - dmg) < AutoHudConfig.HOTBAR_DURABILITY_TOTAL.get();
    }

    private static void reveal(EventPolicy p, Runnable stateChange) {
        switch (p) {
            case REVEAL -> HudComponent.revealAll();
            case HIDE -> HudComponent.forceHideAll();
            case NOTHING -> { if (stateChange != null) stateChange.run(); }
        }
    }

    public static void tick(Minecraft mc, LocalPlayer player) {
        if (player == null) return;
        if (!initialized) reset(player);

        // screens
        boolean onScreen = mc.screen != null;
        boolean paused = mc.screen instanceof PauseScreen;
        boolean chat = mc.screen instanceof ChatScreen;
        if (chat) {
            CHAT.revealNow();
            CHAT_INDICATOR.hideNow();
        }
        if (onScreen && !chat) {
            if (paused) {
                reveal(AutoHudConfig.ON_PAUSE_SCREEN.get(), null);
            } else {
                reveal(AutoHudConfig.ON_SCREEN_OPEN.get(), null);
            }
        }
        if (!onScreen && wasOnScreen) {
            if (wasPaused) {
                switch (AutoHudConfig.ON_PAUSE_SCREEN.get()) {
                    case HIDE -> HudComponent.updateAll();
                    case REVEAL -> HudComponent.hideAll();
                    default -> { }
                }
            } else {
                switch (AutoHudConfig.ON_SCREEN_OPEN.get()) {
                    case HIDE -> HudComponent.updateAll();
                    case REVEAL -> HudComponent.hideAll();
                    default -> { }
                }
            }
        }
        wasOnScreen = onScreen;
        wasPaused = paused;

        // movement
        double dx = player.getX() - lastX, dy = player.getY() - lastY, dz = player.getZ() - lastZ;
        boolean moving = dx * dx + dy * dy + dz * dz > 1.0E-6;
        lastX = player.getX(); lastY = player.getY(); lastZ = player.getZ();
        if (moving) {
            standingTicks = 0;
            reveal(AutoHudConfig.ON_MOVING.get(), null);
        } else {
            if (standingTicks < 20) standingTicks++;
            if (standingTicks >= 20) {
                reveal(AutoHudConfig.ON_STANDING_STILL.get(), () -> {
                    if (AutoHudConfig.ON_MOVING.get() == EventPolicy.HIDE) HudComponent.updateAll();
                });
            }
        }

        boolean sneaking = player.isShiftKeyDown();
        if (sneaking) {
            reveal(AutoHudConfig.ON_SNEAKING.get(), null);
        } else if (wasSneaking) {
            switch (AutoHudConfig.ON_SNEAKING.get()) {
                case REVEAL -> HudComponent.hideAll();
                case HIDE -> HudComponent.updateAll();
                default -> { }
            }
        }
        wasSneaking = sneaking;

        boolean flying = player.isFallFlying();
        if (flying) {
            reveal(AutoHudConfig.ON_FLYING.get(), null);
        } else if (wasFlying) {
            switch (AutoHudConfig.ON_FLYING.get()) {
                case REVEAL -> HudComponent.hideAll();
                case HIDE -> HudComponent.updateAll();
                default -> { }
            }
        }
        wasFlying = flying;

        // interaction
        if (AutoHudConfig.ON_USING_ITEM.get() && player.isUsingItem()) HudComponent.revealAll();
        if (AutoHudConfig.ON_MINING.get() && mc.gameMode != null && mc.gameMode.isDestroying()) HudComponent.revealAll();
        if (player.swinging && player.swingTime == 0) {
            boolean entityHit = mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY;
            if (AutoHudConfig.ON_SWINGING.get() || (AutoHudConfig.ON_ATTACKING.get() && entityHit)) HudComponent.revealAll();
        }

        // targeted block
        if (AutoHudConfig.REVEAL_XP_ON_ENCHANT_BLOCK.get() && mc.hitResult instanceof BlockHitResult bhr
                && bhr.getType() == HitResult.Type.BLOCK && mc.level != null) {
            BlockPos pos = bhr.getBlockPos();
            var state = mc.level.getBlockState(pos);
            if (state.is(Blocks.ENCHANTING_TABLE) || state.is(BlockTags.ANVIL)) XP_LEVEL.reveal();
        }

        // crosshair
        boolean target = mc.hitResult != null && mc.hitResult.getType() != HitResult.Type.MISS;
        if (!target) {
            ItemStack m = player.getMainHandItem(), o = player.getOffhandItem();
            if (m.is(Items.BOW) || m.is(Items.CROSSBOW) || m.is(Items.TRIDENT)
                    || o.is(Items.BOW) || o.is(Items.CROSSBOW) || o.is(Items.TRIDENT)) {
                target = true;
            }
        }
        if (!CROSSHAIR.isActive() || (CROSSHAIR.onChange() && target)) CROSSHAIR.revealCombined();
        else CROSSHAIR.hide();
        prevCrosshairTarget = target;

        // status values
        boolean consumable = policyHelp(player.getMainHandItem());
        int health = Math.round(player.getHealth()), maxHealth = Math.round(player.getMaxHealth());
        boolean heal = consumable && healsFromItem(player.getMainHandItem());
        HEALTH.trackValue(health, maxHealth, heal);
        int food = player.getFoodData().getFoodLevel();
        HUNGER.trackValue(food, 20, consumable && food < 20 && isFood(player.getMainHandItem()));
        AIR.trackValue(player.getAirSupply(), player.getMaxAirSupply(), false);
        int armor = player.getArmorValue();
        boolean armorHeld = false;
        if (consumable) {
            ItemStack main = player.getMainHandItem();
            var eq = main.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
            armorHeld = eq != null && eq.slot().isArmor();
        }
        ARMOR.trackValue(armor, 20, armorHeld);
        XP_LEVEL.trackValue(player.experienceLevel, 0, false);
        XP_BAR.trackValue(player.totalExperience, 0, false);

        // mount
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof LivingEntity living) {
            if (!hadMount) {
                MOUNT_HEALTH.revealCombined();
                mountHealthPrev = -1;
            }
            hadMount = true;
            MOUNT_HEALTH.trackValue((int) living.getHealth(), (int) living.getMaxHealth(), false);
            if (player.getJumpRidingScale() > 0 && AutoHudConfig.MOUNT_JUMP_ON_CHANGE.get()) MOUNT_JUMP.revealNow();
        } else {
            hadMount = false;
        }

        // hotbar
        ItemStack main = player.getMainHandItem(), off = player.getOffhandItem();
        if (prevSlot != -1) {
            if (!ItemStack.matches(main, prevMain)) {
                revealHotbarOnChange(main, prevMain);
                HEALTH.scheduleUpdate(); HUNGER.scheduleUpdate(); ARMOR.scheduleUpdate();
            }
            if (!ItemStack.matches(off, prevOff)) {
                revealHotbarOnChange(off, prevOff);
                HEALTH.scheduleUpdate(); HUNGER.scheduleUpdate(); ARMOR.scheduleUpdate();
            }
            int slot = player.getInventory().getSelectedSlot();
            if (slot != prevSlot && AutoHudConfig.HOTBAR_ON_SLOT_CHANGE.get()) {
                HOTBAR.revealCombined();
                if (AutoHudConfig.XP_WITH_HOTBAR.get()) XP_LEVEL.synchronizeFrom(HOTBAR);
            }
        }
        prevMain = main.copy();
        prevOff = off.copy();
        prevSlot = player.getInventory().getSelectedSlot();
        TOOLTIP.revealIfChanged(false);

        if (AutoHudConfig.HOTBAR_ON_LOW_DURABILITY.get()) {
            if (durabilityLow(main) || durabilityLow(off)) {
                HOTBAR.revealCombined();
                TOOLTIP.revealCombined();
            }
        }

        // tooltip follows main hand changes
        if (TOOLTIP.isActive() && !ItemStack.matches(main, prevMain)) TOOLTIP.revealCombined();

        // status effects
        int effects = player.getActiveEffects().size();
        if (STATUS_EFFECTS.isActive()) {
            if (STATUS_EFFECTS.onChange() && effects != prevEffectCount && prevEffectCount != -1) STATUS_EFFECTS.revealCombined();
        } else {
            STATUS_EFFECTS.reveal();
        }
        prevEffectCount = effects;

        // linking
        if (AutoHudConfig.XP_WITH_HOTBAR.get()) XP_LEVEL.synchronizeFrom(XP_BAR, HOTBAR);

        // ticking
        HudComponent.tickAll();
        CHAT_INDICATOR.tick();
    }

    public static void onChatMessage() {
        if (CHAT.isActive() && CHAT_INDICATOR.isActive() && CHAT.isHidden()) CHAT_INDICATOR.reveal();
    }

    public static boolean cancelEffect() {
        return false;
    }
}
