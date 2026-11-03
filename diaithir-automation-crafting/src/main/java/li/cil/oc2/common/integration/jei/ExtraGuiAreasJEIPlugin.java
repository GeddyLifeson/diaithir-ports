/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.integration.jei;

// TODO(oc2-restore): JEI (mezz.jei) is not available in this build. The original plugin registered
//  the extra GUI areas of AbstractMachineInventoryScreen and AbstractMachineTerminalScreen
//  and removed ComputerBlock.getPreconfiguredComputer() from the JEI ingredient list.
//  Restore the IModPlugin implementation once JEI is available again.
@SuppressWarnings("unused")
public final class ExtraGuiAreasJEIPlugin {
    private ExtraGuiAreasJEIPlugin() {
    }
}
