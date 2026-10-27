package net.diaithir.tooltips.jade;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import net.diaithir.tooltips.ClientHook;
import net.diaithir.tooltips.CultureNames;
import net.diaithir.tooltips.DiaithirTooltips;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Jade: on a room's anchor block (furnace, tables, bookshelf, lectern, bell ...) say whether it sits in a valid building room.
 * The server reads kubejs/data/tech_rooms_found.json (written by the Ages tech engine: culture, settlement, building, codex, volume,
 * min x/y/z and max x/y/z) every ~10 s; blocks outside every listed room get the hint. Nothing shows when the file is absent
 * or lists no bounds.
 */
public final class RoomLine implements IServerDataProvider<BlockAccessor> {
    static final Identifier UID = Identifier.fromNamespaceAndPath(DiaithirTooltips.ID, "room");
    private static final Path FOUND = Path.of("kubejs/data/tech_rooms_found.json");
    private static final Set<String> ANCHORS = Set.of("furnace", "blast_furnace", "smoker", "bookshelf", "chiseled_bookshelf", "lectern", "bell", "enchanting_table", "brewing_stand", "loom", "composter");
    private static JsonArray cache;
    private static long loaded;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor a) {
        if (!anchor(a.getBlock())) {
            return;
        }
        JsonArray found = found();
        if (found == null) {
            return;
        }
        var pos = a.getPosition();
        String dim = a.getLevel().dimension().identifier().toString();
        for (var el : found) {
            JsonObject r = el.getAsJsonObject();
            if (!r.has("maxX")) {
                return;
            }
            if (r.has("dimension") && !r.get("dimension").getAsString().equals(dim)) {
                continue;
            }
            if (pos.getX() >= r.get("x").getAsInt() - 1 && pos.getX() <= r.get("maxX").getAsInt() + 1
                    && pos.getY() >= r.get("y").getAsInt() - 1 && pos.getY() <= r.get("maxY").getAsInt() + 1
                    && pos.getZ() >= r.get("z").getAsInt() - 1 && pos.getZ() <= r.get("maxZ").getAsInt() + 1) {
                data.putString("building", r.get("building").getAsString());
                return;
            }
        }
        data.putString("building", "");
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    private static boolean anchor(Block block) {
        String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return ANCHORS.contains(path) || path.endsWith("_table");
    }

    private static JsonArray found() {
        long now = System.currentTimeMillis();
        if (now - loaded > 10_000) {
            loaded = now;
            try {
                cache = Files.exists(FOUND) ? JsonParser.parseString(Files.readString(FOUND)).getAsJsonArray() : null;
            } catch (Exception e) {
                cache = null;
            }
        }
        return cache;
    }

    /** The client half: Jade 1.21.6+ keeps data providers and component providers apart. */
    public static final class Client implements IComponentProvider<BlockAccessor> {
        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor a, IPluginConfig config) {
            var data = a.getServerData();
            if (!data.contains("building")) {
                return;
            }
            String building = data.getStringOr("building", "");
            if (building.isEmpty()) {
                tooltip.add(Component.literal("Not a building yet: use /ages tech room"));
                return;
            }
            tooltip.add(Component.literal("Counts as a " + building));
            String native_ = CultureNames.phrase(building);
            if (native_ != null) {
                tooltip.add(Component.literal(native_).withStyle(ClientHook.STYLE));
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
