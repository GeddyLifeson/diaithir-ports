/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.renderer.font;

import li.cil.oc2.api.API;
import net.minecraft.resources.Identifier;

// TODO(oc2-restore): This class used to extend li.cil.manual.api.prefab.renderer.BitmapFontRenderer and
//  implement li.cil.manual.api.render.FontRenderer. The Markdown Manual is not available, so only the
//  font metadata is kept here.
public final class MonospaceFontRenderer {
    public static final MonospaceFontRenderer INSTANCE = new MonospaceFontRenderer();

    private static final Identifier LOCATION_FONT_TEXTURE = Identifier.fromNamespaceAndPath(API.MOD_ID, "textures/font/monospace.png");
    private static final String CHARS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    public int lineHeight() {
        return 9;
    }

    protected CharSequence getCharacters() {
        return CHARS;
    }

    protected Identifier getTextureLocation() {
        return LOCATION_FONT_TEXTURE;
    }

    protected int getResolution() {
        return 96;
    }

    protected int getGapU() {
        return 0;
    }

    protected int getGapV() {
        return 0;
    }

    public int charWidth() {
        return 6;
    }
}
