package mcjty.rftoolsutility.modules.spawner.recipes;

import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import mcjty.lib.crafting.BaseRecipe;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class SpawnerRecipe implements BaseRecipe<CraftingInput> {

    public static final RecipeSerializer<SpawnerRecipe> SERIALIZER = new RecipeSerializer<>(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(SpawnerRecipe::getId),
                    SpawnerRecipes.MobSpawnAmount.CODEC.fieldOf("item1").forGetter(SpawnerRecipe::getItem1),
                    SpawnerRecipes.MobSpawnAmount.CODEC.fieldOf("item2").forGetter(SpawnerRecipe::getItem2),
                    SpawnerRecipes.MobSpawnAmount.CODEC.fieldOf("item3").forGetter(SpawnerRecipe::getItem3),
                    Codec.INT.fieldOf("power").forGetter(SpawnerRecipe::getSpawnRf),
                    Identifier.CODEC.fieldOf("entity").forGetter(SpawnerRecipe::getEntity)
            ).apply(instance, SpawnerRecipe::new)),
            StreamCodec.composite(
                    Identifier.STREAM_CODEC, SpawnerRecipe::getId,
                    SpawnerRecipes.MobSpawnAmount.STREAM_CODEC, SpawnerRecipe::getItem1,
                    SpawnerRecipes.MobSpawnAmount.STREAM_CODEC, SpawnerRecipe::getItem2,
                    SpawnerRecipes.MobSpawnAmount.STREAM_CODEC, SpawnerRecipe::getItem3,
                    ByteBufCodecs.INT, SpawnerRecipe::getSpawnRf,
                    Identifier.STREAM_CODEC, SpawnerRecipe::getEntity,
                    SpawnerRecipe::new));

    private final Identifier id;
    private final SpawnerRecipes.MobSpawnAmount item1;
    private final SpawnerRecipes.MobSpawnAmount item2;
    private final SpawnerRecipes.MobSpawnAmount item3;
    private final int spawnRf;
    private final Identifier entity;

    public SpawnerRecipe(Identifier id, SpawnerRecipes.MobSpawnAmount item1, SpawnerRecipes.MobSpawnAmount item2, SpawnerRecipes.MobSpawnAmount item3, int spawnRf,
                         Identifier entity) {
        this.id = id;
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
        this.spawnRf = spawnRf;
        this.entity = entity;
    }

    public Identifier getId() {
        return id;
    }

    public SpawnerRecipes.MobSpawnAmount getItem1() {
        return item1;
    }

    public SpawnerRecipes.MobSpawnAmount getItem2() {
        return item2;
    }

    public SpawnerRecipes.MobSpawnAmount getItem3() {
        return item3;
    }

    public int getSpawnRf() {
        return spawnRf;
    }

    public Identifier getEntity() {
        return entity;
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Nonnull
    @Override
    public RecipeSerializer<SpawnerRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Nonnull
    @Override
    public RecipeType<SpawnerRecipe> getType() {
        return SpawnerModule.SPAWNER_RECIPE_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
