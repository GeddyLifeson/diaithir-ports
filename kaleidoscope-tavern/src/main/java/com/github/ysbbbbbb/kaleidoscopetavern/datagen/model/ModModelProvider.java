package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import org.jspecify.annotations.NonNull;

/**
 * ModelProvider 入口类：只负责协调方块与物品模型生成。
 */
public class ModModelProvider extends ModelProvider {
    private final ModBlockModels blockModelsDelegate;
    private final ModItemModels itemModelsDelegate;

    public ModModelProvider(PackOutput output) {
        super(output, KaleidoscopeTavern.MOD_ID);
        this.blockModelsDelegate = new ModBlockModels(KaleidoscopeTavern.MOD_ID);
        this.itemModelsDelegate = new ModItemModels(KaleidoscopeTavern.MOD_ID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, @NonNull ItemModelGenerators itemModels) {
        blockModelsDelegate.generateAll(blockModels);
        itemModelsDelegate.generateAll(blockModels, itemModels);
    }
}
