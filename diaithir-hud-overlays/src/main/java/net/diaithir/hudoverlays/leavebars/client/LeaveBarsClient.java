// Ported from Leave My Bars Alone (MPL-2.0) https://github.com/Fuzss/leavemybarsalone
// Modifications: GUI layer registered via RegisterGuiLayersEvent; food bar drawn directly with vanilla sprites since Gui methods are private.
package net.diaithir.hudoverlays.leavebars.client;

import net.diaithir.hudoverlays.leavebars.config.LeaveBarsConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class LeaveBarsClient {
    public static final Identifier FOOD_LEVEL_LAYER = Identifier.fromNamespaceAndPath("diaithirhudoverlays", "leavebars_food_level");

    private static final Identifier FOOD_EMPTY_HUNGER = Identifier.withDefaultNamespace("hud/food_empty_hunger");
    private static final Identifier FOOD_HALF_HUNGER = Identifier.withDefaultNamespace("hud/food_half_hunger");
    private static final Identifier FOOD_FULL_HUNGER = Identifier.withDefaultNamespace("hud/food_full_hunger");
    private static final Identifier FOOD_EMPTY = Identifier.withDefaultNamespace("hud/food_empty");
    private static final Identifier FOOD_HALF = Identifier.withDefaultNamespace("hud/food_half");
    private static final Identifier FOOD_FULL = Identifier.withDefaultNamespace("hud/food_full");

    private LeaveBarsClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterGuiLayersEvent.class, e ->
                e.registerAbove(VanillaGuiLayers.VEHICLE_HEALTH, FOOD_LEVEL_LAYER, LeaveBarsClient::renderFoodLayer));
    }

    private static int vehicleMaxHearts(Player player) {
        if (player.getVehicle() instanceof LivingEntity living && living.showVehicleHealth()) {
            int hearts = (int) (living.getMaxHealth() + 0.5F) / 2;
            return Math.min(hearts, 30);
        }
        return 0;
    }

    private static void renderFoodLayer(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        if (!LeaveBarsConfig.FOOD_LEVEL.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameMode == null || !mc.gameMode.canHurtPlayer()) {
            return;
        }
        if (!(mc.getCameraEntity() instanceof Player player)) {
            return;
        }
        if (vehicleMaxHearts(player) <= 0) {
            return;
        }
        Gui gui = mc.gui;
        int y = g.guiHeight() - gui.rightHeight;
        int xRight = g.guiWidth() / 2 + 91;
        renderFood(g, player, y, xRight);
        gui.rightHeight += 10;
    }

    private static void renderFood(GuiGraphicsExtractor g, Player player, int yBase, int xRight) {
        FoodData food = player.getFoodData();
        int level = food.getFoodLevel();
        boolean hunger = player.hasEffect(MobEffects.HUNGER);
        Identifier empty = hunger ? FOOD_EMPTY_HUNGER : FOOD_EMPTY;
        Identifier half = hunger ? FOOD_HALF_HUNGER : FOOD_HALF;
        Identifier full = hunger ? FOOD_FULL_HUNGER : FOOD_FULL;
        for (int i = 0; i < 10; i++) {
            int y = yBase;
            if (food.getSaturationLevel() <= 0.0F && player.tickCount % (level * 3 + 1) == 0) {
                y = yBase + (player.getRandom().nextInt(3) - 1);
            }
            int x = xRight - i * 8 - 9;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, empty, x, y, 9, 9);
            if (i * 2 + 1 < level) {
                g.blitSprite(RenderPipelines.GUI_TEXTURED, full, x, y, 9, 9);
            } else if (i * 2 + 1 == level) {
                g.blitSprite(RenderPipelines.GUI_TEXTURED, half, x, y, 9, 9);
            }
        }
    }
}
