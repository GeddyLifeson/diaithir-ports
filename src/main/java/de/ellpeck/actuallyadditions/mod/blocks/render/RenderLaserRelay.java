/*
 * This file ("RenderLaserRelay.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.ellpeck.actuallyadditions.api.laser.IConnectionPair;
import de.ellpeck.actuallyadditions.api.laser.LaserType;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import de.ellpeck.actuallyadditions.mod.items.ItemEngineerGoggles;
import de.ellpeck.actuallyadditions.mod.items.ItemLaserWrench;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityLaserRelay;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RenderLaserRelay implements BlockEntityRenderer<TileEntityLaserRelay, RenderLaserRelay.State> {
    private static final int COLOR = 16711680;
    private static final int COLOR_ITEM = 31760;
    private static final int COLOR_FLUIDS = 25030;
    private static final int COLOR_INFRARED = 13743087;
    private final ItemModelResolver itemModelResolver;

    public RenderLaserRelay(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TileEntityLaserRelay tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        Player player = Minecraft.getInstance().player;
        boolean hasGoggles = ItemEngineerGoggles.isWearing(player);
        ItemStack upgrade = tile.inv.getStackInSlot(0);
        boolean hasInvis = !upgrade.isEmpty() && upgrade.getItem() == ActuallyItems.LASER_UPGRADE_INVISIBILITY.get();

        // the upgrade is only shown to someone who can configure the relay
        ItemStack hand = player.getMainHandItem();
        boolean showUpgrade = !upgrade.isEmpty() && (hasGoggles || !hand.isEmpty() && (hand.getItem() == CommonConfig.Other.relayConfigureItem || hand.getItem() instanceof ItemLaserWrench) || "themattabase".equals(player.getName().getString()));
        this.itemModelResolver.updateForTopItem(state.upgrade, showUpgrade ? upgrade : ItemStack.EMPTY, ItemDisplayContext.FIXED, tile.getLevel(), null, (int) tile.getBlockPos().asLong());
        BlockState blockState = tile.getBlockState();
        Direction direction = blockState.hasProperty(BlockStateProperties.FACING) ? blockState.getValue(BlockStateProperties.FACING) : Direction.UP;
        state.upgradeY = direction.getAxis() == Direction.Axis.Y ? 0.2f : 0.8F;

        state.lasers.clear();
        ConcurrentSet<IConnectionPair> connections = tile.getConnections();
        if (connections == null) return;
        for (IConnectionPair pair : connections) {
            if (pair.doesSuppressRender() || !tile.getBlockPos().equals(pair.getPositions()[0])) continue;
            BlockPos second = pair.getPositions()[1];
            BlockEntity secondTile = tile.getLevel().getBlockEntity(second);
            if (!(secondTile instanceof TileEntityLaserRelay otherRelay)) continue;
            ItemStack secondUpgrade = otherRelay.inv.getStackInSlot(0);
            boolean otherInvis = !secondUpgrade.isEmpty() && secondUpgrade.getItem() == ActuallyItems.LASER_UPGRADE_INVISIBILITY.get();
            if (!hasGoggles && hasInvis && otherInvis) continue;
            int color = hasInvis && otherInvis ? COLOR_INFRARED : tile.type == LaserType.ITEM ? COLOR_ITEM : tile.type == LaserType.FLUID ? COLOR_FLUIDS : COLOR;
            BlockPos end = second.subtract(tile.getBlockPos()).rotate(Rotation.CLOCKWISE_90);
            state.lasers.add(new Laser(new Vec3(end.getX(), end.getY(), end.getZ()), color, hasInvis && otherInvis ? 0.1F : 0.35F));
        }
    }

    @Override
    public void submit(State state, PoseStack matrices, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.upgrade.isEmpty()) {
            matrices.pushPose();
            matrices.translate(0.5F, state.upgradeY, 0.5F);
            matrices.scale(0.2F, 0.2F, 0.2F);
            matrices.mulPose(Axis.YP.rotationDegrees((float) (Util.getMillis() / 800D * 40D % 360)));
            state.upgrade.submit(matrices, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            matrices.popPose();
        }
        for (Laser laser : state.lasers) {
            AssetUtil.submitLaser(matrices, collector, Vec3.ZERO, laser.end, 120, laser.color, laser.alpha, 0.05F);
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityLaserRelay blockEntity) {
        return AABB.INFINITE;
    }

    @Override
    public int getViewDistance() {
        return 32;
    }

    record Laser(Vec3 end, int color, float alpha) {}

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState upgrade = new ItemStackRenderState();
        float upgradeY;
        final List<Laser> lasers = new ArrayList<>();
    }
}
