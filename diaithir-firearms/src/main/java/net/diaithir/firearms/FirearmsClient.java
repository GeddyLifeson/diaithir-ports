package net.diaithir.firearms;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Reload key (R), scope zoom while sneaking with a sight fitted, and tooltips. */
public final class FirearmsClient {
    private static KeyMapping reload;
    private static final java.util.Map<String, String> NATIVE = loadNames();

    @SuppressWarnings("unchecked")
    private static java.util.Map<String, String> loadNames() {
        try (var r = new java.io.InputStreamReader(FirearmsClient.class.getResourceAsStream("/names.json"))) {
            return new com.google.gson.Gson().fromJson(r, java.util.Map.class);
        } catch (Exception e) { return java.util.Map.of(); }
    }

    static void init(IEventBus bus) {
        bus.addListener(RegisterKeyMappingsEvent.class, e -> {
            reload = new KeyMapping("key." + DiaithirFirearms.ID + ".reload", InputConstants.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_R,
                    KeyMapping.Category.register(Identifier.fromNamespaceAndPath(DiaithirFirearms.ID, "main")));
            e.register(reload);
        });
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, e -> {
            Minecraft mc = Minecraft.getInstance();
            if (reload == null || mc.player == null || mc.screen != null) return;
            while (reload.consumeClick()) ClientPacketDistributor.sendToServer(new Net.Reload());
        });
        NeoForge.EVENT_BUS.addListener(ComputeFovModifierEvent.class, e -> {
            var st = e.getPlayer().getMainHandItem();
            if (!(st.getItem() instanceof GunItem) || !e.getPlayer().isShiftKeyDown()) return;
            float z = Engine.zoom(st);
            if (z > 1f) e.setNewFovModifier(e.getNewFovModifier() / z);
        });
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, e -> {
            if (!(e.getItemStack().getItem() instanceof GunItem gi)) return;
            var g = gi.gun;
            var t = e.getToolTip();
            t.add(Component.literal(g.cls() + " | " + String.format("%.1f hp x%d | %d rpm | ", g.damage(), g.pellets(), 1200 / Math.max(1, g.cooldown()))
                    + (g.auto() ? "auto" : "semi")));
            t.add(Component.translatable("diaithirfirearms.mag").append(": " + Engine.mag(e.getItemStack()) + "/" + g.mag()));
            t.add(Component.translatable("diaithirfirearms.age", g.age()));
        });
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, e -> {
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItemStack().getItem());
            if (!id.getNamespace().equals(DiaithirFirearms.ID)) return;
            String n = NATIVE.get(id.getPath());
            if (n != null) e.getToolTip().add(1, Component.literal(n).withStyle(net.minecraft.ChatFormatting.GRAY, net.minecraft.ChatFormatting.ITALIC));
        });
    }

    private FirearmsClient() {}
}
