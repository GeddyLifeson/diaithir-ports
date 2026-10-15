package mcjty.rftoolspower.modules.monitor.client;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import mcjty.lib.client.LegacyBlockEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.varia.LogicFacing;
import mcjty.rftoolspower.RFToolsPower;
import mcjty.rftoolspower.modules.monitor.MonitorModule;
import mcjty.rftoolspower.modules.monitor.blocks.PowerLevelTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class PowerLevelRenderer extends LegacyBlockEntityRenderer<PowerLevelTileEntity> {

    public static final Identifier[] DIGITS = new Identifier[] {
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_0"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_1"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_2"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_3"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_4"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_5"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_6"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_7"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_8"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/monitor/powerlevel_9")
    };

    public PowerLevelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void render(PowerLevelTileEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        VertexConsumer builder = buffer.getBuffer(RenderTypes.cutoutMovingBlock());

        BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        Block block = state.getBlock();
        if (!(block instanceof LogicSlabBlock)) {
            return;
        }

        matrixStack.pushPose();
        LogicFacing logicFacing = state.getValue(LogicSlabBlock.LOGIC_FACING);
        Direction facing = logicFacing.getSide();

        RenderHelper.adjustTransformToDirection(matrixStack, facing);

        int level = te.getLogicSupport().getPowerOutput();
        if (level > 9) {
            level = 9;
        }

        TextureAtlasSprite sprite = RenderHelper.blockSprite(DIGITS[level]);

        RenderHelper.FaceRotation rotation = RenderHelper.FaceRotation.values()[logicFacing.getRotationStep()];
        RenderHelper.renderNorthSouthQuad(matrixStack, builder, sprite, rotation, .73f);

        matrixStack.popPose();
    }

    public static void register() {
        BlockEntityRenderers.register(MonitorModule.POWER_LEVEL.be().get(), PowerLevelRenderer::new);
    }

}
