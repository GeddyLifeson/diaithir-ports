package net.diaithir.foundry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Tag-driven melt and cast lookups. The recipe set is derived live from item tags, so it covers every metal found at datapack load. */
public final class MoltenMetal {
    public record Melt(String metal, int units) {
    }

    private MoltenMetal() {
    }

    /** What a stack melts into, or empty. */
    public static Optional<Melt> melt(ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        for (String metal : MetalRegistry.discover()) {
            for (String group : MetalRegistry.meltGroups()) {
                if (stack.is(MetalRegistry.tag(group, metal))) return Optional.of(new Melt(metal, MetalRegistry.meltYield(group)));
            }
        }
        return Optional.empty();
    }

    /** The item a cast of the given shape produces for a metal, or empty when no tagged item exists. */
    public static ItemStack cast(String metal, Shape shape) {
        String group = switch (shape) {
            case INGOT -> "ingots";
            case NUGGET -> "nuggets";
            case PLATE -> "plates";
            case ROD -> "rods";
            case GEAR -> "gears";
            case BLOCK -> "storage_blocks";
        };
        var tag = MetalRegistry.tag(group, metal);
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) return new ItemStack(item.value());
        if (shape == Shape.PLATE) return new ItemStack(DiaithirFoundry.PLATE.get());
        if (shape == Shape.ROD) return new ItemStack(DiaithirFoundry.ROD.get());
        if (shape == Shape.GEAR) return new ItemStack(DiaithirFoundry.GEAR.get());
        return ItemStack.EMPTY;
    }

    public static Identifier idOf(Item i) {
        return BuiltInRegistries.ITEM.getKey(i);
    }
}
