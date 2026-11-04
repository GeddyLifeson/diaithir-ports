// Ported from Chat Heads (MPL-2.0) https://github.com/dzwdz/chat_heads
// Modifications: NeoForge-only entry point; ModConfigSpec config replaces Cloth Config/AutoConfig; no mixins are used.
package net.diaithir.chatclient.chatheads;

import net.diaithir.chatclient.chatheads.client.ChatHeadsClient;
import net.diaithir.chatclient.chatheads.config.ChatHeadsConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

// NOTE: no mixin is used by this port. Heads are inserted through ClientChatReceivedEvent, so the purely visual upstream
// tweaks that need glyph/GUI internals (3Dness, right padding, shadow toggle, offset of non-player text, command
// suggestion heads, blended head textures, Chat Patches / Cloth Config compat) are intentionally not ported and no
// mixins.json is emitted.
public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        container.registerConfig(ModConfig.Type.CLIENT, ChatHeadsConfig.SPEC, "diaithir_chat_chatheads-client.toml");
        ChatHeadsClient.init(modBus);
    }
}
