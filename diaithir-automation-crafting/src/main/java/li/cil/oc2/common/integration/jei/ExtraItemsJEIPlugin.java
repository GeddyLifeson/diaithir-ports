/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.integration.jei;

// TODO(oc2-restore): JEI (mezz.jei) is not available in the 26.1.2 compile stage. The original plugin only
// registered JEI subtype interpreters for the computer, robot and custom flash memory items, so it is
// reduced to a no-op here. Restore the IModPlugin implementation once JEI is available.
@SuppressWarnings("unused")
public final class ExtraItemsJEIPlugin {
    private ExtraItemsJEIPlugin() {
    }
}
