package net.diaithir.claims.compass;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Structure Compass: use it to name the nearest structure of the chosen kind; sneak and use to choose the next kind.
 * Clean-room, written from how structure-finding compasses behave.
 */
public final class DiaithirCompass {
    public static final String ID = "diaithircompass";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredItem<Item> COMPASS = ITEMS.registerItem("structure_compass", CompassItem::new, () -> new Item.Properties().stacksTo(1));

    public DiaithirCompass(IEventBus bus) {
        ITEMS.register(bus);
    }

    /** Structure ids in a stable order. */
    static List<net.minecraft.resources.Identifier> kinds(ServerLevel level) {
        List<net.minecraft.resources.Identifier> out = new ArrayList<>();
        level.registryAccess().lookupOrThrow(Registries.STRUCTURE).listElementIds().forEach(k -> out.add(k.identifier()));
        out.sort(Comparator.comparing(net.minecraft.resources.Identifier::toString));
        return out;
    }

    static final class CompassItem extends Item {
        CompassItem(Properties p) {
            super(p);
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (!(level instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.SUCCESS;
            }
            List<net.minecraft.resources.Identifier> kinds = kinds(sl);
            if (kinds.isEmpty()) {
                return InteractionResult.FAIL;
            }
            int idx = Math.floorMod(Selection.get(sp), kinds.size());
            if (sp.isShiftKeyDown()) {
                idx = (idx + 1) % kinds.size();
                Selection.set(sp, idx);
                sp.sendSystemMessage(Component.literal("Looking for: " + kinds.get(idx)));
                return InteractionResult.CONSUME;
            }
            var reg = sl.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            var holder = reg.get(net.minecraft.resources.ResourceKey.create(Registries.STRUCTURE, kinds.get(idx)));
            if (holder.isEmpty()) {
                return InteractionResult.FAIL;
            }
            var found = sl.getChunkSource().getGenerator().findNearestMapStructure(sl, HolderSet.direct(holder.get()), sp.blockPosition(), 100, false);
            if (found == null) {
                sp.sendSystemMessage(Component.literal("No " + kinds.get(idx) + " within range."));
                return InteractionResult.FAIL;
            }
            BlockPos at = found.getFirst();
            int dx = at.getX() - sp.getBlockX(), dz = at.getZ() - sp.getBlockZ();
            sp.sendSystemMessage(Component.literal(kinds.get(idx) + ": " + Math.round(Math.sqrt((double) dx * dx + (double) dz * dz)) + " m " + Selection.dir(dx, dz) + " (" + at.getX() + ", " + at.getZ() + ")"));
            return InteractionResult.CONSUME;
        }
    }
}
