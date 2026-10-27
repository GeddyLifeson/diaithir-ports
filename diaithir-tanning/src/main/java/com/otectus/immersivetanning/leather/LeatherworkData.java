package com.otectus.immersivetanning.leather;

import com.otectus.immersivetanning.trait.TraitRef;
import com.otectus.immersivetanning.util.StackNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * The long-term identity of a piece made at the Leatherworker's Bench: Integrity (how well the hide was prepared),
 * Workmanship (how well it was stitched), Finish (what the leather was made to do) and an optional innate trait.
 * Stored in the namespaced {@code immersive_tanning} compound so other mods' NBT is left alone; stacks without it
 * behave exactly like ordinary items.
 */
public record LeatherworkData(Finish finish, int integrity, int workmanship, @Nullable TraitRef trait, @Nullable String crafter) {
    public static final String ROOT = StackNbt.ROOT;
    public static final int VERSION = 1;

    public LeatherworkData {
        integrity = Mth.clamp(integrity, 0, 100);
        workmanship = Mth.clamp(workmanship, 0, 100);
    }

    public TanningQuality quality() {
        return TanningQuality.fromScore(overallScore());
    }

    public int overallScore() {
        return TanningQuality.overallScore(integrity, workmanship);
    }

    public static boolean has(ItemStack stack) {
        CompoundTag root = StackNbt.root(stack);
        return root != null && root.contains("Finish");
    }

    public static Optional<LeatherworkData> get(ItemStack stack) {
        if (!has(stack)) return Optional.empty();
        CompoundTag root = StackNbt.root(stack);
        Finish finish = Finish.byName(root.getStringOr("Finish", ""));
        if (finish == null) return Optional.empty();
        String crafter = root.getString("Crafter").orElse(null);
        return Optional.of(new LeatherworkData(finish, root.getIntOr("Integrity", 0), root.getIntOr("Workmanship", 0), TraitRef.load(root), crafter));
    }

    /** The UUID of the player who stitched the piece at the bench. Villager and loot leatherwork have none. */
    public static final String CRAFTER_UUID = "CrafterUUID";

    /** Records the player who stitched the piece. Only the bench calls this; {@link #apply} leaves it in place. */
    public static void stampCrafter(ItemStack stack, UUID player) {
        StackNbt.edit(stack, root -> root.putString(CRAFTER_UUID, player.toString()));
    }

    /** The player who stitched the piece, when a player did. */
    public static Optional<UUID> crafterId(ItemStack stack) {
        CompoundTag root = StackNbt.root(stack);
        if (root == null) return Optional.empty();
        return root.getString(CRAFTER_UUID).flatMap(s -> {
            try {
                return Optional.of(UUID.fromString(s));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        });
    }

    /** Writes this identity onto the stack, keeping any durability carry already accumulated. */
    public void apply(ItemStack stack) {
        StackNbt.edit(stack, root -> {
            root.putInt("Version", VERSION);
            root.putString("Finish", finish.getSerializedName());
            root.putInt("Integrity", integrity);
            root.putInt("Workmanship", workmanship);
            for (String key : new String[]{"Trait", "TraitEffect", "TraitStrength", "TraitColor"}) root.remove(key);
            if (trait != null) trait.save(root);
            if (crafter != null) root.putString("Crafter", crafter);
            else root.remove("Crafter");
        });
        com.otectus.immersivetanning.effect.LeatherworkEffects.applyDurability(stack, this);
    }
}
