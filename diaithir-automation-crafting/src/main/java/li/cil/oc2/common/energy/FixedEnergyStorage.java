/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.energy;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.energy.EnergyStorage;

public final class FixedEnergyStorage extends EnergyStorage {
    public static final String STORED_TAG_NAME = "stored";
    public static final String CAPACITY_TAG_NAME = "capacity";

    public FixedEnergyStorage(final int capacity) {
        super(capacity);
    }

    @Override
    public void serialize(final ValueOutput output) {
        output.putInt(STORED_TAG_NAME, energy);
        output.putInt(CAPACITY_TAG_NAME, capacity); // Mostly for tooltips.
    }

    @Override
    public void deserialize(final ValueInput input) {
        energy = input.getIntOr(STORED_TAG_NAME, 0);
    }
}
