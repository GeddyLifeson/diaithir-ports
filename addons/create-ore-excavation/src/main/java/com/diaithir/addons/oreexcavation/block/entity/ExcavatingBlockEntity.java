package com.diaithir.addons.oreexcavation.block.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.diaithir.addons.oreexcavation.recipe.OreRecipes;
import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import java.util.Comparator;
import java.util.List;

import com.zurrtum.create.client.catnip.lang.FontHelper.Palette;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import com.zurrtum.create.content.kinetics.base.IRotate.SpeedLevel;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.OreData;
import com.diaithir.addons.oreexcavation.OreDataAttachment;
import com.diaithir.addons.oreexcavation.Registration;
import com.diaithir.addons.oreexcavation.client.ClientUtil;
import com.diaithir.addons.oreexcavation.recipe.ExcavatingRecipe;
import com.diaithir.addons.oreexcavation.recipe.VeinRecipe;
import com.diaithir.addons.oreexcavation.util.DimChunkPos;
import com.diaithir.addons.oreexcavation.util.NumberFormatter;
import com.diaithir.addons.oreexcavation.util.TooltipUtil;

public abstract class ExcavatingBlockEntity<R extends ExcavatingRecipe> extends SmartBlockEntity implements MultiblockCapHandler, IDrill {
	public int progress;
	protected Kinetic kinetic;
	public Identifier veinClient, recipeClient;
	public long resourceRemClient;
	public boolean hasRotation;
	public ItemStack drillStack;
	protected RecipeHolder<R> current;
	protected RecipeHolder<VeinRecipe> vein;
	protected OreData data;
	public ExcavatorState state = ExcavatorState.NO_VEIN;
	protected boolean completedOneCycle;

