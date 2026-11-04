package dev.shadowsoffire.apothic_enchanting.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.TreeMap;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import dev.shadowsoffire.apothic_enchanting.EnchantmentInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Dumps the current effective {@code apothic_enchanting:enchantment_info} state — datamap entries plus computed
 * fallbacks for unconfigured enchantments — to a datapack-ready JSON file in the game directory.
 * <p>
 * The output is shaped exactly like a NeoForge data map ({@code {"values": {"<id>": <info>, ...}}}) so it can be
 * dropped into a datapack at {@code data/apothic_enchanting/data_maps/enchantment/enchantment_info.json} without
 * any further editing.
 */
public class DumpEnchantmentInfoCommand {

    public static final String DUMP_FILE_NAME = "apothic_enchanting_enchantment_info_dump.json";

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("dump_enchantment_info")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .executes(c -> dump(c.getSource())));
    }

    private static int dump(CommandSourceStack source) {
        try {
            var registry = source.getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

            // TreeMap so the dump is alphabetically ordered for diffability.
            var values = new TreeMap<String, JsonElement>();
            registry.listElements().forEach(ref -> {
                Identifier id = ref.key().identifier();
                Holder<Enchantment> holder = ref;
                EnchantmentInfo info = ApothicEnchanting.getEnchInfo(holder);
                // Concretize the optional level fields so the dump records the effective values rather than dropping
                // the fields entirely for fallback-using enchantments.
                // Users editing the dump should see the actual numbers in play.
                int resolvedMax = info.maxLevel().orElseGet(() -> ApothicEnchanting.getDefaultMaxLevel(holder));
                int resolvedMaxLoot = info.maxLootLevel().orElseGet(() -> holder.value().getMaxLevel());
                EnchantmentInfo concrete = new EnchantmentInfo(
                    Optional.of(resolvedMax),
                    Optional.of(resolvedMaxLoot),
                    Optional.empty(),
                    info.levelCap(),
                    info.maxPower(),
                    info.minPower());
                EnchantmentInfo.CODEC.codec().encodeStart(JsonOps.INSTANCE, concrete)
                    .resultOrPartial(err -> ApothicEnchanting.LOGGER.warn("Failed to encode EnchantmentInfo for {}: {}", id, err))
                    .ifPresent(json -> values.put(id.toString(), json));
            });

            JsonObject valuesObj = new JsonObject();
            values.forEach(valuesObj::add);

            JsonObject root = new JsonObject();
            root.add("values", valuesObj);

            Path file = source.getServer().getServerDirectory().resolve(DUMP_FILE_NAME);
            Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root));

            String path = file.toAbsolutePath().toString();
            Component link = Component.literal(path).withStyle(s -> s
                .withColor(ChatFormatting.AQUA)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.OpenFile(path)));
            source.sendSuccess(() -> Component.translatable("commands.apothic_enchanting.dump.success", values.size(), link), true);
            return values.size();
        }
        catch (IOException ex) {
            ApothicEnchanting.LOGGER.error("Failed to write enchantment info dump", ex);
            source.sendFailure(Component.translatable("commands.apothic_enchanting.dump.failure", ex.getMessage()));
            return 0;
        }
    }

}
