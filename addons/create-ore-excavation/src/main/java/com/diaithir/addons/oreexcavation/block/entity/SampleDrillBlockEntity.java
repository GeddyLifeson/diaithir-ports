package com.diaithir.addons.oreexcavation.block.entity;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.diaithir.addons.oreexcavation.recipe.OreRecipes;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.zurrtum.create.content.equipment.armor.BacktankBlockEntity;
import com.zurrtum.create.foundation.blockEntity.ComparatorUtil;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;

import com.diaithir.addons.oreexcavation.OreData;
import com.diaithir.addons.oreexcavation.OreDataAttachment;
import com.diaithir.addons.oreexcavation.Registration;
import com.diaithir.addons.oreexcavation.client.ClientUtil;
import com.diaithir.addons.oreexcavation.recipe.VeinRecipe;
import com.diaithir.addons.oreexcavation.util.DimChunkPos;
import com.diaithir.addons.oreexcavation.util.NumberFormatter;
import com.diaithir.addons.oreexcavation.util.TooltipUtil;

public class SampleDrillBlockEntity extends SmartBlockEntity implements IDrill {
	public static final int DRILL_TIME = 200;
	public Identifier veinClient;
	public long resourceRemClient;
	private RecipeHolder<VeinRecipe> vein;
	private OreData data;
	public int progress = 0;
	public boolean drilling = false;
	public float airTankLevel = -1f;

	public SampleDrillBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		setLazyTickRate(20);
	}

	private void updateVein() {
		ChunkPos p = ChunkPos.containing(worldPosition);
		data = OreDataAttachment.getData(level.getChunk(p.x(), p.z()));
		if(data != null) {
			vein = data.getRecipe(level);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide() && drilling && progress < DRILL_TIME &&
				level.getBlockState(worldPosition.below()).isCollisionShapeFullBlock(level, worldPosition.below())) {
			BlockEntity be = level.getBlockEntity(worldPosition.above());
			if (be instanceof BacktankBlockEntity t) {
				int air = t.getAirLevel();
				if (t.getComparatorOutput() > ComparatorUtil.fractionToRedstoneLevel(0.2f)) {
					int op = Mth.clamp(air / 80, 1, 4);
					t.setAirLevel(air - op);
					progress += op;
					notifyUpdate();
				}
			}
		}
	}

	@Override
	public void lazyTick() {
		if (level.isClientSide())return;
		updateVein();
		BlockEntity be = level.getBlockEntity(worldPosition.above());
		if (be instanceof BacktankBlockEntity t) {
			airTankLevel = t.getComparatorOutput() / (float) ComparatorUtil.fractionToRedstoneLevel(1f);
		} else {
			airTankLevel = -1f;
		}
		notifyUpdate();
	}

	@Override
	protected void write(ValueOutput compound, boolean clientPacket) {
		super.write(compound, clientPacket);
		compound.putInt("progress", progress);
		compound.putBoolean("drilling", drilling);
		if(clientPacket) {
			if(vein != null) {
				compound.putString("veinId", OreRecipes.idOf(vein).toString());
				compound.putLong("resRem", data.getResourcesRemaining(vein.value()));
			}
			compound.putFloat("air", airTankLevel);
		}
	}

	@Override
	protected void read(ValueInput compound, boolean clientPacket) {
		super.read(compound, clientPacket);
		progress = compound.getIntOr("progress", 0);
		drilling = compound.getBooleanOr("drilling", false);
		if(clientPacket) {
			veinClient = compound.getString("veinId").map(Identifier::tryParse).orElse(null);
			resourceRemClient = compound.getLongOr("resRem", 0L);
			airTankLevel = compound.getFloatOr("air", -1f);
		}
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
	}

	public boolean clickedWithItem(Player player, InteractionHand hand) {
		if (progress >= DRILL_TIME) {
			if (vein != null) {
				ItemStack is = player.getItemInHand(hand);
				if (is.getItem() == Registration.VEIN_ATLAS_ITEM.get()) {
					Registration.VEIN_ATLAS_ITEM.get().addVein(player, is, vein, new DimChunkPos(level, worldPosition), data.getRandomMul());
					return true;
				}
				player.sendOverlayMessage(Component.translatable("chat.coe.sampleDrill.noAtlas"));
			}
		}
		return false;
	}

	public void clicked(Player player) {
		if (progress >= DRILL_TIME) {
			if (vein == null) {
				player.sendOverlayMessage(Component.translatable("chat.coe.veinFinder.found", Component.translatable("chat.coe.veinFinder.nothing")));
			}
		} else if(!drilling) {
			if(airTankLevel > 0.2f) {
				progress = 0;
				drilling = true;
				player.sendOverlayMessage(Component.translatable("chat.coe.sampleDrill.start"));
			} else if(airTankLevel < 0) {
				player.sendOverlayMessage(Component.translatable("chat.coe.sampleDrill.noAir"));
			} else {
				player.sendOverlayMessage(Component.translatable("chat.coe.sampleDrill.lowAir"));
			}
		} else {
			player.sendOverlayMessage(Component.translatable("chat.coe.sampleDrill.running"));
		}
	}

	@Override
	public ItemStack getDrill() {
		return new ItemStack(Registration.NORMAL_DRILL_ITEM.get());
	}

	@Override
	public BlockPos getBelow() {
		return worldPosition.below();
	}

	@Override
	public Direction getFacing() {
		return Direction.SOUTH;
	}

	@Override
	public boolean shouldRenderRubble() {
		return progress > 0;
	}

	@Override
	public float getYOffset() {
		return 1.7f;
	}

	@Override
	public float getDrillOffset() {
		return progress >= DRILL_TIME ? 0f : (progress / (float) DRILL_TIME) * 0.5f;
	}

	@Override
	public float getRotation() {
		if (progress >= DRILL_TIME)return 0f;
		if (progress > 0 && airTankLevel > 0.2f) {
			long ticks = getLevel().getGameTime();
			float rot = (ticks * 20) % 360;
			return rot;
		}
		return 0f;
	}

	@Override
	public float getPrevRotation() {
		if (progress >= DRILL_TIME)return 0f;
		if (progress > 0 && airTankLevel > 0.2f) {
			long ticks = getLevel().getGameTime() - 1;
			float rot = (ticks * 20) % 360;
			return rot;
		}
		return 0f;
	}

	@Override
	public boolean shouldRenderShaft() {
		return (progress / (float) DRILL_TIME) > 0.5f && progress < DRILL_TIME;
	}

	public void setDrilling(boolean drilling) {
		this.drilling = drilling;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public void setAirTankLevel(float airTankLevel) {
		this.airTankLevel = airTankLevel;
	}
}
