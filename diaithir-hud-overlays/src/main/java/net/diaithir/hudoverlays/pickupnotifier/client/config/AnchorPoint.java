// Ported from Pick Up Notifier (MPL-2.0) https://github.com/Fuzss/pickupnotifier
// Modifications: removed deprecation marker, otherwise unchanged.
package net.diaithir.hudoverlays.pickupnotifier.client.config;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Vector2i;
import org.joml.Vector2ic;

/** Helper for positioning gui elements inside an area, usually the whole window. */
public enum AnchorPoint {
    TOP_LEFT(-1, -1),
    TOP_CENTER(0, -1),
    TOP_RIGHT(1, -1),
    CENTER_LEFT(-1, 0),
    CENTER(0, 0),
    CENTER_RIGHT(1, 0),
    BOTTOM_LEFT(-1, 1),
    BOTTOM_CENTER(0, 1),
    BOTTOM_RIGHT(1, 1);

    public final Vector2ic normal;

    AnchorPoint(int offsetX, int offsetY) {
        this.normal = new Vector2i(offsetX, offsetY);
    }

    public int getNormalX() {
        return this.normal.x();
    }

    public int getNormalY() {
        return this.normal.y();
    }

    public boolean isLeft() {
        return this.normal.x() == -1;
    }

    public boolean isRight() {
        return this.normal.x() == 1;
    }

    public boolean isTop() {
        return this.normal.y() == -1;
    }

    public boolean isBottom() {
        return this.normal.y() == 1;
    }

    public boolean isHorizontalCenter() {
        return this.getNormalX() == 0;
    }

    public boolean isVerticalCenter() {
        return this.getNormalY() == 0;
    }

    public Positioner createPositioner(int guiWidth, int guiHeight, int elementWidth, int elementHeight) {
        return new PositionerImpl(this.normal.x(), this.normal.y(), guiWidth, guiHeight, elementWidth, elementHeight);
    }

    public interface Positioner {

        int getPosX(int posX);

        int getPosY(int posY);

        ScreenRectangle getRectangle(int posX, int posY);
    }

    private record PositionerImpl(int offsetX, int offsetY, int guiWidth, int guiHeight, int elementWidth,
                                  int elementHeight) implements Positioner {

        @Override
        public int getPosX(int posX) {
            return Math.round(this.guiWidth / 2.0F + this.offsetX * (this.guiWidth / 2.0F - posX)
                    - (this.offsetX + 1) * this.elementWidth / 2.0F);
        }

        @Override
        public int getPosY(int posY) {
            return Math.round(this.guiHeight / 2.0F + this.offsetY * (this.guiHeight / 2.0F - posY)
                    - (this.offsetY + 1) * this.elementHeight / 2.0F);
        }

        @Override
        public ScreenRectangle getRectangle(int posX, int posY) {
            return new ScreenRectangle(this.getPosX(posX), this.getPosY(posY), this.elementWidth, this.elementHeight);
        }
    }
}
