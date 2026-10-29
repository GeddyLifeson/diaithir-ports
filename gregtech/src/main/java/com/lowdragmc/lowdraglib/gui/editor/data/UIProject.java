package com.lowdragmc.lowdraglib.gui.editor.data;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.editor.ui.MainPanel;
import com.lowdragmc.lowdraglib.gui.editor.ui.tool.WidgetToolBox;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * @author KilaBash
 * @date 2022/12/4
 * @implNote UIProject
 */
@LDLRegister(name = "ui", group = "editor.ui")
public class UIProject implements IProject {

    public Resources resources;
    public WidgetGroup root;

    private UIProject() {

    }

    public UIProject(Resources resources, WidgetGroup root) {
        this.resources = resources;
        this.root = root;
    }

    public UIProject(CompoundTag tag) {
        deserializeNBT(Platform.getFrozenRegistry(), tag);
    }

    public UIProject(CompoundTag tag, HolderLookup.Provider provider) {
        deserializeNBT(provider, tag);
    }

    public UIProject newEmptyProject() {
        return new UIProject(Resources.defaultResource(),
                (WidgetGroup) new WidgetGroup(30, 30, 200, 200).setBackground(ResourceBorderTexture.BORDERED_BACKGROUND));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) { return null; /* ported stub */ }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {  /* ported stub */ }

    @Override
    public Resources getResources() {
        return resources;
    }

    @Override
    public void saveProject(Path file) {
        try {
            NbtIo.write(serializeNBT(Platform.getFrozenRegistry()), file);
        } catch (IOException ignored) {
            // TODO
        }
    }

    @Override
    public void onLoad(Editor editor) {  /* ported stub */ }

    /**
     * Load ui project file for productive environment.
     * @return an ui creator which caches the resources to speed up the creation process.
     */
    public static Supplier<WidgetGroup> loadUIFromTag(HolderLookup.Provider provider, CompoundTag tag) { return null; /* ported stub */ }

    @Nullable
    public static Supplier<WidgetGroup> loadUIFromFile(Identifier location) {
        try {
            var file = new File(LDLib.getLDLibDir(), "assets/%s/projects/ui/%s.ui".formatted(location.getNamespace(), location.getPath()));
            return loadUIFromTag(Platform.getFrozenRegistry(), NbtIo.read(file.toPath()));
        } catch (Exception e) {
            LDLib.LOGGER.error("Failed to load ui project from file: {}", location, e);
            return null;
        }
    }
}
