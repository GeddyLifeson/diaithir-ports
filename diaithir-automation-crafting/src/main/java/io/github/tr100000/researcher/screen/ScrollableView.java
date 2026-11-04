package io.github.tr100000.researcher.screen;

public interface ScrollableView {
    default boolean isScrollable() {
        return true;
    }

    default int getOffsetX() {
        return 0;
    }

    default int getOffsetY() {
        return 0;
    }

    default double toOffsetX(double x) {
        return x - getOffsetX();
    }

    default double toOffsetY(double y) {
        return y - getOffsetY();
    }

    default double offsetToScreenX(double offsetX) {
        return getOffsetX() + offsetX;
    }

    default double offsetToScreenY(double offsetY) {
        return getOffsetY() + offsetY;
    }

    default float getScale() {
        return 1.0f;
    }
}
