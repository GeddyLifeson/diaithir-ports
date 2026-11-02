package net.diaithir.upgrades;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentType;

/**
 * Entry points for other modules. A block entity gets a host automatically when a Target rule
 * matches its block; no code needed on its side.
 * <pre>
 *   Upgrades.insert(be, module, Quality.NORMAL)   // install a module, false if full or not allowed
 *   Upgrades.remove(be, slot)
 *   Upgrades.host(be).ifPresent(h -> progress += h.progressPerTick());
 * </pre>
 */
public final class Upgrades {
    public static final TargetRegistry TARGETS = new TargetRegistry();

    public static final AttachmentType<SlotData> SLOTS =
            AttachmentType.builder(() -> SlotData.EMPTY).serialize(SlotData.CODEC).build();

    private Upgrades() {}

    public static Optional<Target> targetOf(BlockEntity be) {
        String id = BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock()).toString();
        Set<String> tags = new HashSet<>();
        be.getBlockState().getBlock().builtInRegistryHolder().tags().forEach(t -> tags.add("#" + t.location()));
        return TARGETS.find(id, tags);
    }

    public static Optional<UpgradeHost> host(BlockEntity be) {
        return targetOf(be).map(t -> new Bound(be, t));
    }

    public static boolean insert(BlockEntity be, ModuleSpec m, Quality q) {
        return host(be).map(h -> h.slots().install(m, q) && save(be, h.slots())).orElse(false);
    }

    public static boolean remove(BlockEntity be, int slot) {
        return host(be).map(h -> h.slots().remove(slot) && save(be, h.slots())).orElse(false);
    }

    private static boolean save(BlockEntity be, Slotted s) {
        be.setData(SLOTS, SlotData.of(s));
        be.setChanged();
        return true;
    }

    /** Host view over a block entity's attachment. */
    private record Bound(BlockEntity be, Target target, Slotted slots) implements UpgradeHost {
        Bound(BlockEntity be, Target target) {
            this(be, target, be.getData(SLOTS).toSlotted(target));
        }
    }
}
