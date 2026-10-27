package net.diaithir.tooltips;

import com.mojang.serialization.Codec;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.function.Supplier;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Both sides: stores the player's starting culture (a slug from culture_starts.csv) and syncs it to the client. */
@Mod("diaithirtooltips")
public final class DiaithirTooltips {
    public static final String ID = "diaithirtooltips";
    public static final String DEFAULT_CULTURE = "diaithir";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ID);
    /** Empty means not chosen yet, which reads as Diaithir. The culture picker sets it with {@code player.setData(CULTURE, slug)} or {@code /culture set}. */
    public static final Supplier<AttachmentType<String>> CULTURE = ATTACHMENTS.register("culture", () -> AttachmentType.builder(() -> "")
            .serialize(Codec.STRING.fieldOf("slug"))
            .sync(ByteBufCodecs.STRING_UTF8)
            .copyOnDeath()
            .build());

    public DiaithirTooltips(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, DiaithirTooltips::commands);
    }

    private static void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("culture")
                .then(Commands.literal("get").executes(c -> {
                    String slug = c.getSource().getPlayerOrException().getData(CULTURE);
                    c.getSource().sendSuccess(() -> Component.literal(slug.isEmpty() ? DEFAULT_CULTURE + " (not chosen)" : slug), false);
                    return 1;
                }))
                .then(Commands.literal("set").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.argument("slug", StringArgumentType.word()).executes(c -> {
                                    String slug = StringArgumentType.getString(c, "slug");
                                    if (!slug.matches("[a-z0-9_]{1,80}")) {
                                        c.getSource().sendFailure(Component.literal("A culture slug is lowercase letters, digits and underscores."));
                                        return 0;
                                    }
                                    var players = EntityArgument.getPlayers(c, "players");
                                    players.forEach(p -> p.setData(CULTURE, slug));
                                    c.getSource().sendSuccess(() -> Component.literal("Culture set to " + slug + " for " + players.size() + " player(s)"), true);
                                    return players.size();
                                })))));
    }
}
