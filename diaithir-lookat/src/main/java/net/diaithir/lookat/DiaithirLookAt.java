package net.diaithir.lookat;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@Mod(value = DiaithirLookAt.ID, dist = Dist.CLIENT)
public final class DiaithirLookAt {
    public static final String ID = "diaithirlookat";

    public DiaithirLookAt(IEventBus bus) {
        bus.addListener((RegisterGuiLayersEvent e) -> e.registerAboveAll(Identifier.fromNamespaceAndPath(ID, "box"), DiaithirLookAt::render));
    }

    private static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui || mc.screen != null) {
            return;
        }
        String[] lines = lines(mc);
        if (lines == null) {
            return;
        }
        int w = 0;
        for (String l : lines) {
            w = Math.max(w, mc.font.width(l));
        }
        int x = (mc.getWindow().getGuiScaledWidth() - w) / 2 - 3, y = 4;
        g.fill(x, y, x + w + 6, y + lines.length * 10 + 4, 0x90000000);
        for (int i = 0; i < lines.length; i++) {
            g.text(mc.font, lines[i], x + 3, y + 3 + i * 10, i == 0 ? 0xFFFFFFFF : 0xFFAAAAAA);
        }
    }

    private static String[] lines(Minecraft mc) {
        HitResult hit = mc.hitResult;
        if (mc.crosshairPickEntity != null) {
            Entity e = mc.crosshairPickEntity;
            return Info.forEntity(e, BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace());
        }
        if (hit instanceof BlockHitResult b && hit.getType() == HitResult.Type.BLOCK) {
            var state = mc.level.getBlockState(b.getBlockPos());
            return Info.forBlock(state, state.getBlock().getName().getString(), BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
        }
        return null;
    }
}
