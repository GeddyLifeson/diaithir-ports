/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.proxy.client.render;

import com.buuz135.industrial.api.conveyor.ConveyorUpgradeFactory;
import com.buuz135.industrial.api.transporter.TransporterTypeFactory;
import com.buuz135.industrial.block.transportstorage.ConveyorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The conveyor and transporter upgrade parts are standalone models drawn by the block-entity renderers,
 * since 26.1 has no dynamic baked block model that reads block-entity data.
 */
public class UpgradeModels {

    private static final Map<Identifier, StandaloneModelKey<BlockStateModelPart>> KEYS = new HashMap<>();

    public static void register(ModelEvent.RegisterStandalone event) {
        for (TransporterTypeFactory factory : TransporterTypeFactory.FACTORIES) {
            for (Direction upgradeFacing : factory.getValidFacings()) {
                for (TransporterTypeFactory.TransporterAction action : TransporterTypeFactory.TransporterAction.values()) {
                    add(event, factory.getModel(upgradeFacing, action));
                }
            }
        }
        for (ConveyorUpgradeFactory factory : ConveyorUpgradeFactory.FACTORIES) {
            for (Direction upgradeFacing : factory.getValidFacings()) {
                for (Direction conveyorFacing : ConveyorBlock.FACING.getPossibleValues()) {
                    add(event, factory.getModel(upgradeFacing, conveyorFacing));
                }
            }
        }
    }

    private static void add(ModelEvent.RegisterStandalone event, Identifier model) {
        if (KEYS.containsKey(model)) return;
        StandaloneModelKey<BlockStateModelPart> key = new StandaloneModelKey<>(model::toString);
        KEYS.put(model, key);
        event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(model));
    }

    public static void submit(Identifier model, PoseStack stack, SubmitNodeCollector collector, int light) {
        StandaloneModelKey<BlockStateModelPart> key = KEYS.get(model);
        if (key == null) return;
        BlockStateModelPart part = Minecraft.getInstance().getModelManager().getStandaloneModel(key);
        if (part == null) return;
        collector.submitBlockModel(stack, Sheets.cutoutBlockSheet(), List.of(part), new int[0], light, OverlayTexture.NO_OVERLAY, 0);
    }
}
