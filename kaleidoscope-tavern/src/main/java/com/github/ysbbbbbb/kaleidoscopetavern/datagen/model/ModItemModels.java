package com.github.ysbbbbbb.kaleidoscopetavern.datagen.model;

import com.github.ysbbbbbb.kaleidoscopetavern.datagen.model.catalog.*;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.Optional;

public final class ModItemModels {
    private final String modId;

    ModItemModels(String modId) {
        this.modId = modId;
    }

    void generateAll(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        ItemModelOutput itemOutput = itemModels.itemModelOutput;

        itemOutput.accept(ModItems.BASE_SANDWICH_BOARD.get(), ItemModelUtils.plainModel(modLoc("item/base_sandwich_board")));
        itemOutput.accept(ModItems.BARREL.get(), ItemModelUtils.plainModel(modLoc("item/barrel")));

        itemModels.generateFlatItem(ModItems.CHALKBOARD.get(), ModelTemplates.FLAT_ITEM);

        for (BlockItemCatalogEntry barStool : BarStoolModelCatalog.entries()) {
            Item barStoolItem = barStool.item().get();
            Identifier modelId = ModelLocationUtils.getModelLocation(barStoolItem);
            Material particle = vanillaTexture("block/" + barStool.path() + "_wool");
            Material texture = blockTexture("block/deco/bar_stool/" + barStool.path());
            Identifier parent = modLoc("item/bar_stool_base");
            ModelTemplate template = new ModelTemplate(Optional.of(parent), Optional.empty(),
                    TextureSlot.TEXTURE, TextureSlot.PARTICLE);
            TextureMapping mapping = new TextureMapping()
                    .put(TextureSlot.TEXTURE, texture)
                    .put(TextureSlot.PARTICLE, particle);
            template.create(modelId, mapping, blockModels.modelOutput);
            itemOutput.accept(barStoolItem, ItemModelUtils.plainModel(modelId));
        }

        for (BlockItemCatalogEntry sofa : SofaModelCatalog.entries()) {
            Item sofaItem = sofa.item().get();
            Identifier modelId = ModelLocationUtils.getModelLocation(sofaItem);
            Material particle = vanillaTexture("block/" + sofa.path() + "_wool");
            Material texture = blockTexture("block/deco/sofa/" + sofa.path());
            Identifier parent = modLoc("block/deco/sofa/base/single");
            ModelTemplate template = new ModelTemplate(Optional.of(parent), Optional.empty(),
                    TextureSlot.TEXTURE, TextureSlot.PARTICLE);
            TextureMapping mapping = new TextureMapping()
                    .put(TextureSlot.TEXTURE, texture)
                    .put(TextureSlot.PARTICLE, particle);
            template.create(modelId, mapping, blockModels.modelOutput);
            itemOutput.accept(sofaItem, ItemModelUtils.plainModel(modelId));
        }

        for (BlockItemCatalogEntry sandwichBoard : SandwichBoardModelCatalog.entries()) {
            Item sandwichItem = sandwichBoard.item().get();
            Identifier modelId = ModelLocationUtils.getModelLocation(sandwichItem);
            Identifier parent = modLoc("item/deco_sandwich_board");
            Material layer1 = blockTexture("block/deco/sandwich_board/" + sandwichBoard.path());
            ModelTemplate template = new ModelTemplate(Optional.of(parent), Optional.empty(), TextureSlot.LAYER1);
            TextureMapping mapping = new TextureMapping().put(TextureSlot.LAYER1, layer1);
            template.create(modelId, mapping, blockModels.modelOutput);
            itemOutput.accept(sandwichItem, ItemModelUtils.plainModel(modelId));
        }

        for (BlockItemCatalogEntry stringLights : StringLightsModelCatalog.entries()) {
            Item item = stringLights.item().get();
            Identifier blockModel = modLoc("block/deco/string_lights/" + stringLights.path());
            itemOutput.accept(item, ItemModelUtils.plainModel(blockModel));
        }

        for (BlockItemCatalogEntry painting : PaintingModelCatalog.entries()) {
            Item paintingItem = painting.item().get();
            Identifier modelId = ModelLocationUtils.getModelLocation(paintingItem);
            Material layer0 = blockTexture("block/deco/painting/" + painting.path());
            ModelTemplates.FLAT_ITEM.create(modelId, TextureMapping.layer0(layer0), blockModels.modelOutput);
            itemOutput.accept(paintingItem, ItemModelUtils.plainModel(modelId));
        }

        itemOutput.accept(ModItems.BAR_COUNTER.get(),
                ItemModelUtils.plainModel(modLoc("block/deco/bar_counter/single")));

        itemModels.generateFlatItem(ModItems.STEPLADDER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GRAPEVINE.get(), ModelTemplates.FLAT_ITEM);

        itemOutput.accept(ModItems.TRELLIS.get(),
                ItemModelUtils.plainModel(modLoc("block/plant/trellis/single")));

        itemModels.generateFlatItem(ModItems.GRAPE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ICE_GRAPE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GOLD_GRAPE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GREEN_GRAPE.get(), ModelTemplates.FLAT_ITEM);

        itemOutput.accept(ModItems.PRESSING_TUB.get(),
                ItemModelUtils.plainModel(modLoc("block/brew/pressing_tub")));

        itemOutput.accept(ModItems.BAR_CABINET.get(),
                ItemModelUtils.plainModel(modLoc("block/brew/bar_cabinet/single")));
        itemOutput.accept(ModItems.GLASS_BAR_CABINET.get(),
                ItemModelUtils.plainModel(modLoc("block/brew/glass_bar_cabinet/single")));

        itemOutput.accept(ModItems.TABLE.get(),
                ItemModelUtils.plainModel(modLoc("block/deco/table/single")));

        itemModels.generateFlatItem(ModItems.GRAPE_BUCKET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.ICE_GRAPE_BUCKET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GOLD_GRAPE_BUCKET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GREEN_GRAPE_BUCKET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.SWEET_BERRIES_BUCKET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.GLOW_BERRIES_BUCKET.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.TAP.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.EMPTY_BOTTLE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.MOLOTOV.get(), ModelTemplates.FLAT_ITEM);

        for (BlockItemCatalogEntry drink : DrinkModelCatalog.entries()) {
            itemModels.generateFlatItem(drink.item().get(), ModelTemplates.FLAT_ITEM);
        }
    }

    private Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(modId, path);
    }

    private Material blockTexture(String path) {
        return new Material(Identifier.fromNamespaceAndPath(modId, path));
    }

    private Material vanillaTexture(String path) {
        return new Material(Identifier.withDefaultNamespace(path));
    }
}
