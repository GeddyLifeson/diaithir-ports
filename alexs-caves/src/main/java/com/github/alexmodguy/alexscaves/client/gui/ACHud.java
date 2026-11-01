package com.github.alexmodguy.alexscaves.client.gui;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.entity.living.*;
import com.github.alexmodguy.alexscaves.server.entity.util.RidingMeterMount;
import com.github.alexmodguy.alexscaves.server.item.DarknessArmorItem;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** 26.1 port of the HUD overlays from upstream ClientEvents: riding meters, darkness armor meter, irradiated hearts. */
public class ACHud {
    private static final Identifier POTION = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/misc/potion_effect_hud_overlays.png");
    private static final Identifier DINOSAUR = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/misc/dinosaur_hud_overlays.png");
    private static final Identifier ARMOR = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/misc/armor_hud_overlays.png");
    private static final java.util.Random RANDOM = new java.util.Random();

    private static void blit(GuiGraphicsExtractor g, Identifier tex, int x, int y, int u, int v, int w, int h, int tw, int th) {
        if (w <= 0 || h <= 0) return;
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, (float) u, (float) v, w, h, tw, th, 0xFFFFFFFF);
    }

    /** Meters above the hotbar. */
    public static void renderMeters(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || mc.getCameraEntity() != player) return;
        int hudY = 0;
        int sw = g.guiWidth(), sh = g.guiHeight();
        if (player.getVehicle() instanceof RidingMeterMount mount && mount.hasRidingMeter()) {
            int guiY = 53;
            if (player.getArmorValue() > 0 && mount instanceof SubterranodonEntity) guiY += 25;
            int j = sw / 2 - AlexsCaves.CLIENT_CONFIG.subterranodonIndicatorX.get();
            int k = sh - guiY - AlexsCaves.CLIENT_CONFIG.subterranodonIndicatorY.get();
            float inv = 1 - mount.getMeterAmount();
            int vOffset = 0, dinoHeight = 31;
            if (mount instanceof TremorsaurusEntity) { vOffset = 63; k += 5; hudY = 20; }
            else if (mount instanceof AtlatitanEntity) { vOffset = 126; dinoHeight = 32; k += 3; hudY = 40; }
            else if (mount instanceof TremorzillaEntity z) {
                vOffset = 193;
                if (z.isPowered() && !z.isFiring() && z.getSpikesDownAmount() > 0) {
                    if (z.tickCount / 2 % 2 == 1) vOffset = 251;
                    inv = 1F;
                }
                dinoHeight = 29; k += 5; hudY = 20;
            } else if (mount instanceof CandicornEntity) { vOffset = 280; dinoHeight = 25; hudY = 40; k += 4; }
            else hudY = 40;
            blit(g, DINOSAUR, j, k, 0, vOffset + dinoHeight, 43, dinoHeight, 128, 512);
            blit(g, DINOSAUR, j, k, 0, vOffset, 43, (int) Math.floor(dinoHeight * inv), 128, 512);
        }
        if (DarknessArmorItem.hasMeter(player)) {
            ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
            int j = sw / 2 - AlexsCaves.CLIENT_CONFIG.subterranodonIndicatorX.get() + 13;
            int k = sh - 53 - AlexsCaves.CLIENT_CONFIG.subterranodonIndicatorY.get() + 9 - hudY;
            float f = DarknessArmorItem.getMeterProgress(stack);
            int uv = DarknessArmorItem.canChargeUp(stack) && f >= 1.0F ? 0 : 18;
            blit(g, ARMOR, j, k, uv, 19, 18, 19, 128, 128);
            blit(g, ARMOR, j, k, 0, 0, 18, (int) Math.floor(19 * (1 - f)), 128, 128);
        }
    }

    /** Green irradiated hearts drawn over the vanilla health row. */
    public static void renderIrradiatedHearts(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || mc.getCameraEntity() != player || !mc.gameMode.canHurtPlayer()
                || !player.hasEffect(ACEffectRegistry.IRRADIATED)) return;
        int width = g.guiWidth(), height = g.guiHeight();
        int health = Mth.ceil(player.getHealth());
        int tick = mc.gui.getGuiTicks();
        float healthMax = (float) player.getAttributeValue(Attributes.MAX_HEALTH);
        float absorb = Mth.ceil(player.getAbsorptionAmount());
        int rows = Mth.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (rows - 2), 3);
        RANDOM.setSeed(tick * 312871L);
        int left = width / 2 - 91, top = height - 39;
        int regen = player.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION) ? tick % Mth.ceil(healthMax + 5.0F) : -1;
        int heartV = player.level().getLevelData().isHardcore() ? 9 : 0;
        float absorbRemaining = absorb;
        for (int i = Mth.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
            int row = Mth.ceil((float) (i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;
            if (health <= 4) y += RANDOM.nextInt(2);
            if (i == regen) y -= 2;
            blit(g, POTION, x, y, 0, heartV + 18, 9, 9, 32, 32);
            if (absorbRemaining > 0.0F) {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) { blit(g, POTION, x, y, 9, heartV, 9, 9, 32, 32); absorbRemaining -= 1.0F; }
                else { blit(g, POTION, x, y, 0, heartV, 9, 9, 32, 32); absorbRemaining -= 2.0F; }
            } else if (i * 2 + 1 < health) blit(g, POTION, x, y, 0, heartV, 9, 9, 32, 32);
            else if (i * 2 + 1 == health) blit(g, POTION, x, y, 9, heartV, 9, 9, 32, 32);
        }
    }
}
