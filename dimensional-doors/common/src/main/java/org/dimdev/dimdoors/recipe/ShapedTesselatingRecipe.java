package org.dimdev.dimdoors.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ShapedTesselatingRecipe implements TesselatingRecipe {
    final ShapedRecipePattern pattern;
    final ItemStackTemplate result;
    final String group;
    final boolean showNotification;
    private final int weavingTime;
    private PlacementInfo placementInfo;

    public ShapedTesselatingRecipe(String group, ShapedRecipePattern pattern, ItemStackTemplate result, int weavingTime, boolean showNotification) {
        this.group = group;
        this.pattern = pattern;
        this.result = result;
        this.weavingTime = weavingTime;
        this.showNotification = showNotification;
    }

    @Override
    public @NotNull RecipeSerializer<? extends ShapedTesselatingRecipe> getSerializer() {
        return ModRecipeSerializers.SHAPED_TESSELATING;
    }

    @Override
    public @NotNull String group() {
        return this.group;
    }

    public ItemStack getResult() {
        return this.result.create();
    }

    public List<java.util.Optional<Ingredient>> getIngredients() {
        return this.pattern.ingredients();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.createFromOptionals(this.pattern.ingredients());
        }
        return this.placementInfo;
    }

    @Override
    public boolean showNotification() {
        return this.showNotification;
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        return this.pattern.matches(inv);
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        return this.result.create();
    }

    public int getWidth() {
        return this.pattern.width();
    }

    public int getHeight() {
        return this.pattern.height();
    }

    public int weavingTime() {
        return weavingTime;
    }

    public static class Serializer {
        public static final MapCodec<ShapedTesselatingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> {
            return instance.group(
                    Codec.STRING.optionalFieldOf("group", "").forGetter(a -> a.group),
                    ShapedRecipePattern.MAP_CODEC.forGetter(a -> a.pattern),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(a -> a.result),
                    Codec.INT.optionalFieldOf("weaving_time", 200).forGetter(a -> a.weavingTime),
                    Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(a -> a.showNotification)
            ).apply(instance, ShapedTesselatingRecipe::new);
        });

        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedTesselatingRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.group,
                ShapedRecipePattern.STREAM_CODEC, r -> r.pattern,
                ItemStackTemplate.STREAM_CODEC, r -> r.result,
                ByteBufCodecs.INT, r -> r.weavingTime,
                ByteBufCodecs.BOOL, r -> r.showNotification,
                ShapedTesselatingRecipe::new);

        public static RecipeSerializer<ShapedTesselatingRecipe> create() {
            return new RecipeSerializer<>(CODEC, STREAM_CODEC);
        }
    }
}