	protected ExcavatingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		drillStack = ItemStack.EMPTY;
		setLazyTickRate(20);
	}


	public void updateRecipe() {
		vein = null;
		current = null;

		ChunkPos p = ChunkPos.containing(worldPosition);
		data = OreDataAttachment.getData(level.getChunk(p.x(), p.z()));
		if(data != null) {
			vein = data.getRecipe(level);
			if(vein != null) {
				Identifier veinId = OreRecipes.idOf(vein);
				List<RecipeHolder<R>> rec = OreRecipes.all(level, getRecipeType()).stream().filter(r -> r.value().veinId.equals(veinId)).sorted(Comparator.comparingInt(r -> r.value().priority)).toList();
				if(rec.size() == 1)current = rec.get(0);
				else if(rec.size() > 1) {
					for (RecipeHolder<R> r : rec) {
						if(validateRecipe(r.value())) {
							current = r;
						}
					}
					if(current == null)current = rec.get(0);
				}
			}
		}
	}

	protected boolean validateRecipe(R recipe) {
		return recipe.getDrill().test(drillStack);
	}

	public abstract RecipeType<R> getRecipeType();
	protected abstract boolean canExtract();
	protected abstract void onFinished();

	@Override
	public void tick() {
		super.tick();
		if(!level.isClientSide()) {
			if(current != null && state == ExcavatorState.NO_ERROR) {
				if(kinetic != null)kinetic.setStress(current.value().getStress());
				if(canExtract() && kinetic != null && kinetic.getRotationSpeed() >= SpeedLevel.MEDIUM.getSpeedValue() &&
						current.value().getDrill().test(drillStack) && level.getBlockState(getBelow()).isCollisionShapeFullBlock(level, getBelow())) {
					float prg = kinetic.getRotationSpeed() / SpeedLevel.MEDIUM.getSpeedValue();
					progress += prg;
					if(progress >= current.value().getTicks()) {
						updateState();
						if(state == ExcavatorState.NO_ERROR) {
							onFinished();
							data.extract(1);
							completedOneCycle = true;
							setChanged();
						}
						progress = 0;
					}
				} else if(!current.value().getDrill().test(drillStack)) {
					RecipeHolder<R> old = current;
					updateRecipe();
					if (old != current)
						progress = 0;
				}
			} else if(progress > 10) {
				progress = 0;
				updateState();
			} else {
				progress++;
			}
		}
	}

	private void updateState() {
		updateRecipe();
		if(vein == null) {
			state = ExcavatorState.NO_VEIN;
		} else if(!data.canExtract(level, worldPosition)) {
			state = ExcavatorState.TOO_MANY_EXCAVATORS;
		} else if(data.getResourcesRemaining(vein.value()) == -1) {
			state = ExcavatorState.VEIN_EMPTY;
		} else if(current == null) {
			state = ExcavatorState.NO_RECIPE;
		} else {
			state = ExcavatorState.NO_ERROR;
		}
		notifyUpdate();
	}

	@Override
	public void lazyTick() {
		notifyUpdate();
	}

	@Override
	protected void read(ValueInput tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		progress = tag.getIntOr("progress", 0);
		drillStack = tag.read("drill", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		completedOneCycle = tag.getBooleanOr("finishedOnce", false);
		if(clientPacket) {
			state = ExcavatorState.VALUES[Mth.clamp(tag.getIntOr("state", 0), 0, ExcavatorState.VALUES.length - 1)];
			veinClient = tag.getString("veinId").map(Identifier::tryParse).orElse(null);
			resourceRemClient = tag.getLongOr("resRem", 0L);
			recipeClient = tag.getString("currentRecipeId").map(Identifier::tryParse).orElse(null);
			hasRotation = tag.getBooleanOr("hasRot", false);
		}
	}

	@Override
	protected void write(ValueOutput tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		tag.putInt("progress", progress);
		if (!drillStack.isEmpty())tag.store("drill", ItemStack.CODEC, drillStack);
		tag.putBoolean("finishedOnce", completedOneCycle);
		if(clientPacket) {
			tag.putInt("state", state.ordinal());
			if(vein != null) {
				tag.putString("veinId", OreRecipes.idOf(vein).toString());
				tag.putLong("resRem", data.getResourcesRemaining(vein.value()));
			}
			if(current != null) {
				tag.putString("currentRecipeId", OreRecipes.idOf(current).toString());
			}
			tag.putBoolean("hasRot", kinetic != null && kinetic.getRotationSpeed() >= SpeedLevel.MEDIUM.getSpeedValue());
		}
	}

	@Override
	public void initialize() {
		super.initialize();
		if(!level.isClientSide()) {
			if(progress > 0 && current == null)
				updateRecipe();
			if(current == null)progress = 0;
		}
	}

	@Override
	public ItemStack getDrill() {
		return drillStack;
	}

	@Override
	public BlockPos getBelow() {
		return worldPosition.below(2);
	}

	@Override
	public void addKinetic(Kinetic k) {
		this.kinetic = k;
	}

	@Override
	public void dropInv() {
		dropItemStack(drillStack);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
		if (level != null && !level.isClientSide())
			dropInv();
		super.preRemoveSideEffects(pos, oldState);
	}

	public InteractionResult onClick(Player player, InteractionHand hand) {
		ItemStack item = player.getItemInHand(hand);
		if (item.getItem() == com.diaithir.addons.oreexcavation.Registration.VEIN_ATLAS_ITEM.get()) {
			if(!level.isClientSide()) {
				if (completedOneCycle) {
					com.diaithir.addons.oreexcavation.Registration.VEIN_ATLAS_ITEM.get().addVein(player, item, vein, new DimChunkPos(level, worldPosition), data.getRandomMul());
				} else {
					player.sendOverlayMessage(Component.translatable("chat.coe.sampleDrill.notDone"));
				}
			}
			return InteractionResult.SUCCESS;
		} else if(item.is(CreateOreExcavation.DRILL_TAG)) {
			if (drillStack.isEmpty()) {
				if(!level.isClientSide())drillStack = item.split(1);
				notifyUpdate();
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	public InteractionResult onClick(Player player) {
		if(!drillStack.isEmpty()) {
			if(!level.isClientSide()) {
				if(player.addItem(drillStack)) {
					drillStack = ItemStack.EMPTY;
					notifyUpdate();
					return InteractionResult.CONSUME;
				}
				return InteractionResult.SUCCESS;
			} else return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
	}

	protected void dropItemStack(ItemStack stackInSlot) {
		Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stackInSlot);
	}

	@Override
	protected AABB createRenderBoundingBox() {
		return AABB.encapsulatingFullBlocks(worldPosition.offset(-1, -1, -1), worldPosition.offset(1, 0, 1));
	}

	@Override
	public Direction getFacing() {
		return getBlockState().getValue(HORIZONTAL_FACING);
	}

	@Override
	public boolean shouldRenderRubble() {
		return completedOneCycle || (recipeClient != null && progress > 0);
	}

	@Override
	public float getYOffset() {
		return 0.8f;
	}

	@Override
	public float getDrillOffset() {
		return isActive() ? 0.2f : 0.0f;
	}

	private boolean isActive() {
		return recipeClient != null && hasRotation;
	}

	@Override
	public float getRotation() {
		if (isActive()) {
			long ticks = getLevel().getGameTime();
			float rot = (ticks * 20) % 360;
			return rot;
		}
		return 0f;
	}

	@Override
	public float getPrevRotation() {
		if (isActive()) {
			long ticks = getLevel().getGameTime() - 1;
			float rot = (ticks * 20) % 360;
			return rot;
		}
		return 0f;
	}

	@Override
	public boolean shouldRenderShaft() {
		return true;
	}

	public static enum ExcavatorState {
		NO_ERROR,
		NO_VEIN,
		VEIN_EMPTY,
		TOO_MANY_EXCAVATORS,
		NO_RECIPE,
		;
		public static final ExcavatorState[] VALUES = values();
	}
}
