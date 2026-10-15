package mcjty.rftoolsutility.modules.logic.client;

import net.minecraft.client.renderer.rendertype.RenderTypes;

import mcjty.lib.client.LegacyBlockEntityRenderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.client.RenderHelper;
import mcjty.lib.varia.LogicFacing;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.logic.LogicBlockModule;
import mcjty.rftoolsutility.modules.logic.blocks.DigitTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class DigitRenderer extends LegacyBlockEntityRenderer<DigitTileEntity> {

    public static final Identifier[] DIGITS = new Identifier[] {
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_0"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_1"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_2"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_3"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_4"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_5"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_6"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_7"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_8"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_9"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_a"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_b"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_c"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_d"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_e"),
            Identifier.fromNamespaceAndPath(RFToolsUtility.MODID, "block/logic/machineoutput_f")
    };

    public DigitRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void render(DigitTileEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLightIn, int combinedOverlayIn) {
        BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        Block block = state.getBlock();
        if (!(block instanceof LogicSlabBlock)) {
            return;
        }

        matrixStack.pushPose();
        VertexConsumer builder = buffer.getBuffer(RenderTypes.cutoutMovingBlock());

        LogicFacing logicFacing = state.getValue(LogicSlabBlock.LOGIC_FACING);
        Direction facing = logicFacing.getSide();

        RenderHelper.adjustTransformToDirection(matrixStack, facing);

        int level = te.getPowerLevel();
        if (level < 0) {
            level = 0;
        } else if (level > 15) {
            level = 15;
        }

        TextureAtlasSprite sprite = RenderHelper.blockSprite(DIGITS[level]);

        RenderHelper.FaceRotation rotation = RenderHelper.FaceRotation.values()[logicFacing.getRotationStep()];
        RenderHelper.renderNorthSouthQuad(matrixStack, builder, sprite, rotation, .73f);

        matrixStack.popPose();
    }

    public static void register() {
        BlockEntityRenderers.register(LogicBlockModule.DIGIT.be().get(), DigitRenderer::new);
    }

}
