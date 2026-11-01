package net.diaithir.combat;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Client side. Animation: vanilla arm swing, driven by the server's swing packet. PlayerAnimationLib keyframes are not
 * wired (see README); this class only intercepts the attack key, sends the roll key and adds a tooltip line.
 */
public final class CombatClient {
    private static KeyMapping roll;

    static void init(IEventBus bus) {
        bus.addListener(RegisterKeyMappingsEvent.class, e -> {
            roll = new KeyMapping("key.diaithircombat.roll", InputConstants.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_V,
                    KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath(DiaithirCombat.ID, "main")));
            e.register(roll);
        });
        NeoForge.EVENT_BUS.addListener(InputEvent.InteractionKeyMappingTriggered.class, CombatClient::attackKey);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, CombatClient::tick);
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, CombatClient::tooltip);
    }

    private static void attackKey(InputEvent.InteractionKeyMappingTriggered e) {
        Minecraft mc = Minecraft.getInstance();
        if (!e.isAttack() || mc.player == null || !CombatConfig.ENABLED.getAsBoolean()) return;
        if (Cat.melee(mc.player.getMainHandItem()) == null) return;
        ClientPacketDistributor.sendToServer(new Net.Swing());
        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) {
            e.setCanceled(true);
            e.setSwingHand(false);
        }
    }

    private static void tick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (roll == null || mc.player == null || mc.screen != null) return;
        while (roll.consumeClick()) {
            var in = mc.player.input.keyPresses;
            int dir = in.backward() ? 1 : in.left() ? 2 : in.right() ? 3 : 0;
            ClientPacketDistributor.sendToServer(new Net.Roll(dir));
        }
    }

    private static void tooltip(ItemTooltipEvent e) {
        Cat c = Cat.of(e.getItemStack());
        if (c == null) return;
        if (c.ranged) {
            e.getToolTip().add(Component.translatable("diaithircombat.category." + c.id).append(" - ")
                    .append(Component.translatable("diaithircombat.draw")).append(" " + c.drawTicks));
        } else {
            e.getToolTip().add(Component.translatable("diaithircombat.category." + c.id).append(" - ")
                    .append(Component.translatable("diaithircombat.reach")).append(String.format(" %.1f, ", c.range))
                    .append(Component.translatable("diaithircombat.arc")).append(String.format(" %.0f, ", c.arc))
                    .append(Component.translatable("diaithircombat.combo")).append(" " + c.combo.size()));
        }
    }

    private CombatClient() {}
}
