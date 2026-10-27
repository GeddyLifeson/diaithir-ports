package com.mrh0.createaddition.blocks.alternator;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.mrh0.createaddition.energy.fe.FeCache;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.config.CommonConfig;
import com.mrh0.createaddition.energy.IEnergyProvider;
import com.mrh0.createaddition.energy.InternalEnergyStorage;
import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.client.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import com.mrh0.createaddition.energy.fe.IEnergyStorage;

import javax.annotation.Nullable;

public class AlternatorBlockEntity extends KineticBlockEntity implements IEnergyProvider {

	protected final InternalEnergyStorage energy;
	private final IEnergyStorage capability;

	private final EnumSet<Direction> invalidSides = EnumSet.allOf(Direction.class);
	private final EnumMap<Direction, FeCache> cache = new EnumMap<>(Direction.class);

	public AlternatorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
		energy = new InternalEnergyStorage(CommonConfig.ALTERNATOR_CAPACITY.get(), 0, CommonConfig.ALTERNATOR_MAX_OUTPUT.get());
		capability = energy;
	}

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(
				Capabilities.Energy.BLOCK,
				CABlockEntities.ALTERNATOR.get(),
				(be, context) -> com.mrh0.createaddition.energy.fe.Fe.asHandler(be.capability));
	}

	@Override
	public float calculateStressApplied() {
		float impact = CommonConfig.MAX_STRESS.get()/256f;
		this.lastStressApplied = impact;
		return impact;
	}

	public boolean isEnergyInput(Direction side) {
		return false;
	}

	public boolean isEnergyOutput(Direction side) {
		return true; //side != getBlockState().getValue(AlternatorBlock.FACING);
	}

	@Override
	protected void read(ValueInput tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		energy.read(tag);
	}

	@Override
	public void writeSafe(ValueOutput tag) {
		super.writeSafe(tag);
		energy.write(tag);
	}

	private boolean firstTickState = true;

	@Override
	public void tick() {
		super.tick();
		if (level == null) return;
		if (level.isClientSide()) return;
		if (firstTickState) firstTick();
		firstTickState = false;

		if (Math.abs(getSpeed()) > 0 && isSpeedRequirementFulfilled())
			energy.internalProduceEnergy(getEnergyProductionRate((int)getSpeed()));

		for (Direction d : Direction.values()) {
			if(!isEnergyOutput(d)) continue;
			IEnergyStorage ies = cache.get(d).getCapability();
			if(ies == null) continue;
			int ext = energy.extractEnergy(ies.receiveEnergy(CommonConfig.ALTERNATOR_MAX_OUTPUT.get(), true), false);
			ies.receiveEnergy(ext, false);
		}
	}

	public static int getEnergyProductionRate(int rpm) {
		rpm = Math.abs(rpm);
		return (int)((double) CommonConfig.FE_RPM.get() * ((double)Math.abs(rpm) / 256d) * CommonConfig.ALTERNATOR_EFFICIENCY.get());//return (int)((double)Config.FE_TO_SU.get() * ((double)Math.abs(rpm)/256d) * EFFICIENCY);
	}

	@Override
	protected Block getStressConfigKey() {
		return CABlocks.ALTERNATOR.get();
	}

	public void firstTick() {
		updateCache();
	}

	public void updateCache() {
		if (level == null) return;
		if (level.isClientSide()) return;
		for (Direction side : Direction.values()) {
			cache.put(side, FeCache.create(
					(ServerLevel) level,
					getBlockPos().relative(side),
					side.getOpposite(),
					() -> !this.isRemoved(),
					() -> invalidSides.add(side)
			));
		}
	}

	@Override
	public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
		return energy;
	}
}
