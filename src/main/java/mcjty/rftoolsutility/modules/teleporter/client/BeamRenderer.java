package mcjty.rftoolsutility.modules.teleporter.client;

import mcjty.lib.client.LegacyBlockEntityRenderer;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.lib.client.RenderHelper;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.teleporter.TeleportationTools;
import mcjty.rftoolsutility.modules.teleporter.TeleporterModule;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterTransmitterTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class BeamRenderer extends LegacyBlockEntityRenderer<MatterTransmitterTileEntity> {

    public static final Identifier BEAM_OK = Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/machineteleporter");
    public static final Identifier BEAM_WARN = Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/machineteleporterwarn");
    public static final Identifier BEAM_UNKNOWN = Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/machineteleporterunknown");

    public BeamRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void render(MatterTransmitterTileEntity tileEntity, float v, @Nonnull PoseStack matrixStack, @Nonnull MultiBufferSource buffer, int i, int ix) {
        if (tileEntity.isDialed() && !tileEntity.isBeamHidden()) {

            Identifier beamIcon = switch (tileEntity.getStatus()) {
                case TeleportationTools.STATUS_OK -> BEAM_OK;
                case TeleportationTools.STATUS_WARN -> BEAM_WARN;
                default -> BEAM_UNKNOWN;
            };

            //noinspection deprecation
            TextureAtlasSprite sprite = RenderHelper.blockSprite(beamIcon);

            VertexConsumer builder = buffer.getBuffer(RenderTypes.translucentMovingBlock());

            float o = .15f;
            RenderHelper.drawBox(matrixStack, builder, sprite, false, false, true, true, true, true,
                    1-o, o, 0, 4, 1-o, o, RenderHelper.FULLBRIGHT_SETTINGS);
        }

    }

    public static void register() {
        BlockEntityRenderers.register(TeleporterModule.MATTER_TRANSMITTER.be().get(), BeamRenderer::new);
    }

    @Override
    public AABB getRenderBoundingBox(MatterTransmitterTileEntity be) {
        Vec3 v1 = Vec3.atLowerCornerOf(be.getBlockPos());
        Vec3 v2 = Vec3.atLowerCornerOf(be.getBlockPos().offset(1, 4, 1));
        return new AABB(v1, v2);
    }
}
