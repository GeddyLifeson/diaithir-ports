package folk.sisby.antique_atlas.gui.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * Core visual component class, which facilitates hierarchy. You can add child
 * GuiComponent's to it, and they will be rendered, notified about mouse and
 * keyboard events, window resize and will be moved around together with the
 * parent component.
 */
public class GuiComponent extends Screen {
	public GuiComponent parent = null;
	public final List<GuiComponent> children = new CopyOnWriteArrayList<>();

	/**
	 * The component's own size.
	 */
	protected int properWidth;
	protected int properHeight;
	/**
	 * The component's total calculated size, including itself and its children.
	 */
	protected int contentWidth;
	protected int contentHeight;
	/**
	 * If true, this GUI will not be rendered.
	 */
	public boolean isClipped = false;

	/**
	 * guiX and guiY are absolute coordinates on the screen.
	 */
	public int guiX = 0, guiY = 0;

	public GuiComponent() {
		super(Minecraft.getInstance(), Minecraft.getInstance().font, net.minecraft.network.chat.Component.literal("component"));
	}

	/**
	 * Set absolute coordinates of the top left corner of this component on
	 * the screen. If this GUI has a parent, its size will be invalidated.
	 */
	public void setGuiCoords(int x, int y) {
		int dx = x - guiX;
		int dy = y - guiY;
		this.guiX = x;
		this.guiY = y;
		for (GuiComponent child : children) {
			child.offsetGuiCoords(dx, dy);
		}
		if (parent != null && (dx != 0 || dy != 0)) {
			parent.updateSize();
		}
	}

	/**
	 * Set coordinates relative to the parent's (or to the screen, if none)
	 * top left corner.
	 */
	public final void setRelativeCoords(int x, int y) {
		if (parent != null) {
			setGuiCoords(parent.getGuiX() + x, parent.getGuiY() + y);
		} else {
			setGuiCoords(x, y);
		}
	}

	/**
	 * Set x coordinate relative to the parent's (or the screen, if none) left.
	 */
	public final void setRelativeX(int x) {
		if (parent != null) {
			setGuiCoords(parent.getGuiX() + x, guiY);
		} else {
			setGuiCoords(x, guiY);
		}
	}

	/**
	 * Set y coordinate relative to the parent's (or the screen, if none) top.
	 */
	public final void setRelativeY(int y) {
		if (parent != null) {
			setGuiCoords(guiX, parent.getGuiY() + y);
		} else {
			setGuiCoords(guiX, y);
		}
	}

	/**
	 * Offset the component's coordinates by the given values. If the component
	 * has only just been added to a parent component, the result will be the
	 * same as setRelativeGuiCoords().
	 */
	public final void offsetGuiCoords(int dx, int dy) {
		setGuiCoords(guiX + dx, guiY + dy);
	}

	/**
	 * Absolute X coordinate on the screen.
	 */
	public int getGuiX() {
		return guiX;
	}

	/**
	 * Absolute Y coordinate on the screen.
	 */
	public int getGuiY() {
		return guiY;
	}

	/**
	 * X coordinate relative to the parent's top left corner.
	 */
	public int getRelativeX() {
		return parent == null ? guiX : (guiX - parent.guiX);
	}

	/**
	 * Y coordinate relative to the parent's top left corner.
	 */
	public int getRelativeY() {
		return parent == null ? guiY : (guiY - parent.guiY);
	}

	/**
	 * Set this component's own size. This shouldn't affect the size or position of the children.
	 */
	public void setSize(int width, int height) {
		this.properWidth = width;
		this.properHeight = height;
		this.contentWidth = width;
		this.contentHeight = height;
		updateSize();
	}

	/**
	 * Adds the child component to this GUI's content and initializes it.
	 * The child is placed at the top left corner of this component.
	 *
	 * @return the child added.
	 */
	public GuiComponent addChild(GuiComponent child) {
		doAddChild(null, child, null);
		return child;
	}

