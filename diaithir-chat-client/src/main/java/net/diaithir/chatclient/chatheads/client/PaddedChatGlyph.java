// Ported from Chat Heads (MPL-2.0) https://github.com/dzwdz/chat_heads
// Modifications: fixed padding values (config-independent).
package net.diaithir.chatclient.chatheads.client;

import com.mojang.blaze3d.font.GlyphInfo;

// adds padding when used inside the chat
public class PaddedChatGlyph implements GlyphInfo {
    public GlyphInfo glyphInfo;

    public PaddedChatGlyph(GlyphInfo original) {
        this.glyphInfo = original;
    }

    @Override
    public float getAdvance() {
        return glyphInfo.getAdvance()
                + (ChatHeads.customHeadRendering ? ChatHeads.RIGHT_PADDING + Math.round(2 * ChatHeads.THREE_DEE_NESS) : 0);
    }
}
