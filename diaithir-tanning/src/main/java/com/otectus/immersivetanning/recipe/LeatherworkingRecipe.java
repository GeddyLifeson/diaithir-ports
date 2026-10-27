package com.otectus.immersivetanning.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.otectus.immersivetanning.minigame.StitchPattern;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * One piece the Leatherworker's Bench can make: how many units of finished leather (all of one finish and trait),
 * which other ingredients, and which seam is stitched. Explicit recipes are datapack files under
 * {@code immersive_tanning/leatherworking}; automatic ones are generated from crafting recipes at reload. The server
 * sends the whole list to clients, so it never touches the vanilla recipe manager.
 */
public class LeatherworkingRecipe {
    public record Data(int leather, List<AuxiliaryIngredient> auxiliary, ItemStack result, Optional<Identifier> seam) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                ExtraCodecs.intRange(1, 64).fieldOf("leather").forGetter(Data::leather),
                AuxiliaryIngredient.CODEC.listOf().optionalFieldOf("auxiliary", List.of()).forGetter(Data::auxiliary),
                ItemStack.CODEC.fieldOf("result").forGetter(Data::result),
                Identifier.CODEC.optionalFieldOf("seam").forGetter(Data::seam)
        ).apply(i, Data::new));
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, LeatherworkingRecipe> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, LeatherworkingRecipe::getId,
            ByteBufCodecs.VAR_INT, LeatherworkingRecipe::leather,
            AuxiliaryIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), LeatherworkingRecipe::auxiliary,
            ItemStack.STREAM_CODEC, LeatherworkingRecipe::result,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), r -> Optional.ofNullable(r.seam),
            ByteBufCodecs.BOOL, LeatherworkingRecipe::isAuto,
            Identifier.STREAM_CODEC, LeatherworkingRecipe::seam,
            (id, leather, aux, result, seam, auto, resolved) -> {
                LeatherworkingRecipe r = new LeatherworkingRecipe(id, leather, aux, result, seam.orElse(null), auto);
                r.resolveSeam(resolved);
                return r;
            });

    private final Identifier id;
    private final int leather;
    private final List<AuxiliaryIngredient> auxiliary;
    private final ItemStack result;
    @Nullable
    private final Identifier seam;
    private final boolean auto;
    /** Filled in on the server during rebuild and carried to clients. */
    private Identifier resolvedSeam = StitchPattern.GENERIC;

    public LeatherworkingRecipe(Identifier id, int leather, List<AuxiliaryIngredient> auxiliary, ItemStack result,
                                @Nullable Identifier seam, boolean auto) {
        this.id = id;
        this.leather = leather;
        this.auxiliary = List.copyOf(auxiliary);
        this.result = result;
        this.seam = seam;
        this.auto = auto;
        if (seam != null) resolvedSeam = seam;
    }

    public static LeatherworkingRecipe of(Identifier id, Data data) {
        return new LeatherworkingRecipe(id, data.leather(), data.auxiliary(), data.result(), data.seam().orElse(null), false);
    }

    public Identifier getId() { return id; }
    public int leather() { return leather; }
    public List<AuxiliaryIngredient> auxiliary() { return auxiliary; }
    public ItemStack result() { return result; }
    @Nullable public Identifier explicitSeam() { return seam; }
    public boolean isAuto() { return auto; }
    public Identifier seam() { return resolvedSeam; }
    public Item resultItem() { return result.getItem(); }

    public void resolveSeam(Identifier seam) {
        this.resolvedSeam = seam;
    }
}
