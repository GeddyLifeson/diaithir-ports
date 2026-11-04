/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.manual;

import li.cil.oc2.api.API;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// TODO(oc2-restore): Re-implement li.cil.manual.api.ManualScreenStyle once the Markdown Manual library is available.
@OnlyIn(Dist.CLIENT)
public final class ModManualScreenStyle {
    public static final ModManualScreenStyle INSTANCE = new ModManualScreenStyle();

    public Identifier getWindowBackground() {
        return Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/gui/manual/manual.png");
    }

    public Identifier getScrollButtonTexture() {
        return Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/gui/manual/scroll_button.png");
    }

    public Identifier getTabButtonTexture() {
        return Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/gui/manual/tab_button.png");
    }

    public Rect2i getDocumentRect() {
        return new Rect2i(12, 12, 216, 232);
    }

    public Rect2i getScrollBarRect() {
        return new Rect2i(236, 8, 12, 240);
    }

    public Rect2i getScrollButtonRect() {
        return new Rect2i(0, 0, 12, 12);
    }

    public Rect2i getTabAreaRect() {
        return new Rect2i(-52, 12, 52, 232);
    }

    public Rect2i getTabRect() {
        return new Rect2i(0, 0, 64, 24);
    }

    public int getTabOverlap() {
        return 0;
    }
}
