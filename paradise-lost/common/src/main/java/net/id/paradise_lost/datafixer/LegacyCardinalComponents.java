package net.id.paradise_lost.datafixer;

import net.id.paradise_lost.ModConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public final class LegacyCardinalComponents {
    public static final String ROOT_KEY = "cardinal_components";
    public static final Identifier MOA_GENETICS = ModConstants.locate("moa_genetics");
    public static final Identifier MINECART_FLOAT = ModConstants.locate("minecart_float");

    private LegacyCardinalComponents() {
    }

    @Nullable
    public static CompoundTag get(CompoundTag entityTag, Identifier componentId) {
        if (entityTag == null || !entityTag.contains(ROOT_KEY)) {
            return null;
        }
        CompoundTag root = entityTag.getCompoundOrEmpty(ROOT_KEY);
        String key = componentId.toString();
        if (!root.contains(key)) {
            return null;
        }
        return root.getCompoundOrEmpty(key);
    }

    public static void remove(CompoundTag entityTag, Identifier componentId) {
        if (entityTag == null || !entityTag.contains(ROOT_KEY)) {
            return;
        }
        CompoundTag root = entityTag.getCompoundOrEmpty(ROOT_KEY);
        root.remove(componentId.toString());
        if (root.isEmpty()) {
            entityTag.remove(ROOT_KEY);
        }
    }
}
