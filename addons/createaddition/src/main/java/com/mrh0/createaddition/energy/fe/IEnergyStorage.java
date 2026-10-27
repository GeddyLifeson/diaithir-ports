package com.mrh0.createaddition.energy.fe;

/** The pre-26.1 energy interface that Create Crafts and Additions was written against; bridged to the new EnergyHandler. */
public interface IEnergyStorage {
  int receiveEnergy(int maxReceive, boolean simulate);
  int extractEnergy(int maxExtract, boolean simulate);
  int getEnergyStored();
  int getMaxEnergyStored();
  boolean canExtract();
  boolean canReceive();
}