	/**
	 * Adds the child component to this GUI's content and initializes it.
	 * The child is placed in the list immediately before the specified child,
	 * which is equivalent to putting it behind that child in Z-order.
	 * The child is placed at the top left corner of this component.
	 *
	 * @return the child added.
	 */
	public GuiComponent addChildBehind(GuiComponent behind, GuiComponent child) {
		doAddChild(null, child, behind);
		return child;
	}

	public void doAddChild(GuiComponent inFrontOf, GuiComponent child, GuiComponent behind) {
		if (child == null || children.contains(child) || parent == child) {
			return;
		}
		int i = children.indexOf(inFrontOf);
		if (i == -1) {
			int j = children.indexOf(behind);
			if (j == -1) {
				children.add(child);
			} else {
				children.add(j, child);
			}
		} else {
			children.add(i + 1, child);
		}
		child.parent = this;
		child.setGuiCoords(guiX, guiY);
		child.width = width;
		child.height = height;
		child.init();
		updateSize();
	}

	/**
	 * @return the child removed.
	 */
	public GuiComponent removeChild(GuiComponent child) {
		if (child != null && children.contains(child)) {
			child.parent = null;
			children.remove(child);
			updateSize();
			onChildClosed(child);
		}
		return child;
	}

	public void removeAllChildren() {
		children.clear();
		updateSize();
	}

	/**
	 * Null if this is a top-level GUI.
	 */
	public GuiComponent getParent() {
		return parent;
	}

	public List<GuiComponent> getChildren() {
		return children;
	}

	public boolean iterateInput(Predicate<GuiComponent> callMethod) {
		// Traverse children backwards, because the topmost child should be the
		// first to process input:
		ListIterator<GuiComponent> iter = children.listIterator(children.size());
		while (iter.hasPrevious()) {
			GuiComponent child = iter.previous();
			if (callMethod.test(child)) {
				return true;
			}
		}

		return false;
	}

