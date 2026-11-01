package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * A decoration placed on a book page. 26.1 port: widgets are drawn flat (2D) in page space,
 * where one unit is one unscaled page pixel. The screen has already translated and scaled the pose
 * to the page origin; the context carries the absolute screen position of the page for widgets that
 * need real screen coordinates (entities).
 */
public abstract class BookWidget {

    @Expose
    @SerializedName("display_page")
    private int displayPage;
    @Expose
    private Type type;
    @Expose
    private int x;
    @Expose
    private int y;
    @Expose
    private float scale;

    public BookWidget(int displayPage, Type type, int x, int y, float scale) {
        this.displayPage = displayPage;
        this.type = type;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public int getDisplayPage() {
        return displayPage;
    }

    public Type getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getScale() {
        return scale;
    }

    public abstract void render(GuiGraphicsExtractor graphics, PageContext ctx, float partialTicks);

    /** Absolute screen origin and scale of the page the widget is being drawn on. */
    public record PageContext(float originX, float originY, float pageScale) {
    }

    public static final int SEPIA_TINT = 0xFFD9BC8C;

    public enum Type {

        @SerializedName("image")
        IMAGE(ImageWidget.class),
        @SerializedName("item")
        ITEM(ItemWidget.class),
        @SerializedName("entity")
        ENTITY(EntityWidget.class),
        @SerializedName("entity_box")
        ENTITY_BOX(EntityBoxWidget.class),
        @SerializedName("crafting_recipe")
        CRAFTING_RECIPE(CraftingRecipeWidget.class);
        private final Class<? extends BookWidget> widgetClass;

        Type(Class<? extends BookWidget> widgetClass) {
            this.widgetClass = widgetClass;
        }

        public Class<? extends BookWidget> getWidgetClass() {
            return widgetClass;
        }

    }
}
