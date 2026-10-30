package com.github.alexmodguy.alexscaves.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.entity.SubmarineRenderer;
import com.github.alexmodguy.alexscaves.server.entity.item.SubmarineEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.PossessesCamera;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Set;

/** Whole-screen post effects (submarine floodlights, watcher/darkness view, sugar rush saturation) as 26.1 post_effect chains. */
public class ACPostEffects {
    public static final Identifier SUBMARINE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "submarine_light");
    public static final Identifier WATCHER = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "watcher_perspective");
    public static final Identifier SUGAR_RUSH = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "sugar_rush");
    private static final Set<Identifier> OURS = Set.of(SUBMARINE, WATCHER, SUGAR_RUSH);

    public static void tick(Minecraft mc) {
        if (mc.level == null) {
            return;
        }
        GameRenderer renderer = mc.gameRenderer;
        Entity camera = mc.getCameraEntity();
        boolean firstPerson = mc.options.getCameraType().isFirstPerson();
        Identifier wanted = null;
        if (camera != null) {
            if (firstPerson && camera.isPassenger() && camera.getVehicle() instanceof SubmarineEntity submarine && SubmarineRenderer.isFirstPersonFloodlightsMode(submarine)) {
                wanted = SUBMARINE;
            } else if ((firstPerson && camera instanceof PossessesCamera) || (camera instanceof LivingEntity afflicted && afflicted.hasEffect(ACEffectRegistry.DARKNESS_INCARNATE))) {
                wanted = WATCHER;
            } else if (camera instanceof LivingEntity afflicted && afflicted.hasEffect(ACEffectRegistry.SUGAR_RUSH) && AlexsCaves.CLIENT_CONFIG.sugarRushSaturationEffect.get()) {
                wanted = SUGAR_RUSH;
            }
        }
        Identifier current = renderer.currentPostEffect();
        if (wanted != null) {
            if (!wanted.equals(current)) {
                renderer.setPostEffect(wanted);
            }
        } else if (current != null && OURS.contains(current)) {
            renderer.clearPostEffect();
        }
    }
}
