package mcjty.rftoolsutility.modules.environmental.recipes;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.Codec;
import mcjty.lib.crafting.BaseShapedRecipe;
import mcjty.rftoolsutility.modules.spawner.items.SyringeItem;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class SyringeBasedRecipe extends BaseShapedRecipe {

    public static final RecipeSerializer<SyringeBasedRecipe> SERIALIZER = new RecipeSerializer<>(
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.STRING.fieldOf("group").forGetter(ShapedRecipe::group),
                    ShapedRecipePattern.MAP_CODEC.fieldOf("pattern").forGetter(recipe -> recipe.pattern),
                    ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
                    Identifier.CODEC.fieldOf("mob").forGetter(SyringeBasedRecipe::getMobId),
                    Codec.INT.fieldOf("syringe").forGetter(SyringeBasedRecipe::getSyringeIndex)
            ).apply(instance, SyringeBasedRecipe::new)),
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, ShapedRecipe::group,
                    ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
                    ItemStack.STREAM_CODEC, recipe -> recipe.result,
                    Identifier.STREAM_CODEC, SyringeBasedRecipe::getMobId,
                    ByteBufCodecs.INT, SyringeBasedRecipe::getSyringeIndex,
                    SyringeBasedRecipe::new));

    private final ItemStack result;
    private final Identifier mobId;
    private final int syringeIndex;

    public SyringeBasedRecipe(String group, ShapedRecipePattern pattern, ItemStack result, Identifier mobId, int syringeIndex) {
        super(group, CraftingBookCategory.MISC, pattern, result);
        this.result = result;
        this.mobId = mobId;
        this.syringeIndex = syringeIndex;
    }

    @Override
    public boolean matches(@Nonnull CraftingInput inv, @Nonnull Level level) {
        boolean matches = super.matches(inv, level);
        if (matches) {
            for (int i = 0 ; i < inv.width() * inv.height() ; i++) {
                ItemStack stack = inv.getItem(i);
                if (stack.getItem() instanceof SyringeItem) {
                    Identifier mob = SyringeItem.getMobId(stack);
                    if (mob == null || !mob.equals(mobId)) {
                        return false;
                    }
                    int amount = SyringeItem.getLevel(stack);
                    if (amount < 100) {
                        return false;
                    }
                }
            }
        }
        return matches;
    }

    public Identifier getMobId() {
        return mobId;
    }

    @Nonnull
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer) SERIALIZER;
    }

    public int getSyringeIndex() {
        return syringeIndex;
    }
}
