package com.otectus.immersivetanning.leather;

import com.otectus.immersivetanning.hide.HideData;
import com.otectus.immersivetanning.item.FinishedLeatherItem;
import com.otectus.immersivetanning.trait.TraitRef;
import com.otectus.immersivetanning.util.StackNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * One unit of finished leather. The finish is the item itself (Supple/Harness/Hardened Leather); the stack carries the
 * Integrity of the hide it came from, in steps of {@link HideData#INTEGRITY_STEP}, and the hide's innate trait.
 * Leather without data (creative menu, loot from other mods) counts as Standard.
 */
public record LeatherData(Finish finish, int integrity, @Nullable TraitRef trait) {
    public static final int DEFAULT_INTEGRITY = 50;

    public LeatherData {
        integrity = Mth.clamp(integrity, 0, 100);
    }

    public static Optional<LeatherData> get(ItemStack stack) {
        if (!(stack.getItem() instanceof FinishedLeatherItem leather)) return Optional.empty();
        CompoundTag root = StackNbt.root(stack);
        if (root == null) return Optional.of(new LeatherData(leather.finish(), DEFAULT_INTEGRITY, null));
        int integrity = root.getIntOr("Integrity", DEFAULT_INTEGRITY);
        return Optional.of(new LeatherData(leather.finish(), integrity, TraitRef.load(root)));
    }

    /** Writes Integrity and trait onto a leather stack of this finish. */
    public ItemStack applyTo(ItemStack stack) {
        CompoundTag root = new CompoundTag();
        root.putInt("Integrity", integrity);
        if (trait != null) trait.save(root);
        StackNbt.set(stack, root);
        return stack;
    }

    /** Leather that can be cut together for one piece: same finish and same trait. Integrity may differ. */
    public boolean compatible(LeatherData other) {
        return finish == other.finish && TraitRef.same(trait, other.trait);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(finish);
        buf.writeVarInt(integrity);
        TraitRef.writeNullable(buf, trait);
    }

    public static LeatherData read(FriendlyByteBuf buf) {
        return new LeatherData(buf.readEnum(Finish.class), buf.readVarInt(), TraitRef.readNullable(buf));
    }
}
