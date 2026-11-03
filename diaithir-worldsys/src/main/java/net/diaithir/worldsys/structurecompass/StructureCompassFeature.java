package net.diaithir.worldsys.structurecompass;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;

/** Wayfinder compass: right click finds the selected structure type, sneak right click cycles the type. */
public final class StructureCompassFeature {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("diaithirworldsys");
    public static final DeferredItem<Item> WAYFINDER = ITEMS.registerItem("wayfinder_compass", WayfinderCompassItem::new, p -> p.stacksTo(1));

    static final List<String> TARGETS = List.of("village", "stronghold", "mineshaft", "shipwreck", "ruined_portal", "pillager_outpost", "ancient_city", "trial_chambers");
    static final String TAG = "wayfinder";

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, e -> {
            if (e.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) e.accept(WAYFINDER.get());
        });
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, e -> {
            if (e.getEntity() instanceof ServerPlayer sp && sp.tickCount % 10 == 0) hint(sp);
        });
    }

    private static void hint(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem().is(WAYFINDER.get()) ? player.getMainHandItem()
                : player.getOffhandItem().is(WAYFINDER.get()) ? player.getOffhandItem() : ItemStack.EMPTY;
        if (stack.isEmpty()) return;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompoundOrEmpty(TAG);
        String name = TARGETS.get(Math.floorMod(tag.getIntOr("index", 0), TARGETS.size()));
        if (!tag.contains("x") || !tag.getStringOr("dim", "").equals(player.level().dimension().identifier().toString())) {
            player.sendOverlayMessage(Component.literal(label(name) + ": right click to search"));
            return;
        }
        BlockPos target = new BlockPos(tag.getIntOr("x", 0), 0, tag.getIntOr("z", 0));
        double dx = target.getX() + 0.5 - player.getX();
        double dz = target.getZ() + 0.5 - player.getZ();
        int dist = (int) Math.sqrt(dx * dx + dz * dz);
        player.sendOverlayMessage(Component.literal(label(name) + " " + arrow(dx, dz) + " " + dist + " blocks"));
    }

    static String label(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1).replace('_', ' ');
    }

    private static String arrow(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        String[] dirs = {"E", "SE", "S", "SW", "W", "NW", "N", "NE"};
        return dirs[Math.floorMod(Math.round(angle / 45.0), 8)];
    }

    static InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;
        CompoundTag root = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag tag = root.getCompoundOrEmpty(TAG);
        int index = Math.floorMod(tag.getIntOr("index", 0), TARGETS.size());
        if (player.isShiftKeyDown()) {
            index = (index + 1) % TARGETS.size();
            tag = new CompoundTag();
            tag.putInt("index", index);
            root.put(TAG, tag);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
            sp.sendOverlayMessage(Component.literal("Target: " + label(TARGETS.get(index))));
            return InteractionResult.CONSUME;
        }
        Identifier id = Identifier.withDefaultNamespace(TARGETS.get(index));
        Optional<Holder.Reference<Structure>> holder = server.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(ResourceKey.create(Registries.STRUCTURE, id));
        BlockPos found = holder.map(h -> server.getChunkSource().getGenerator().findNearestMapStructure(server, HolderSet.direct(h), sp.blockPosition(), 100, false))
                .map(pair -> pair == null ? null : pair.getFirst()).orElse(null);
        if (found == null) {
            sp.sendOverlayMessage(Component.literal("No " + label(TARGETS.get(index)).toLowerCase() + " found nearby."));
        } else {
            tag = new CompoundTag();
            tag.putInt("index", index);
            tag.putInt("x", found.getX());
            tag.putInt("z", found.getZ());
            tag.putString("dim", server.dimension().identifier().toString());
            root.put(TAG, tag);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
            sp.sendOverlayMessage(Component.literal("Located " + label(TARGETS.get(index)).toLowerCase() + "."));
        }
        sp.getCooldowns().addCooldown(stack, 40);
        return InteractionResult.CONSUME;
    }
}
