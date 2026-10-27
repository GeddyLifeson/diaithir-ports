package com.mrh0.createaddition.energy;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import com.mrh0.createaddition.energy.fe.EnergyStorage;
import com.mrh0.createaddition.energy.fe.IEnergyStorage;

public class InternalEnergyStorage extends EnergyStorage {
	public InternalEnergyStorage(int capacity) {
        super(capacity, capacity, capacity, 0);
    }

    public InternalEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer, maxTransfer, 0);
    }

    public InternalEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract, 0);
    }

    public InternalEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }
    
    public ValueOutput write(ValueOutput nbt) {
    	nbt.putInt("energy", energy);
    	return nbt;
    }
    
    public void read(ValueInput nbt) {
    	setEnergy(nbt.getIntOr("energy", 0));
    }
    
    public ValueOutput write(ValueOutput nbt, String name) {
    	nbt.putInt("energy_"+name, energy);
    	return nbt;
    }
    
    public void read(ValueInput nbt, String name) {
    	setEnergy(nbt.getIntOr("energy_"+name, 0));
    }
    
    public int getSpace() {
    	return Math.max(getMaxEnergyStored() - getEnergyStored(), 0);
    }
    
    @Override
    public boolean canExtract() {
    	return maxExtract > 0;
    }
    
    @Override
    public boolean canReceive() {
    	return maxReceive > 0;
    }
    
    public int internalConsumeEnergy(int consume) {
    	int oenergy = energy;
        energy = Math.max(0, energy - consume);
        return oenergy - energy;
    }
    
    public int internalProduceEnergy(int produce) {
    	int oenergy = energy;
        energy = Math.min(capacity, energy + produce);
        return oenergy - energy;
    }
    
    public void setEnergy(int energy) {
    	this.energy = energy;
    }
    
    public void setCapacity(int capacity) {
    	this.capacity = capacity;
    }
    
    @Deprecated
    public void outputToSide(Level level, BlockPos pos, Direction side, int max) {
		IEnergyStorage ies = com.mrh0.createaddition.energy.fe.Fe.block(level, pos, side.getOpposite());
		if(ies == null) return;
		int ext = this.extractEnergy(max, false);
		this.receiveEnergy(ext - ies.receiveEnergy(ext, false), false);
    }
    
    @Override
    public String toString() {
    	return getEnergyStored() + "/" + getMaxEnergyStored() + " <-" + maxExtract + " ->" + maxReceive;
    }
}