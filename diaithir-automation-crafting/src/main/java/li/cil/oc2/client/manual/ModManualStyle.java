/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.manual;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// TODO(oc2-restore): Markdown Manual (li.cil.manual) is not available; this no longer implements ManualStyle
// and no longer exposes the monospace FontRenderer. Restore the interface and getMonospaceFont once available.
@OnlyIn(Dist.CLIENT)
public final class ModManualStyle {
    public static final ModManualStyle INSTANCE = new ModManualStyle();

    public int getRegularTextColor() {
        return 0xFFDDDDDD;
    }

    public int getMonospaceTextColor() {
        return 0xFF37FF99;
    }

    public int getRegularLinkColor() {
        return 0xFF9CC6E7;
    }

    public int getHoveredLinkColor() {
        return 0xFFBADCF7;
    }

    public int getRegularDeadLinkColor() {
        return 0xFFFF3755;
    }

    public int getHoveredDeadLinkColor() {
        return 0xFFFF8497;
    }

    public SoundEvent getPageChangeSound() {
        return SoundEvents.UI_BUTTON_CLICK.value();
    }
}
