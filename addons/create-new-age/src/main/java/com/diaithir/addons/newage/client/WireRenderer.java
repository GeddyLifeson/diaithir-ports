package com.diaithir.addons.newage.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import com.diaithir.addons.newage.CreateNewAge;
import com.diaithir.addons.newage.config.CNAConfig;
import com.diaithir.addons.newage.content.electricity.connector.AbstractElectricalConnector;
import com.diaithir.addons.newage.content.electricity.wire.ElectricWireItem;
import com.diaithir.addons.newage.content.electricity.wire.WireType;

/** Draws the wires between electrical connectors and the wire held by the player. */
public class WireRenderer implements BlockEntityRenderer<AbstractElectricalConnector, WireRenderer.State> {
    public WireRenderer(BlockEntityRendererProvider.Context ignored) {
    }

    public static class State extends BlockEntityRenderState {
        final List<Entry> wires = new ArrayList<>();
        @Nullable Level level;
    }

    record Entry(CNAWire wire, Identifier texture) {
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(AbstractElectricalConnector be, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.wires.clear();
        state.level = be.getLevel();
        if (state.level == null)
            return;
        be.getConnectorPositions().forEach((end, type) -> addConnection(state, be, end, type));
        addHand(state, be, partialTick);
    }

    private void addConnection(State state, AbstractElectricalConnector origin, BlockPos endPos, WireType type) {
        BlockPos pos = origin.getBlockPos();
        // only one of the two connectors draws the wire
        if (pos.compareTo(endPos) >= 0)
            return;
        BlockEntity other = state.level.getBlockEntity(endPos);
        if (!(other instanceof AbstractElectricalConnector end))
            return;
        Vec3 a = origin.getConnectionPoint().add(Vec3.atLowerCornerOf(pos));
        Vec3 b = end.getConnectionPoint().add(Vec3.atLowerCornerOf(endPos));
        state.wires.add(new Entry(newWire(a, b), type.getTextureLocation()));
    }

    private static CNAWire newWire(Vec3 a, Vec3 b) {
        return new CNAWire(a.toVector3f(), b.toVector3f(),
                CNAConfig.getClient().wireSectionsPerMeter.get(),
                CNAConfig.getClient().wireThickness.get().floatValue(),
                CNAConfig.getServer().maxWireLength.get());
    }

    private void addHand(State state, AbstractElectricalConnector be, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !mc.options.getCameraType().isFirstPerson())
            return;
        Level level = player.level();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof ElectricWireItem))
            stack = player.getOffhandItem();
        if (!(stack.getItem() instanceof ElectricWireItem wireItem))
            return;
        BlockPos bound = wireItem.getBoundConnector(stack);
        if (bound == null || !bound.equals(be.getBlockPos()))
            return;

        Vec3 eye = mc.gameRenderer.getMainCamera().position();
        Vec3 view = player.getViewVector(partialTick);
        Vec3 wireEnd = eye.add(view.scale(player.blockInteractionRange()));
        Vec3 wireStart = be.getConnectionPoint().add(Vec3.atLowerCornerOf(bound));

        HitResult looking = mc.hitResult;
        if (looking instanceof BlockHitResult hit) {
            BlockPos lookedPos = hit.getBlockPos();
            BlockEntity lookedBe = level.getBlockEntity(lookedPos);
            boolean snapped = false;
            if (lookedBe != null) {
                if (lookedBe == be)
                    return;
                if (lookedBe instanceof AbstractElectricalConnector other) {
                    if (other.isConnected(be.getBlockPos()))
                        return;
                    wireEnd = other.getConnectionPoint().add(Vec3.atLowerCornerOf(lookedPos));
                    snapped = true;
                }
            }
            if (!snapped) {
                Vec3 v = hit.getLocation().subtract(view.scale(0.1));
                if (eye.distanceToSqr(wireEnd) > eye.distanceToSqr(v))
                    wireEnd = v;
            }
        }

        Identifier texture = wireItem.getWireType().getTextureLocation();
        double distSqr = wireEnd.distanceToSqr(wireStart);
        int max = CNAConfig.getServer().maxWireLength.get();
        if (distSqr >= max * max) {
            if (distSqr > max * max * 4)
                return;
            texture = Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, "textures/wire/red.png");
        }
        state.wires.add(new Entry(newWire(wireStart, wireEnd), texture));
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector queue, CameraRenderState camera) {
        Level level = state.level;
        if (level == null)
            return;
        for (Entry e : state.wires) {
            queue.submitCustomGeometry(poseStack, CNARenderTypes.wire(e.texture()),
                    (pose, consumer) -> e.wire().render(consumer, pose, level));
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(AbstractElectricalConnector be) {
        return AABB.INFINITE;
    }
}
