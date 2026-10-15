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

package com.buuz135.industrial.entity.client;// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.15 - 1.16
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class InfinityNukeModelArmed extends Model<Unit> {

    public InfinityNukeModelArmed(ModelPart model) {
        super(model, RenderTypes::entityTranslucent);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation size) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        partDefinition.addOrReplaceChild("top", CubeListBuilder.create()
                        .texOffs(60, 53).addBox(10.0f, -10.4f, -11.0f, -4.0f, 3.0f, 4.0f, size)
                        .texOffs(48, 24).addBox(6.0f, -12.4f, -11.0f, 4.0f, 1.0f, 4.0f, size)
                        .texOffs(32, 52).addBox(4.0f, -11.4f, -13.0f, 8.0f, 4.0f, 8.0f, size),
                PartPose.offsetAndRotation(-8.0f, 17.4f, 8.0f, 0.0f, 0.0f, 0.0f));

        partDefinition.addOrReplaceChild("bottom", CubeListBuilder.create()
                        .texOffs(60, 53).addBox(10.0f, -7.4f, -11.0f, -4.0f, 3.0f, 4.0f, size).mirror()
                        .texOffs(0, 52).addBox(4.0f, -7.4f, -13.0f, 8.0f, 4.0f, 8.0f, size),
                PartPose.offsetAndRotation(-8.0f, 17.4f, 8.0f, 0.0f, 0.0f, 0.0f));

        partDefinition.addOrReplaceChild("core", CubeListBuilder.create()
                        .texOffs(50, 35).addBox(5.0f, -7.9f, -9.5f, 6.0f, 1.0f, 1.0f, size)
                        .texOffs(52, 29).addBox(6.5f, -8.9f, -10.5f, 3.0f, 3.0f, 3.0f, size),
                PartPose.offsetAndRotation(-8.0f, 17.4f, 8.0f, 0.0f, 0.0f, 0.0f));

        partDefinition.addOrReplaceChild("leg_left", CubeListBuilder.create()
                        .texOffs(49, 5).addBox(11.0f, -16.4f, -10.5f, 2.0f, 16.0f, 3.0f, size)
                        .texOffs(56, 37).addBox(11.0f, 1.6f, -13.0f, 2.0f, 4.0f, 2.0f, size).mirror()
                        .texOffs(56, 43).addBox(11.0f, 1.6f, -7.0f, 2.0f, 4.0f, 2.0f, size)
                        .texOffs(0, 42).addBox(11.0f, -0.4f, -13.0f, 2.0f, 2.0f, 8.0f, size),
                PartPose.offsetAndRotation(-8.0f, 18.4f, 8.0f, 0.0f, 0.0f, 0.0f));

        partDefinition.addOrReplaceChild("leg_right", CubeListBuilder.create()
                        .texOffs(56, 37).addBox(3.0f, 2.6f, -13.0f, 2.0f, 4.0f, 2.0f, size)
                        .texOffs(0, 42).addBox(3.0f, 0.6f, -13.0f, 2.0f, 2.0f, 8.0f, size)
                        .texOffs(56, 43).addBox(3.0f, 2.6f, -7.0f, 2.0f, 4.0f, 2.0f, size)
                        .texOffs(54, 5).addBox(3.0f, -15.4f, -10.5f, 2.0f, 16.0f, 3.0f, size),
                PartPose.offsetAndRotation(-8.0f, 17.4f, 8.0f, 0.0f, 0.0f, 0.0f));

        partDefinition.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(42, 0).addBox(-5.0f, -23.0f, -1.5f, 10.0f, 4.0f, 1.0f, size),
                PartPose.offsetAndRotation(0.0f, 24.0f, 0.0f, 0.0f, 0.0f, 0.0f));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}
