/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;

public class DroneMinigunLayer extends LegacyLayer {
    private final ModelMinigun modelDroneMinigun;

    DroneMinigunLayer() {

        modelDroneMinigun = new ModelMinigun(Minecraft.getInstance().getEntityModels().bakeLayer(PNCModelLayers.MINIGUN));
    }

    @Override
    protected void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, AbstractDroneEntity entityIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entityIn instanceof DroneEntity drone) {
            if (drone.hasMinigun()) {
                modelDroneMinigun.renderMinigun(matrixStackIn, bufferIn, packedLightIn, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, drone.getMinigun(), partialTicks, true);
            }
        }
    }
}
