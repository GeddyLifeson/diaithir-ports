package com.lowdragmc.lowdraglib.syncdata.accessor;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.syncdata.AccessorOp;
import com.lowdragmc.lowdraglib.syncdata.payload.ITypedPayload;
import com.lowdragmc.lowdraglib.syncdata.payload.NbtTagPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

public class RecipeAccessor extends CustomObjectAccessor<RecipeHolder> {

    public RecipeAccessor() {
        super(RecipeHolder.class, true);
    }

    @Override
    public ITypedPayload<?> serialize(AccessorOp op, RecipeHolder value, HolderLookup.Provider provider) {
        var type = BuiltInRegistries.RECIPE_TYPE.getKey(value.value().getType());
        var id = value.id();
        CompoundTag tag = new CompoundTag();
        tag.putString("type", type == null ? "null" : type.toString());
        tag.putString("id", id.toString());
        return NbtTagPayload.of(tag);
    }

    @Override
    public RecipeHolder deserialize(AccessorOp op, ITypedPayload<?> payload, HolderLookup.Provider provider) {
        // PORT: recipes are keyed by ResourceKey now; lookup through the manager
        if (payload instanceof NbtTagPayload nbtTagPayload && nbtTagPayload.getPayload() instanceof CompoundTag tag) {
            var id = net.minecraft.resources.Identifier.parse(tag.getStringOr("id", "minecraft:air"));
            net.minecraft.world.item.crafting.RecipeManager recipeManager;
            if (com.lowdragmc.lowdraglib.LDLib.isRemote()) {
                return null;
            } else {
                recipeManager = com.lowdragmc.lowdraglib.Platform.getMinecraftServer().getRecipeManager();
            }
            return recipeManager.byKey(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, id)).orElse(null);
        }
        return null;
    }
}
