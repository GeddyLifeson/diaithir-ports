package mcjty.rftoolsutility.modules.spawner.client;

import mcjty.lib.client.LegacyBlockEntityRenderer;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.lib.client.RenderGlowEffect;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsbase.RFToolsBase;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.spawner.SpawnerModule;
import mcjty.rftoolsutility.modules.spawner.blocks.MatterBeamerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class MatterBeamerRenderer extends LegacyBlockEntityRenderer<MatterBeamerTileEntity> {

    public static final Identifier REDGLOW = Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/effects/redglow");
    public static final Identifier BLUEGLOW = Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/effects/blueglow");
    public static final Vec3 START = new Vec3(.5, .5, .5);

    public MatterBeamerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void render(MatterBeamerTileEntity tileEntity, float v, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int i, int i1) {
        Identifier txt;

        // @todo 1.15
        BlockPos destination = tileEntity.getDestination();
        if (destination != null) {
            if (tileEntity.isGlowing()) {
                TextureAtlasSprite sprite = RenderHelper.blockSprite(REDGLOW);

                VertexConsumer builder = buffer.getBuffer(RenderTypes.translucentMovingBlock());

                int tex = tileEntity.getBlockPos().getX();
                int tey = tileEntity.getBlockPos().getY();
                int tez = tileEntity.getBlockPos().getZ();
                Vec3 player = Minecraft.getInstance().gameRenderer.getMainCamera().position().add(-tex, -tey, -tez);
                Vec3 end = new Vec3(destination.getX() - tex + .5f, destination.getY() - tey + .5f, destination.getZ() - tez + .5f);

                RenderHelper.drawBeam(matrixStack, builder, sprite, START, end, player, tileEntity.isGlowing() ? .1f : .05f);
            }
        }

        BlockPos coord = tileEntity.getBlockPos();
        if (coord.equals(RFToolsBase.instance.clientInfo.getSelectedTE())) {
            txt = REDGLOW;
        } else if (coord.equals(RFToolsBase.instance.clientInfo.getDestinationTE())) {
            txt = BLUEGLOW;
        } else {
            txt = null;
        }

        if (txt != null) {
            RenderGlowEffect.renderGlow(matrixStack, buffer, txt);
        }
    }

    public static void register() {
        BlockEntityRenderers.register(SpawnerModule.MATTER_BEAMER.be().get(), MatterBeamerRenderer::new);
    }

    @Override
    public AABB getRenderBoundingBox(MatterBeamerTileEntity be) {
        int xCoord = be.getBlockPos().getX();
        int yCoord = be.getBlockPos().getY();
        int zCoord = be.getBlockPos().getZ();
        return new AABB(xCoord - 4, yCoord - 4, zCoord - 4, xCoord + 5, yCoord + 5, zCoord + 5);
    }
}

