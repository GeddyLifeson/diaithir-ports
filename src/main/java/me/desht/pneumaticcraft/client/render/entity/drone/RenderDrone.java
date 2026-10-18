package me.desht.pneumaticcraft.client.render.entity.drone;

import com.mojang.blaze3d.vertex.PoseStack;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.model.entity.drone.ModelDrone;
import me.desht.pneumaticcraft.common.entity.drone.AbstractDroneEntity;
import me.desht.pneumaticcraft.common.entity.drone.DroneEntity;
import me.desht.pneumaticcraft.common.item.minigun.AbstractGunAmmoItem;
import me.desht.pneumaticcraft.lib.Textures;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RenderDrone extends MobRenderer<AbstractDroneEntity, DroneRenderState, ModelDrone> {
    private final Identifier texture;
    private final float scale;

    private RenderDrone(EntityRendererProvider.Context ctx, float scale, Identifier texture) {
        super(ctx, new ModelDrone(ctx.bakeLayer(PNCModelLayers.DRONE)), 0.25f);

        this.scale = scale;
        this.texture = texture;

        addLayer(new DroneColourLayer(this, ctx));
        addLayer(new DroneHeldItemLayer(this));
        addLayer(new DroneDigLaserLayer(this));
        addLayer(new DroneMinigunLayer(this, ctx));
        addLayer(new DroneTargetLaserLayer(this));
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
    public DroneRenderState createRenderState() {
        return new DroneRenderState();
    }

    @Override
    public void extractRenderState(AbstractDroneEntity drone, DroneRenderState state, float partialTicks) {
        super.extractRenderState(drone, state, partialTicks);

        state.propRotation = net.minecraft.util.Mth.lerp(partialTicks, drone.oldPropRotation, drone.propRotation);
        state.droneColor = drone.getDroneColor();
        state.laserOffsetY = drone.getLaserOffsetY();
        state.laserColor = drone.getLaserColor();
        state.hasMinigun = drone.hasMinigun();

        ItemStack held = drone.getDroneHeldItem();
        if (!held.isEmpty() && !(held.getItem() instanceof AbstractGunAmmoItem && drone.hasMinigun())) {
            itemModelResolver.updateForLiving(state.heldItem, held, ItemDisplayContext.FIXED, drone);
            // items are rendered suspended under the drone, so tools look more natural held upside down
            state.flipHeldItem = !(held.has(net.minecraft.core.component.DataComponents.TOOL) || held.getItem() instanceof HoeItem);
            state.heldItemScale = held.getItem() instanceof BlockItem ? 0.7F : 0.5F;
        } else {
            state.heldItem.clear();
        }

        if (drone instanceof DroneEntity de) {
            state.minigun = de.hasMinigun() ? de.getMinigun() : null;
            state.targetLine = de.getTargetLine();
            state.oldTargetLine = de.getOldTargetLine();
        } else {
            state.minigun = null;
            state.targetLine = null;
            state.oldTargetLine = null;
        }

        var diggingPos = drone.getDugBlock();
        if (diggingPos != null) {
            VoxelShape shape = drone.level().getBlockState(diggingPos).getShape(drone.level(), diggingPos);
            Vec3 target = shape.isEmpty() ?
                    new Vec3(diggingPos.getX() + 0.5, diggingPos.getY() + 0.45, diggingPos.getZ() + 0.5) :
                    shape.bounds().getCenter().add(Vec3.atLowerCornerOf(diggingPos));
            state.digTarget = target.subtract(drone.getX(), drone.getY(), drone.getZ());
        } else {
            state.digTarget = null;
        }
    }

    @Override
    protected void scale(DroneRenderState state, PoseStack poseStack) {
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTextureLocation(DroneRenderState state) {
        return texture;
    }
}
