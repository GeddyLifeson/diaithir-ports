package com.lowdragmc.lowdraglib.gui.editor.ui;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.editor.ILDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.data.IProject;
import com.lowdragmc.lowdraglib.gui.editor.ui.view.HistoryView;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.gui.util.TreeNode;
import com.lowdragmc.lowdraglib.gui.widget.DialogWidget;
import com.lowdragmc.lowdraglib.gui.widget.MenuWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.nbt.NbtIo;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2022/11/30
 * @implNote MainPage
 */
@Getter
public abstract class Editor extends WidgetGroup implements ILDLRegister {
    @OnlyIn(Dist.CLIENT)
    public static Editor INSTANCE;
    protected final File workSpace;
    protected IProject currentProject;
    @Setter
    @Nullable
    protected File currentProjectFile;
    protected MenuPanel menuPanel;
    protected StringTabContainer tabPages;
    protected ConfigPanel configPanel;
    protected ResourcePanel resourcePanel;
    protected WidgetGroup floatView;
    protected ToolPanel toolPanel;
    protected String copyType;
    protected Object copied;
    public record HistoryItem(String name, CompoundTag date, @Nullable Object source) { }
    protected final List<HistoryItem> history = new ArrayList<>();
    @Nullable
    protected HistoryItem currentHistory;

    public Editor(String modID) {
        this(new File(LDLib.getLDLibDir(), "assets/" + modID));
        if (LDLib.isClient()) {
            if (!this.workSpace.exists() && !this.workSpace.mkdirs()) {
                LDLib.LOGGER.error("Failed to create work space for mod: " + modID);
            }
        }
    }

    public Editor(File workSpace) {
        super(0, 0, 10, 10);
        setClientSideWidget();
        this.workSpace = workSpace;
    }

    @Override
    public void setGui(ModularUI gui) {  /* ported stub */ }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        setSize(new Size(screenWidth, screenHeight));
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        this.clearAllWidgets();
        initEditorViews();
        var lastPageIndex = tabPages == null ? -1 : tabPages.getTabIndex();
        loadProject(currentProject);
        if (tabPages != null) {
            tabPages.switchTabIndex(lastPageIndex);
        }
    }

    public void initEditorViews() {  /* ported stub */ }

    public DialogWidget openDialog(DialogWidget dialog) { return null; /* ported stub */ }

    public <T, C> MenuWidget<T, C> openMenu(double posX, double posY, TreeNode<T, C> menuNode) { return null; /* ported stub */ }

    public void openMenu(double posX, double posY, TreeBuilder.Menu menuBuilder) {  /* ported stub */ }

    public void addRawHistory(String name, CompoundTag date) {
        addRawHistory(name, date, null);
    }

    public void addRawHistory(String name, CompoundTag date, @Nullable Object source) {
        if (currentProject != null) {
            if (currentHistory != null) {
                var index = history.indexOf(currentHistory);
                if (index >= 0) {
                    while (history.size() > index + 1) {
                        history.remove(history.size() - 1);
                    }
                }
            }
            currentHistory = new HistoryItem(name, date, source);
            history.add(currentHistory);
            // if history view is opened, update it
            for (Widget widget : floatView.widgets) {
                if (widget instanceof HistoryView historyView) {
                    historyView.loadList();
                    break;
                }
            }
        }
    }

    public void addAutoHistory(String name, @Nullable Object source) {  /* ported stub */ }

    public void jumpToHistory(HistoryItem historyItem) {  /* ported stub */ }

    public void loadProject(IProject project) {
        if (currentProject != null && currentProject != project) {
            currentProject.onClosed(this);
            currentProjectFile = null;
        }

        currentProject = project;
        tabPages.clearAllWidgets();
        toolPanel.clearAllWidgets();
        toolPanel.hide(false);
        configPanel.clearAllConfigurators();
        resourcePanel.clear();
        floatView.clearAllWidgets();

        if (currentProject != null) {
            currentProject.onLoad(this);
        }
    }

    public void setCopy(String copyType, Object copied) {
        this.copied = copied;
        this.copyType = copyType;
    }

    public void ifCopiedPresent(String copyType, Consumer<Object> consumer) {
        if (Objects.equals(copyType, this.copyType)) {
            consumer.accept(copied);
        }
    }

    public void askToSaveProject(BooleanConsumer result) {
        DialogWidget.showCheckBox(this, "ldlib.gui.editor.tips.save_project", "ldlib.gui.editor.tips.ask_to_save", isSave -> {
            if (isSave) {
                saveProject(result);
            } else {
                result.accept(false);
            }
        });
    }

    public void saveProject(BooleanConsumer result) {
        if (currentProject != null) {
            if (currentProjectFile == null) {
                saveAsProject(result);
            } else {
                currentProject.saveProject(currentProjectFile.toPath());
                DialogWidget.showNotification(this, "ldlib.gui.editor.menu.save", "ldlib.gui.compass.save_success");
                result.accept(true);
            }
        } else {
            result.accept(false);
        }
    }

    public void saveAsProject(BooleanConsumer result) {
        if (currentProject != null) {
            String suffix = "." + currentProject.getSuffix();
            DialogWidget.showFileDialog(this, "ldlib.gui.editor.tips.save_as", currentProject.getProjectWorkSpace(this), false,
                    DialogWidget.suffixFilter(suffix), file -> {
                        if (file != null && !file.isDirectory()) {
                            if (!file.getName().endsWith(suffix)) {
                                file = new File(file.getParentFile(), file.getName() + suffix);
                            }
                            currentProject.saveProject(file.toPath());
                            currentProjectFile = file;
                            result.accept(true);
                        } else {
                            result.accept(false);
                        }
                    });
        }
    }

    public boolean isCurrentProjectSaved() { return false; /* ported stub */ }

    private boolean isWaitingForSave = false;

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; /* ported stub */ }
}
