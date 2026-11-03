package net.diaithir.worldsys.gridkeys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Keybinds that rotate, balance or clear the open crafting grid. */
public final class GridKeysFeature {
    private GridKeysFeature() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, e -> {
            PayloadRegistrar registrar = e.registrar("1");
            registrar.playToServer(GridActionPayload.TYPE, GridActionPayload.CODEC, (payload, ctx) -> {
                if (ctx.player() instanceof ServerPlayer sp) GridOps.run(sp, payload.action());
            });
        });
        if (FMLEnvironment.getDist() == Dist.CLIENT) Client.register(modBus);
    }

    private static final class Client {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath("diaithirworldsys", "gridkeys"));
    private static KeyMapping rotateKey;
    private static KeyMapping balanceKey;
    private static KeyMapping clearKey;

        static void register(IEventBus modBus) {
            rotateKey = new KeyMapping("key.diaithirworldsys.grid_rotate", InputConstants.UNKNOWN.getValue(), CATEGORY);
            balanceKey = new KeyMapping("key.diaithirworldsys.grid_balance", InputConstants.UNKNOWN.getValue(), CATEGORY);
            clearKey = new KeyMapping("key.diaithirworldsys.grid_clear", InputConstants.UNKNOWN.getValue(), CATEGORY);
            modBus.addListener(RegisterKeyMappingsEvent.class, e -> {
                e.register(rotateKey);
                e.register(balanceKey);
                e.register(clearKey);
            });
            NeoForge.EVENT_BUS.addListener(ScreenEvent.KeyPressed.Pre.class, e -> {
                if (!(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>)) return;
                GridAction action = null;
                if (rotateKey.matches(e.getKeyEvent())) action = GridAction.ROTATE;
                else if (balanceKey.matches(e.getKeyEvent())) action = GridAction.BALANCE;
                else if (clearKey.matches(e.getKeyEvent())) action = GridAction.CLEAR;
                if (action == null) return;
                ClientPacketDistributor.sendToServer(new GridActionPayload(action));
                e.setCanceled(true);
            });
        }
    }
}
