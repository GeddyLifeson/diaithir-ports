package rpggods.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/**
 * Small helpers that keep the pre-1.21.5 style of reading tags with defaults.
 */
public final class Nbt {
    private Nbt() {}

    public static String getString(CompoundTag t, String k) { return t.getStringOr(k, ""); }
    public static int getInt(CompoundTag t, String k) { return t.getIntOr(k, 0); }
    public static long getLong(CompoundTag t, String k) { return t.getLongOr(k, 0L); }
    public static float getFloat(CompoundTag t, String k) { return t.getFloatOr(k, 0F); }
    public static double getDouble(CompoundTag t, String k) { return t.getDoubleOr(k, 0D); }
    public static boolean getBoolean(CompoundTag t, String k) { return t.getBooleanOr(k, false); }
    public static CompoundTag getCompound(CompoundTag t, String k) { return t.getCompoundOrEmpty(k); }
    public static ListTag getList(CompoundTag t, String k) { return t.getListOrEmpty(k); }

    /** Saves the entity to a tag, like the old Forge serializeNBT did **/
    public static CompoundTag saveEntity(net.minecraft.world.entity.Entity entity) {
        net.minecraft.world.level.storage.TagValueOutput out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess());
        entity.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        tag.putString("id", net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
        return tag;
    }

    /** Loads the tag into the entity **/
    public static void loadEntity(net.minecraft.world.entity.Entity entity, CompoundTag tag) {
        entity.load(net.minecraft.world.level.storage.TagValueInput.create(
                net.minecraft.util.ProblemReporter.DISCARDING, entity.registryAccess(), tag));
    }

    /** Sends a message to the player, either in the chat or above the hotbar **/
    public static void msg(net.minecraft.world.entity.player.Player player, net.minecraft.network.chat.Component message, boolean overlay) {
        if (overlay) {
            player.sendOverlayMessage(message);
        } else {
            player.sendSystemMessage(message);
        }
    }
}
