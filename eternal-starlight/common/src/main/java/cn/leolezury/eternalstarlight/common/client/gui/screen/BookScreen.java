package cn.leolezury.eternalstarlight.common.client.gui.screen;

import net.minecraft.client.renderer.RenderPipelines;
import cn.leolezury.eternalstarlight.common.client.book.BookContext;
import cn.leolezury.eternalstarlight.common.client.book.ResolvedBookDefinition;
import cn.leolezury.eternalstarlight.common.client.book.component.ConfiguredBookComponent;
import cn.leolezury.eternalstarlight.common.client.gui.screen.widget.BookHistoryButton;
import cn.leolezury.eternalstarlight.common.client.gui.screen.widget.BookProgressButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BookScreen extends Screen {
	public static final int OVERLAY_Z_OFFSET = 770;
	public static final int BUTTON_Z_OFFSET = 780;
	public static final int TOOLTIP_Z_OFFSET = 790;

	private final ResolvedBookDefinition book;
	private final BookContext context;
	private final List<List<ConfiguredBookComponent<?, ?>>> unlockedComponents;
	private final List<ConfiguredBookComponent<?, ?>> currentComponents = new ArrayList<>();

	private BookProgressButton upButton;
	private BookProgressButton downButton;
	private BookHistoryButton leftButton;
	private BookHistoryButton rightButton;

	private int tickCount = 0;
	private int totalHeight = 0;
	private int scrollProgress = 0;
	private boolean scrolling = false;
	private int mouseOffset;
	private int mouseX, mouseY;

	private final List<Stamp> stamps = new ArrayList<>();
	private int currentIndex = 0;

	public BookScreen(ResolvedBookDefinition book, Set<Identifier> unlocked, boolean allUnlocked) {
		super(Component.empty());
		this.book = book;
		this.unlockedComponents = new ArrayList<>();
		for (List<ConfiguredBookComponent<?, ?>> innerList : book.components()) {
			this.unlockedComponents.add(new ArrayList<>(innerList));
		}
		if (!allUnlocked) {
			this.unlockedComponents.forEach(list -> list.removeIf(component -> !component.isEnabled(unlocked)));
		}
		this.unlockedComponents.removeIf(List::isEmpty);
		if (!this.unlockedComponents.isEmpty()) {
			this.currentComponents.addAll(this.unlockedComponents.getFirst());
			if (!this.currentComponents.isEmpty()) {
				this.stamps.add(new Stamp(this.currentComponents.getFirst().config().id(), 0));
			}
		}
		this.context = new BookContext() {
			@Override
			public int getMouseX() {
				return mouseX;
			}

			@Override
			public int getMouseY() {
				return mouseY;
			}

			@Override
			public Font getFont() {
				return font;
			}

			@Override
			public ResolvedBookDefinition getBookDefinition() {
				return book;
			}

			@Override
			public int getContentX() {
				return BookScreen.this.getContentX();
			}

			@Override
			public int getContentY() {
				return BookScreen.this.getContentY();
			}

			@Override
			public int getTickCount() {
				return tickCount;
			}

			@Override
			public boolean isComponentEnabled(Identifier id) {
				return unlockedComponents.stream().flatMap(List::stream).anyMatch(component -> component.config().id().equals(id));
			}

			@Override
			public void jumpToComponent(Identifier id) {
				BookScreen.this.jumpToComponent(id, true);
			}
		};
	}

	@Override
	protected void init() {
		updateTotalHeight();
		upButton = addRenderableWidget(new BookProgressButton(getBaseX() + book.width() - book.buttons().upDownButtonWidth() - book.buttons().upDownButtonDistanceFromRight(), getBaseY() + book.buttons().upButtonOffset(), book.definition(), false, button -> {
			setScrollProgress(scrollProgress - font.lineHeight);
		}));
		downButton = addRenderableWidget(new BookProgressButton(getBaseX() + book.width() - book.buttons().upDownButtonWidth() - book.buttons().upDownButtonDistanceFromRight(), getBaseY() + book.buttons().downButtonOffset(), book.definition(), true, button -> {
			setScrollProgress(scrollProgress + font.lineHeight);
		}));
		leftButton = addRenderableWidget(new BookHistoryButton(getBaseX() + book.buttons().leftButtonOffset(), getBaseY() + book.buttons().leftRightButtonDistanceFromTop(), book.definition(), true, button -> {
			if (currentIndex - 1 >= 0 && currentIndex - 1 < stamps.size()) {
				Stamp stamp = stamps.get(currentIndex - 1);
				jumpToComponent(stamp.id(), false);
				setScrollProgress(stamp.progress());
				currentIndex = currentIndex - 1;
			}
		}));
		rightButton = addRenderableWidget(new BookHistoryButton(getBaseX() + book.buttons().rightButtonOffset(), getBaseY() + book.buttons().leftRightButtonDistanceFromTop(), book.definition(), false, button -> {
			if (currentIndex + 1 >= 0 && currentIndex + 1 < stamps.size()) {
				Stamp stamp = stamps.get(currentIndex + 1);
				jumpToComponent(stamp.id(), false);
				setScrollProgress(stamp.progress());
				currentIndex = currentIndex + 1;
			}
		}));
		setScrollProgress(scrollProgress);
		updateButtonVisibility();
	}

	public void jumpToComponent(Identifier id, boolean leaveStamp) {
		if (currentIndex >= 0 && currentIndex < stamps.size()) {
			stamps.set(currentIndex, new Stamp(stamps.get(currentIndex).id(), scrollProgress));
		}
		for (List<ConfiguredBookComponent<?, ?>> list : unlockedComponents) {
			int currentHeight = 0;
			for (ConfiguredBookComponent<?, ?> component : list) {
				if (component.config().id().equals(id)) {
					currentComponents.clear();
					currentComponents.addAll(list);
					updateTotalHeight();
					setScrollProgress(currentHeight);
					if (leaveStamp) {
						List<Stamp> toRemove = new ArrayList<>();
						for (int i = currentIndex + 1; i < stamps.size(); i++) {
							toRemove.add(stamps.get(i));
						}
						stamps.removeIf(toRemove::contains);
						stamps.add(new Stamp(id, scrollProgress));
						currentIndex = stamps.size() - 1;
					}
					rebuildWidgets();
					break;
				}
				currentHeight += component.getTotalHeight(context);
			}
		}
	}

	@Override
	public void mouseMoved(double x, double y) {
		super.mouseMoved(x, y);
		mouseX = (int) x;
		mouseY = (int) y;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double x = event.x();
		double y = event.y();
		int button = event.button();
		boolean result = super.mouseClicked(event, doubleClick);
		if (!result) {
			if (x >= getContentX() && x <= getContentX() + book.width() - 2 * book.frameWidth()
				&& y >= getContentY() && y <= getContentY() + book.height() - 2 * book.frameWidth()) {
				int startHeight = 0;
				for (ConfiguredBookComponent<?, ?> component : new ArrayList<>(currentComponents)) {
					int currentY = getContentY() - scrollProgress + startHeight;
					if (y >= currentY && y <= currentY + component.getTotalHeight(context)) {
						component.onClick(context, getContentX(), getContentY() - scrollProgress + startHeight);
						break;
					}
					startHeight += component.getTotalHeight(context);
				}
			}
			boolean scrollArea = x >= getBaseX() + book.scrollbar().scrollbarXOffset()
				&& x <= getBaseX() + book.scrollbar().scrollbarXOffset() + book.scrollbar().scrollbarWidth()
				&& y >= getBaseY() + book.scrollbar().scrollbarYOffset()
				&& y <= getBaseY() + book.scrollbar().scrollbarYOffset() + book.scrollbar().scrollbarHeight();
			if (scrollArea && button == 0) {
				this.scrolling = true;
				this.mouseOffset = Mth.clamp((int) (y - (getBaseY() + book.scrollbar().scrollbarYOffset() + (int) (getMaxScroll() * ((double) scrollProgress / (double) Math.max(totalHeight - book.height() + 2 * book.frameWidth(), 1))))), 0, getScrollButtonHeight());
				return true;
			}
		}
		return result;
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0) {
			this.scrolling = false;
			this.mouseOffset = 0;
		}

		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		double y = event.y();
		if (this.scrolling) {
			setScrollProgress((int) ((y - this.mouseOffset - getBaseY() - book.scrollbar().scrollbarYOffset()) * (double) Math.max(totalHeight - book.height() + 2 * book.frameWidth(), 1) / (double) Math.max(getMaxScroll(), 1)));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		setScrollProgress(scrollProgress - (int) (scrollY * font.lineHeight));
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int keyCode = event.key();
		boolean up = keyCode == GLFW.GLFW_KEY_UP;
		boolean down = keyCode == GLFW.GLFW_KEY_DOWN;
		if (up || down) {
			setScrollProgress(scrollProgress + (up ? -1 : 1) * font.lineHeight);
		}
		return super.keyPressed(event);
	}

	@Override
	public void tick() {
		tickCount++;
		int startHeight = 0;
		for (ConfiguredBookComponent<?, ?> component : new ArrayList<>(currentComponents)) {
			component.tick(context, getContentX(), getContentY() - scrollProgress + startHeight);
			startHeight += component.getTotalHeight(context);
		}
		if (this.stamps.isEmpty()) {
			this.currentIndex = 0;
		} else {
			this.currentIndex = Mth.clamp(this.currentIndex, 0, this.stamps.size() - 1);
		}
		updateButtonVisibility();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, book.textures().background(), getBaseX(), getBaseY(), (float) 0, (float) 0, book.width(), book.height(), book.width(), book.height());
		guiGraphics.enableScissor(getContentX(), getContentY(), getContentX() + book.width() - 2 * book.frameWidth(), getContentY() + book.height() - 2 * book.frameWidth());
		int startHeight = 0;
		for (ConfiguredBookComponent<?, ?> component : new ArrayList<>(currentComponents)) {
			if (getContentY() - scrollProgress + startHeight < getContentY() + book.height() - 2 * book.frameWidth()
				&& getContentY() - scrollProgress + startHeight + component.getTotalHeight(context) > getContentY()) {
				component.render(context, guiGraphics, getContentX(), getContentY() - scrollProgress + startHeight);
			}
			startHeight += component.getTotalHeight(context);
		}
		guiGraphics.pose().pushMatrix();
		guiGraphics.disableScissor();
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, book.textures().overlay(), getBaseX(), getBaseY(), (float) 0, (float) 0, book.width(), book.height(), book.width(), book.height());
		guiGraphics.pose().popMatrix();
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, book.textures().topOverlay(), getBaseX(), getBaseY(), (float) 0, (float) 0, book.width(), book.height(), book.width(), book.height());
		if (getScrollButtonHeight() < book.scrollbar().scrollbarHeight()) {
			int scrollButtonX = getBaseX() + book.scrollbar().scrollbarXOffset() + (book.scrollbar().scrollbarWidth() - book.scrollbar().scrollButtonWidth()) / 2;
			int scrollButtonY = getBaseY() + book.scrollbar().scrollbarYOffset() + (int) (getMaxScroll() * ((double) scrollProgress / (double) (totalHeight - book.height() + 2 * book.frameWidth())));
			guiGraphics.pose().pushMatrix();
			guiGraphics.fill(scrollButtonX, scrollButtonY, scrollButtonX + book.scrollbar().scrollButtonWidth(), scrollButtonY + getScrollButtonHeight(), book.scrollbar().scrollButtonColor());
			guiGraphics.pose().popMatrix();
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		int startHeight = 0;
		for (ConfiguredBookComponent<?, ?> component : new ArrayList<>(currentComponents)) {
			if (getContentY() - scrollProgress + startHeight < getContentY() + book.height() - 2 * book.frameWidth()
				&& getContentY() - scrollProgress + startHeight + component.getTotalHeight(context) > getContentY()) {
				component.renderDelayed(context, guiGraphics, getContentX(), getContentY() - scrollProgress + startHeight);
			}
			startHeight += component.getTotalHeight(context);
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void updateButtonVisibility() {
		upButton.visible = scrollProgress > 0;
		downButton.visible = scrollProgress < totalHeight - book.height() + 2 * book.frameWidth();
		if (totalHeight <= book.height() - 2 * book.frameWidth()) {
			upButton.visible = false;
			downButton.visible = false;
		}
		leftButton.visible = currentIndex - 1 >= 0 && currentIndex - 1 < stamps.size();
		rightButton.visible = currentIndex + 1 >= 0 && currentIndex + 1 < stamps.size();
	}

	private int getBaseX() {
		return (width - book.width()) / 2;
	}

	private int getContentX() {
		return getBaseX() + book.frameWidth();
	}

	private int getBaseY() {
		return (height - book.height()) / 2;
	}

	private int getContentY() {
		return getBaseY() + book.frameWidth();
	}

	private void updateTotalHeight() {
		totalHeight = 0;
		for (ConfiguredBookComponent<?, ?> component : currentComponents) {
			totalHeight += component.getTotalHeight(context);
		}
	}

	private void setScrollProgress(int progress) {
		this.scrollProgress = Mth.clamp(progress, 0, Math.max(totalHeight - book.height() + 2 * book.frameWidth(), 0));
	}

	private int getMaxScroll() {
		return book.scrollbar().scrollbarHeight() - getScrollButtonHeight();
	}

	private int getScrollButtonHeight() {
		return Math.clamp((int) (book.scrollbar().scrollbarHeight() * (double) (book.height() - 2 * book.frameWidth()) / (double) Math.max(totalHeight, 1)), book.scrollbar().scrollButtonWidth(), book.scrollbar().scrollbarHeight());
	}

	private record Stamp(Identifier id, int progress) {
	}
}
