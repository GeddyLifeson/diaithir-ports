package com.diaithir.addons.oreexcavation.recipe;

import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import com.zurrtum.create.foundation.recipe.CreateRecipe;

public abstract class ExcavatingRecipe implements CreateRecipe<NoInput> {
	public Identifier veinId;
	public Ingredient drill;
	public int priority, ticks, stressMul;
	public Optional<FluidIngredient> drillingFluid = Optional.empty();

	protected void setFromCommon(ExcavatingRecipeCommon c) {
		veinId = c.veinId();
		drill = c.drill();
		priority = c.priority();
		ticks = c.ticks();
		stressMul = c.stressMul();
		drillingFluid = c.drillingFluid();
	}

	protected ExcavatingRecipeCommon getCommon() {
		return new ExcavatingRecipeCommon(veinId, drill, priority, ticks, stressMul, drillingFluid);
	}

	@Override
	public boolean matches(NoInput in, Level level) {
		return false;
	}

	@Override
	public ItemStack assemble(NoInput in) {
		return ItemStack.EMPTY;
	}

	public Ingredient getDrill() {
		return drill;
	}

	public int getTicks() {
		return ticks;
	}

	public int getStress() {
		return stressMul;
	}

	public Optional<FluidIngredient> getDrillingFluid() {
		return drillingFluid;
	}

	public static record ExcavatingRecipeCommon(Identifier veinId, Ingredient drill, int priority, int ticks, int stressMul, Optional<FluidIngredient> drillingFluid) {
		public static final MapCodec<ExcavatingRecipeCommon> CODEC = RecordCodecBuilder.mapCodec(
				b -> b.group(
						Identifier.CODEC.fieldOf("veinId").forGetter(ExcavatingRecipeCommon::veinId),
						Ingredient.CODEC.fieldOf("drill").forGetter(ExcavatingRecipeCommon::drill),
						Codec.INT.fieldOf("priority").forGetter(ExcavatingRecipeCommon::priority),
						Codec.INT.fieldOf("ticks").forGetter(ExcavatingRecipeCommon::ticks),
						Codec.INT.fieldOf("stress").forGetter(ExcavatingRecipeCommon::stressMul),
						FluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(ExcavatingRecipeCommon::drillingFluid)
						).apply(b, ExcavatingRecipeCommon::new));

		public static ExcavatingRecipeCommon read(RegistryFriendlyByteBuf buf) {
			Ingredient drill = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
			int ticks = buf.readVarInt();
			int stress = buf.readVarInt();
			int prio = buf.readVarInt();
			Identifier vein = buf.readIdentifier();
			Optional<FluidIngredient> fl = buf.readBoolean() ? Optional.of(FluidIngredient.PACKET_CODEC.decode(buf)) : Optional.empty();
			return new ExcavatingRecipeCommon(vein, drill, prio, ticks, stress, fl);
		}

		public void write(RegistryFriendlyByteBuf buf) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(buf, drill);
			buf.writeVarInt(ticks);
			buf.writeVarInt(stressMul);
			buf.writeVarInt(priority);
			buf.writeIdentifier(veinId);
			buf.writeBoolean(drillingFluid.isPresent());
			drillingFluid.ifPresent(f -> FluidIngredient.PACKET_CODEC.encode(buf, f));
		}
	}
}
