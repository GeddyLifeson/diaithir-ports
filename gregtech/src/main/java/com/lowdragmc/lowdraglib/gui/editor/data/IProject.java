package com.lowdragmc.lowdraglib.gui.editor.data;

import com.gregtechceu.gtceu.compat.INBTSerializable;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.editor.ILDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.ui.Editor;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;


import javax.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author KilaBash
 * @date 2022/12/9
 * @implNote IProject
 */
public interface IProject extends ILDLRegister, INBTSerializable<CompoundTag> {

    Resources getResources();

    /**
     * Save project
     */
    default void saveProject(Path file) {  /* ported stub */ }

    /**
     * Load project from file. return null if loading failed
     */
    @Nullable
    default IProject loadProject(Path file) { return null; /* ported stub */ }

    IProject newEmptyProject();

    /**
     * Get project work space
     */
    default File getProjectWorkSpace(Editor editor) { return null; /* ported stub */ }

    /**
     * Suffix name of this project
     */
    default String getSuffix() {
        return name();
    }

    /**
     * Fired when project is closed
     */
    default void onClosed(Editor editor) {
    }

    /**
     * Fired when project is opened
     */
    default void onLoad(Editor editor) {  /* ported stub */ }

    /**
     * Attach menu
     * @param name menu name
     * @param menu current menu
     */
    default void attachMenu(Editor editor, String name, TreeBuilder.Menu menu) {

    }

    /**
     * Load resource from nbt data
     */
    default Resources loadResources(CompoundTag tag) {
        return Resources.fromNBT(tag);
    }

}
