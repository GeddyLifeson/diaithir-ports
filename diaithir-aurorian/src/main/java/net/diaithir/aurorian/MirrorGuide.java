package net.diaithir.aurorian;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;


/**
 * Mirror of Guidance, clean-room: upstream draws a node screen from `data/theaurorian/mirror_of_guidance/*.json` (icon, x, y, children).
 * Here the same data becomes a book that opens on use: one page per node, walked breadth first from the `aurorian` root, using upstream's lang keys.
 */
public final class MirrorGuide {
    private static final Map<Identifier, List<Identifier>> CHILDREN = new LinkedHashMap<>();

    public static SimpleJsonResourceReloadListener listener() {
        return new SimpleJsonResourceReloadListener<JsonElement>(com.mojang.serialization.Codec.PASSTHROUGH.xmap(d -> d.convert(com.mojang.serialization.JsonOps.INSTANCE).getValue(), j -> new com.mojang.serialization.Dynamic<>(com.mojang.serialization.JsonOps.INSTANCE, j)), net.minecraft.resources.FileToIdConverter.json("mirror_of_guidance")) {
            @Override
            protected void apply(Map<Identifier, JsonElement> in, ResourceManager rm, ProfilerFiller p) {
                CHILDREN.clear();
                in.forEach((id, el) -> {
                    JsonObject o = el.getAsJsonObject();
                    List<Identifier> kids = new ArrayList<>();
                    if (o.has("children")) {
                        o.getAsJsonArray("children").forEach(c -> kids.add(Identifier.parse(c.getAsString())));
                    }
                    CHILDREN.put(id, kids);
                });
            }
        };
    }

    public static int nodes() {
        return CHILDREN.size();
    }

    public static void open(Player player, InteractionHand hand) {
        Identifier root = Identifier.fromNamespaceAndPath(DiaithirAurorian.ID, "aurorian");
        List<Identifier> order = new ArrayList<>();
        Set<Identifier> seen = new HashSet<>();
        ArrayDeque<Identifier> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            Identifier id = q.poll();
            if (CHILDREN.containsKey(id) && seen.add(id)) {
                order.add(id);
                q.addAll(CHILDREN.get(id));
            }
        }
        if (order.isEmpty()) {
            return;
        }
        // vanilla only opens a book screen from a real book item, so each use shows the next entry in chat; sneaking restarts
        ItemStack mirror = player.getItemInHand(hand);
        int page = player.isShiftKeyDown() ? 0 : mirror.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("page", 0) % order.size();
        final int next = (page + 1) % order.size();
        CustomData.update(DataComponents.CUSTOM_DATA, mirror, tag -> tag.putInt("page", next));
        Identifier id = order.get(page);
        String key = "mirror_of_guidance." + id.getNamespace() + "." + id.getPath();
        player.sendSystemMessage(Component.translatable(key + ".name").withStyle(net.minecraft.ChatFormatting.BOLD, net.minecraft.ChatFormatting.AQUA)
            .append(Component.literal("  (" + (page + 1) + "/" + order.size() + ")").withStyle(net.minecraft.ChatFormatting.GRAY)));
        player.sendSystemMessage(Component.translatable(key + ".desc"));
    }

    private MirrorGuide() {}
}