	// The 26.1 input events are objects; the component tree keeps the older (x, y, button) style internally.
	@Override
	public final boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return mouseClicked(event.x(), event.y(), event.button());
	}

	@Override
	public final boolean mouseReleased(MouseButtonEvent event) {
		return mouseReleased(event.x(), event.y(), event.button());
	}

	@Override
	public final boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		return mouseDragged(event.x(), event.y(), event.button(), dx, dy);
	}

	@Override
	public final boolean keyPressed(KeyEvent event) {
		return keyPressed(event.key(), event.scancode(), event.modifiers());
	}

	@Override
	public final boolean keyReleased(KeyEvent event) {
		return keyReleased(event.key(), event.scancode(), event.modifiers());
	}

	@Override
	public final boolean charTyped(CharacterEvent event) {
		return charTyped((char) event.codepoint(), 0);
	}

	public static boolean shiftDown() {
		return Minecraft.getInstance().hasShiftDown();
	}

	public static boolean altDown() {
		return Minecraft.getInstance().hasAltDown();
	}

	public boolean mouseClicked(double mx, double my, int mb) {
		return iterateInput((c) -> c.mouseClicked(mx, my, mb));
	}

	public boolean mouseReleased(double mx, double my, int mb) {
		return iterateInput((c) -> c.mouseReleased(mx, my, mb));
	}

	public boolean mouseDragged(double mx, double my, int mb, double mx2, double my2) {
		return iterateInput((c) -> c.mouseDragged(mx, my, mb, mx2, my2));
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double dx, double dy) {
		return iterateInput((c) -> c.mouseScrolled(mx, my, dx, dy));
	}

	@Override
	public void mouseMoved(double mx, double my) {
		iterateInput((c) -> {
			c.mouseMoved(mx, my);
			return false;
		});
	}

	/**
	 * Handle keyboard input for this GUI and its children.
	 */
	public boolean keyPressed(int a, int b, int c) {
		return iterateInput((cpt) -> cpt.keyPressed(a, b, c));
	}

	public boolean charTyped(char aa, int bb) {
		return iterateInput((cpt) -> cpt.charTyped(aa, bb));
	}

	public boolean keyReleased(int a, int b, int c) {
		return iterateInput((cpt) -> cpt.keyReleased(a, b, c));
	}

	/**
	 * Render this GUI and its GuiComponent children.
	 * Drawable children are not rendered.
	 */
	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
		// Do not call super() as it would render the background
		for (GuiComponent child : children) {
			if (!child.isClipped) {
				child.extractRenderState(context, mouseX, mouseY, partialTick);
			}
		}
	}

	/**
	 * Called when the GUI is unloaded, called for each child as well.
	 */
	@Override
	public void onClose() {
		for (GuiComponent child : children) {
			child.onClose();
		}
		super.onClose();
	}

	/**
	 * Called each in-game tick for this GUI and its children. If this GUI's
	 * size has been invalidated, it will be validated on the next update.
	 */
	@Override
	public void tick() {
		for (GuiComponent child : children) {
			child.tick();
		}
		super.tick();
	}

	@Override
	public void init() {
		super.init();
		for (GuiComponent child : children) {
			child.width = width;
			child.height = height;
			child.init();
		}
	}

	/**
	 * Width of the GUI or its contents. This method may be called often so it
	 * should be fast.
	 */
	public int getWidth() {
		return contentWidth;
	}

	/**
	 * Height of the GUI or its contents. This method may be called often so it
	 * should be fast.
	 */
	public int getHeight() {
		return contentHeight;
	}

	/**
	 * If set to true, the parent of this GUI will not render it.
	 */
	public void setClipped(boolean value) {
		this.isClipped = value;
	}

	public void updateSize() {
		int leftmost = Integer.MAX_VALUE;
		int rightmost = Integer.MIN_VALUE;
		int topmost = Integer.MAX_VALUE;
		int bottommost = Integer.MIN_VALUE;
		for (GuiComponent child : children) {
			int x = child.getGuiX();
			if (x < leftmost) {
				leftmost = x;
			}
			int childWidth = child.getWidth();
			if (x + childWidth > rightmost) {
				rightmost = x + childWidth;
			}
			int y = child.getGuiY();
			if (y < topmost) {
				topmost = y;
			}
			int childHeight = child.getHeight();
			if (y + childHeight > bottommost) {
				bottommost = y + childHeight;
			}
		}
		contentWidth = Math.max(properWidth, rightmost - leftmost);
		contentHeight = Math.max(properHeight, bottommost - topmost);
		if (parent != null) parent.updateSize();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= getGuiX() && mouseX < getGuiX() + getWidth() && mouseY >= getGuiY() && mouseY < getGuiY() + getHeight();
	}

	/**
	 * Remove itself from its parent component (if any), notifying it.
	 */
	public void closeChild() {
		if (parent != null) {
			parent.removeChild(this); // This sets parent to null
		} else {
			Minecraft.getInstance().setScreen(null);
		}
	}

	/**
	 * Called when a child removes itself from this component.
	 */
	public void onChildClosed(GuiComponent child) {
	}

	/**
	 * Draw a text string centered horizontally, using this GUI's font.
	 */
	public void drawCentered(GuiGraphicsExtractor context, net.minecraft.network.chat.Component text, int y, int color, boolean dropShadow) {
		int length = this.font.width(text);
		context.text(font, text, (this.width - length) / 2, y, color, dropShadow);
	}

	public double getMouseX() {
		return Minecraft.getInstance().mouseHandler.xpos() * width / Minecraft.getInstance().getWindow().getScreenWidth();
	}

	public double getMouseY() {
		return Minecraft.getInstance().mouseHandler.ypos() * height / Minecraft.getInstance().getWindow().getScreenHeight();
	}
}
