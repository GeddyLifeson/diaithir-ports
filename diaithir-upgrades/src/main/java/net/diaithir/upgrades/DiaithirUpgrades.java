package net.diaithir.upgrades;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(DiaithirUpgrades.ID)
public final class DiaithirUpgrades {
    public static final String ID = "diaithirupgrades";

    private static final DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ID);

    public DiaithirUpgrades(IEventBus bus) {
        ATTACHMENTS.register("slots", () -> Upgrades.SLOTS);
        ATTACHMENTS.register(bus);
        bus.addListener(RegisterCapabilitiesEvent.class, DiaithirUpgrades::registerCaps);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class,
                e -> e.addListener(Identifier.fromNamespaceAndPath(ID, "targets"), new Rules()));
    }

    /** Every block entity type answers the host capability; it is empty unless a rule matches. */
    private static void registerCaps(RegisterCapabilitiesEvent e) {
        for (var type : BuiltInRegistries.BLOCK_ENTITY_TYPE) {
            e.registerBlockEntity(UpgradeHost.CAP, type, (be, ctx) -> Upgrades.host(be).orElse(null));
        }
    }

    /** Reads data/&lt;ns&gt;/upgrades/targets/*.json from every loaded pack. */
    private static final class Rules extends SimpleJsonResourceReloadListener<JsonElement> {
        Rules() {
            super(ExtraCodecs.JSON, FileToIdConverter.json("upgrades/targets"));
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> files, ResourceManager rm, ProfilerFiller p) {
            Upgrades.TARGETS.clear();
            files.values().forEach(f -> {
                if (f.isJsonObject()) RuleLoader.parse(f.getAsJsonObject()).forEach(Upgrades.TARGETS::add);
            });
        }
    }
}
