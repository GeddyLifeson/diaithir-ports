package net.diaithir.tooltips.mixin;

import net.diaithir.tooltips.CultureNames;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** JourneyMap: the minimap's Biome and Dimension info labels also carry the native name. Skipped when the mod is absent. */
@Pseudo
@Mixin(targets = "journeymap.client.ui.theme.ThemeLabelSource", remap = false)
abstract class JourneyMapLabelsMixin {
    @Inject(method = "getBiome()Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true)
    private static void diaithir$biome(CallbackInfoReturnable<Component> cir) {
        var mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        String name = mc.level.getBiome(mc.player.blockPosition()).unwrapKey()
                .map(k -> CultureNames.name("biome", k.identifier().toString())).orElse(null);
        if (name != null) {
            cir.setReturnValue(Component.empty().append(cir.getReturnValue()).append(" · " + name));
        }
    }

    @Inject(method = "getDimension()Lnet/minecraft/network/chat/Component;", at = @At("RETURN"), cancellable = true)
    private static void diaithir$dimension(CallbackInfoReturnable<Component> cir) {
        var mc = Minecraft.getInstance();
        String name = mc.level == null ? null : CultureNames.name("dimension", mc.level.dimension().identifier().toString());
        if (name != null) {
            cir.setReturnValue(Component.empty().append(cir.getReturnValue()).append(" · " + name));
        }
    }
}
