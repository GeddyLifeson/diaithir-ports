package com.diaithir.addons.oreexcavation.recipe;

import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import com.zurrtum.create.foundation.recipe.CreateRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.diaithir.addons.oreexcavation.Config;
import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.util.ThreeState;

public class VeinRecipe implements CreateRecipe<NoInput> {
	public int priority;
	public Component veinName;
	public TagKey<Biome> biomeWhitelist, biomeBlacklist;
	public ThreeState finite;
	public float amountMultiplierMin, amountMultiplierMax;
	public RandomSpreadStructurePlacement placement;
	public net.minecraft.world.item.ItemStackTemplate icon;
	protected boolean isNet;

	public VeinRecipe() {
	}

	public VeinRecipe(Component veinName, int priority, Optional<TagKey<Biome>> biomeWhitelist, Optional<TagKey<Biome>> biomeBlacklist,
			ThreeState finite, float amountMultiplierMin, float amountMultiplierMax,
			RandomSpreadStructurePlacement placement, net.minecraft.world.item.ItemStackTemplate icon) {
		this.veinName = veinName;
		this.priority = priority;
		this.biomeWhitelist = biomeWhitelist.orElse(null);
		this.biomeBlacklist = biomeBlacklist.orElse(null);
		this.finite = finite;
		this.amountMultiplierMin = amountMultiplierMin;
		this.amountMultiplierMax = amountMultiplierMax;
		this.placement = placement;
		this.icon = icon;
	}

	@Override
	public boolean matches(NoInput in, Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(NoInput in) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeSerializer<VeinRecipe> getSerializer() {
		return Serializer.SERIALIZER;
	}

	@Override
	public RecipeType<VeinRecipe> getType() {
		return CreateOreExcavation.VEIN_RECIPES.getRecipeType();
	}

	public Component getName() {
		return veinName;
	}

	public boolean canGenerate(ServerLevel lvl, Holder<Biome> b) {
		if(biomeBlacklist != null && isInTag(biomeBlacklist, lvl, b))return false;
		if(biomeWhitelist != null) {
			return isInTag(biomeWhitelist, lvl, b);
		} else
			return true;
	}

	private static boolean isInTag(TagKey<Biome> tag, ServerLevel lvl, Holder<Biome> b) {
		return lvl.getServer().registryAccess().lookupOrThrow(Registries.BIOME).get(tag).map(t -> t.contains(b)).orElse(false);
	}

	public ThreeState isFinite() {
		return finite;
	}

	public float getMinAmount() {
		return amountMultiplierMin;
	}

	public float getMaxAmount() {
		return amountMultiplierMax;
	}

	public boolean isInfiniteClient() {
		return isNet ? finite == ThreeState.NEVER : finite == ThreeState.DEFAULT ? Config.defaultInfinite : finite == ThreeState.NEVER;
	}

	public long getMinAmountClient() {
		return Math.round(isNet ? (double) amountMultiplierMin : (double) amountMultiplierMin * Config.finiteAmountBase);
	}

	public long getMaxAmountClient() {
		return Math.round(isNet ? (double) amountMultiplierMax : (double) amountMultiplierMax * Config.finiteAmountBase);
	}

	public int getNegGenerationPriority() {
		return -priority;
	}

	public int getPriority() {
		return priority;
	}

	public RandomSpreadStructurePlacement getPlacement() {
		return placement;
	}

	public Optional<TagKey<Biome>> biomeWhitelist() {
		return Optional.ofNullable(biomeWhitelist);
	}

	public Optional<TagKey<Biome>> biomeBlacklist() {
		return Optional.ofNullable(biomeBlacklist);
	}

	public ItemStack getIcon() {
		return icon.create();
	}

	public net.minecraft.world.item.ItemStackTemplate getIconTemplate() {
		return icon;
	}

	public static class Serializer {
		private static final Identifier NULL = Identifier.tryParse("coe:null");

		private static VeinRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
			VeinRecipe r = new VeinRecipe();
			r.priority = buffer.readVarInt();
			r.veinName = ComponentSerialization.STREAM_CODEC.decode(buffer);
			r.biomeWhitelist = create(buffer);
			r.biomeBlacklist = create(buffer);
			r.finite = buffer.readBoolean() ? ThreeState.ALWAYS : ThreeState.NEVER;
			r.amountMultiplierMin = buffer.readFloat();
			r.amountMultiplierMax = buffer.readFloat();
			r.icon = net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.decode(buffer);
			r.isNet = true;
			r.placement = new RandomSpreadStructurePlacement(2, 1, RandomSpreadType.LINEAR, 0);
			return r;
		}

		private static TagKey<Biome> create(FriendlyByteBuf buffer) {
			Identifier rl = buffer.readIdentifier();
			if(NULL.equals(rl))return null;
			else return TagKey.create(Registries.BIOME, rl);
		}

		private static void toNetwork(RegistryFriendlyByteBuf buffer, VeinRecipe recipe) {
			buffer.writeVarInt(recipe.priority);
			ComponentSerialization.STREAM_CODEC.encode(buffer, recipe.veinName);
			write(recipe.biomeWhitelist, buffer);
			write(recipe.biomeBlacklist, buffer);
			buffer.writeBoolean(recipe.finite == ThreeState.DEFAULT ? !Config.defaultInfinite : recipe.finite == ThreeState.ALWAYS);
			buffer.writeFloat(recipe.amountMultiplierMin * Config.finiteAmountBase);
			buffer.writeFloat(recipe.amountMultiplierMax * Config.finiteAmountBase);
			net.minecraft.world.item.ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.icon);
		}

		private static void write(TagKey<Biome> tag, FriendlyByteBuf buffer) {
			buffer.writeIdentifier(tag != null ? tag.location() : NULL);
		}

		public static final MapCodec<VeinRecipe> CODEC = RecordCodecBuilder.<VeinRecipe>mapCodec(b -> {
			return b.group(
					ComponentSerialization.CODEC.fieldOf("name").forGetter(VeinRecipe::getName),
					Codec.INT.fieldOf("priority").forGetter(VeinRecipe::getPriority),
					TagKey.codec(Registries.BIOME).optionalFieldOf("biomeWhitelist").forGetter(VeinRecipe::biomeWhitelist),
					TagKey.codec(Registries.BIOME).optionalFieldOf("biomeBlacklist").forGetter(VeinRecipe::biomeBlacklist),
					ThreeState.CODEC.fieldOf("finite").forGetter(VeinRecipe::isFinite),
					Codec.FLOAT.fieldOf("amountMultiplierMin").forGetter(VeinRecipe::getMinAmount),
					Codec.FLOAT.fieldOf("amountMultiplierMax").forGetter(VeinRecipe::getMaxAmount),
					RandomSpreadStructurePlacement.CODEC.fieldOf("placement").forGetter(VeinRecipe::getPlacement),
					net.minecraft.world.item.ItemStackTemplate.CODEC.fieldOf("icon").forGetter(VeinRecipe::getIconTemplate)
					).apply(b, VeinRecipe::new);
		});

		public static final StreamCodec<RegistryFriendlyByteBuf, VeinRecipe> STREAM_CODEC = StreamCodec.of(
				VeinRecipe.Serializer::toNetwork, VeinRecipe.Serializer::fromNetwork
				);

		public static final RecipeSerializer<VeinRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
	}

}
