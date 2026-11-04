package net.diaithir.bridges;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decides whether a storage cell or disk holds anything, from the data components the owning mod puts on the stack (AE2 keeps a cell's stored
 * items in ae2:storage_cell_inv; a Refined Storage disk only carries a reference to world storage, so any reference counts as used). No hard
 * dependency on either mod.
 */
public final class CellGuard {
    private CellGuard() {}

    static Set<Identifier> configured() {
        var list = Config.SPEC.isLoaded() ? Config.GUARD_COMPONENTS.get() : Config.GUARD_COMPONENTS.getDefault();
        return list.stream().map(Identifier::tryParse).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
    }

    /** True when the stack carries any of the given components with content. A collection counts only when it is not empty. */
    public static boolean hasContents(ItemStack stack, Set<Identifier> components) {
        for (var entry : stack.getComponents()) {
            DataComponentType<?> type = entry.type();
            Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
            if (id == null || !components.contains(id)) continue;
            Object v = entry.value();
            if (v instanceof Collection<?> c) { if (!c.isEmpty()) return true; }
            else if (v instanceof Optional<?> o) { if (o.isPresent()) return true; }
            else return true;
        }
        return false;
    }

    public static boolean isEmpty(ItemStack stack) {
        return !hasContents(stack, configured());
    }
}
