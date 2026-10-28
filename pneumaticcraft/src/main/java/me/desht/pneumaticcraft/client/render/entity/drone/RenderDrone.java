package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.client.render.LegacyEntityRenderer;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.List;

public class RenderDrone extends LegacyEntityRenderer<AbstractDroneEntity> {
    private final Identifier texture;
    private final float scale;
    private final ModelDrone model;
    private final List<LegacyLayer> layers;

    private RenderDrone(EntityRendererProvider.Context ctx, float scale, Identifier texture) {
        super(ctx);
        this.shadowRadius = 0.25f;
        this.model = new ModelDrone(ctx.bakeLayer(PNCModelLayers.DRONE));
        this.scale = scale;
        this.texture = texture;
        this.layers = List.of(new DroneColourLayer(), new DroneHeldItemLayer(), new DroneDigLaserLayer(),
                new DroneMinigunLayer(), new DroneTargetLaserLayer());
    }

    public static RenderDrone standard(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.DRONE_ENTITY);
    }

    public static RenderDrone programmableController(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.25f, Textures.DRONE_ENTITY);
    }

    public static RenderDrone logistics(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.LOGISTICS_DRONE_ENTITY);
    }

    public static RenderDrone harvesting(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.HARVESTING_DRONE_ENTITY);
    }

    public static RenderDrone guard(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.GUARD_DRONE_ENTITY);
    }

    public static RenderDrone collector(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.COLLECTOR_DRONE_ENTITY);
    }

    public static RenderDrone amadrone(EntityRendererProvider.Context ctx) {
        return new RenderDrone(ctx, 0.35f, Textures.AMADRONE_ENTITY);
    }

    @Override
    protected void render(AbstractDroneEntity drone, float entityYaw, float partialTicks, PoseStack ps, MultiBufferSource buffer, int light) {
        ps.pushPose();
        ps.scale(scale, scale, scale);
        float bodyYaw = Mth.rotLerp(partialTicks, drone.yBodyRotO, drone.yBodyRot);
        ps.mulPose(Axis.YP.rotationDegrees(180f - bodyYaw));
        ps.scale(-1f, -1f, 1f);
        ps.translate(0f, -1.501f, 0f);

        model.prepareMobModel(drone, 0f, 0f, partialTicks);
        model.renderToBuffer(ps, buffer.getBuffer(RenderTypes.entityCutout(texture)), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
        float age = drone.tickCount + partialTicks;
        for (LegacyLayer layer : layers) {
            layer.render(ps, buffer, light, drone, 0f, 0f, partialTicks, age, 0f, 0f);
        }
        ps.popPose();
    }

    @Override
    public Identifier getTextureLocation(AbstractDroneEntity entity) {
        return texture;
    }
}
