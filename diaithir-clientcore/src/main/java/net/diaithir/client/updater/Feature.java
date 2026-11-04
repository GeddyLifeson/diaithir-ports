package net.diaithir.client.updater;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Clean-room updater entry: /diaithirupdate check|download &lt;https manifest url&gt;. Off until a player runs it. */
public final class Feature {
    private Feature() {}

    public static void init(IEventBus modBus, ModContainer container) {
        if (FMLEnvironment.getDist() != Dist.CLIENT) return;
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, e ->
                e.getDispatcher().register(Commands.literal("diaithirupdate")
                        .then(Commands.literal("check").then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(c -> run(StringArgumentType.getString(c, "url"), false))))
                        .then(Commands.literal("download").then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(c -> run(StringArgumentType.getString(c, "url"), true))))));
    }

    private static int run(String url, boolean download) {
        Thread t = new Thread(() -> {
            try {
                Updater u = new Updater(FMLPaths.GAMEDIR.get());
                Manifest m = u.fetch(url);
                List<Manifest.Step> plan = m.plan(u.localHashes());
                if (plan.isEmpty()) {
                    say("Pack " + m.version() + ": already up to date.");
                } else if (!download) {
                    say("Pack " + m.version() + ": " + plan.size() + " file(s) to update. Run /diaithirupdate download <url> to stage them.");
                } else {
                    int kept = u.stage(plan);
                    say("Staged " + kept + "/" + plan.size() + " verified file(s) in diaithir-update/staged. Move them over the game folder with the launcher closed.");
                }
            } catch (Exception ex) {
                say("Update check failed: " + ex.getMessage());
            }
        }, "diaithir-updater");
        t.setDaemon(true);
        t.start();
        return Command.SINGLE_SUCCESS;
    }

    private static void say(String msg) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) mc.player.sendSystemMessage(Component.literal(msg));
        });
    }
}
