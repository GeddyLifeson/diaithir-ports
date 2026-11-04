package net.diaithir.entityanimation.notenoughanimations.client;

import java.util.Locale;

enum PoseType {
    NONE, TOOL, BLOCK, RANGED, FOOD, SHIELD, TWOHANDED;

    static PoseType parse(String s) {
        try {
            return valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
