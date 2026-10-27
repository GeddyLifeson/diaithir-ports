package net.id.paradise_lost.client.rendering.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.id.paradise_lost.ModConstants;
import net.id.paradise_lost.block.blockentity.CalciteDecoratedPotBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartNames;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

import static net.minecraft.client.renderer.Sheets.DECORATED_POT_SHEET;

public class CalciteDecoratedPotBlockEntityRenderer implements BlockEntityRenderer<CalciteDecoratedPotBlockEntity, CalciteDecoratedPotBlockEntityRenderer.State> {

    public static final SpriteId CALCITE_DECORATED_POT_BASE = createDecoratedPotPatternTextureId(ModConstants.locate("calcite_decorated_pot_base"));
    public static final SpriteId CALCITE_DECORATED_POT_SIDE = createDecoratedPotPatternTextureId(ModConstants.locate("calcite_decorated_pot_side"));

    private static SpriteId createDecoratedPotPatternTextureId(Identifier patternId) {
        return new SpriteId(DECORATED_POT_SHEET, patternId.withPrefix("entity/decorated_pot/"));
    }

    public static class State extends BlockEntityRenderState {
        public Direction direction = Direction.NORTH;
        public PotDecorations decorations = PotDecorations.EMPTY;
        public CalciteDecoratedPotBlockEntity.@Nullable WobbleType wobbleType;
        public float wobbleProgress;
    }

    private final SpriteGetter sprites;
    private final ModelPart neck;
    private final ModelPart front;
    private final ModelPart back;
    private final ModelPart left;
    private final ModelPart right;
    private final ModelPart top;
    private final ModelPart bottom;

    public CalciteDecoratedPotBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        ModelPart modelPart = context.bakeLayer(ModelLayers.DECORATED_POT_BASE);
        this.neck = modelPart.getChild(PartNames.NECK);
        this.top = modelPart.getChild("top");
        this.bottom = modelPart.getChild("bottom");
        ModelPart modelPart2 = context.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
        this.front = modelPart2.getChild("front");
        this.back = modelPart2.getChild("back");
        this.left = modelPart2.getChild("left");
        this.right = modelPart2.getChild("right");
    }

    public static LayerDefinition getTopBottomNeckTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        CubeDeformation dilation = new CubeDeformation(0.2F);
        CubeDeformation dilation2 = new CubeDeformation(-0.1F);
        modelPartData.addOrReplaceChild(
                PartNames.NECK,
                CubeListBuilder.create().texOffs(0, 0).addBox(4.0F, 17.0F, 4.0F, 8.0F, 3.0F, 8.0F, dilation2).texOffs(0, 5).addBox(5.0F, 20.0F, 5.0F, 6.0F, 1.0F, 6.0F, dilation),
                PartPose.offsetAndRotation(0.0F, 37.0F, 16.0F, (float) Math.PI, 0.0F, 0.0F)
        );
        CubeListBuilder modelPartBuilder = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0F, 0.0F, 0.0F, 14.0F, 0.0F, 14.0F);
        modelPartData.addOrReplaceChild("top", modelPartBuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        modelPartData.addOrReplaceChild("bottom", modelPartBuilder, PartPose.offsetAndRotation(1.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(modelData, 32, 32);
    }

    public static LayerDefinition getSidesTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        CubeListBuilder modelPartBuilder = CubeListBuilder.create().texOffs(1, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 16.0F, 0.0F, EnumSet.of(Direction.NORTH));
        modelPartData.addOrReplaceChild("back", modelPartBuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 1.0F, 0.0F, 0.0F, (float) Math.PI));
        modelPartData.addOrReplaceChild("left", modelPartBuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 1.0F, 0.0F, (float) (-Math.PI / 2), (float) Math.PI));
        modelPartData.addOrReplaceChild("right", modelPartBuilder, PartPose.offsetAndRotation(15.0F, 16.0F, 15.0F, 0.0F, (float) (Math.PI / 2), (float) Math.PI));
        modelPartData.addOrReplaceChild("front", modelPartBuilder, PartPose.offsetAndRotation(1.0F, 16.0F, 15.0F, (float) Math.PI, 0.0F, 0.0F));
        return LayerDefinition.create(modelData, 16, 16);
    }

    private static SpriteId getTextureIdFromSherd(Optional<Item> sherd) {
        if (sherd.isPresent()) {
            SpriteId normalSprite = Sheets.getDecoratedPotSprite(DecoratedPotPatterns.getPatternFromItem(sherd.get()));

            if (normalSprite != null) {
                var textureIdentifier = normalSprite.texture();

                var calciteSprite = new SpriteId(
                        normalSprite.atlasLocation(),
                        Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, textureIdentifier.getPath() + "_calcite")
                );
                return calciteSprite;
            }
            Identifier patternId = Identifier.fromNamespaceAndPath("sherdsapi", "sherd_pattern");
            @SuppressWarnings("unchecked")
            DataComponentType<Identifier> patternType =
                    (DataComponentType<Identifier>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(patternId).map(h -> (Object) h.value()).orElse(null);
            ItemStack stack = sherd.get().getDefaultInstance();
            Identifier pattern = patternType != null ? stack.get(patternType) : null;
            if (pattern != null) {
                return new SpriteId(DECORATED_POT_SHEET,
                        Identifier.fromNamespaceAndPath(pattern.getNamespace(), "entity/decorated_pot/" + pattern.getPath() + "_calcite"));
            }
        }

        return CALCITE_DECORATED_POT_SIDE;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(CalciteDecoratedPotBlockEntity pot, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pot, state, partialTicks, cameraPosition, breakProgress);
        state.direction = pot.getHorizontalFacing();
        state.decorations = pot.getSherds();
        state.wobbleType = pot.lastWobbleType;
        if (state.wobbleType != null && pot.getLevel() != null) {
            state.wobbleProgress = ((float) (pot.getLevel().getGameTime() - pot.lastWobbleTime) + partialTicks) / (float) state.wobbleType.lengthInTicks;
        } else {
            state.wobbleProgress = 0.0F;
        }
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.direction.toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);
        float g = state.wobbleProgress;
        if (state.wobbleType != null && g >= 0.0F && g <= 1.0F) {
            if (state.wobbleType == CalciteDecoratedPotBlockEntity.WobbleType.POSITIVE) {
                float k = g * (float) (Math.PI * 2);
                float l = -1.5F * (Mth.cos(k) + 0.5F) * Mth.sin(k / 2.0F);
                poseStack.rotateAround(Axis.XP.rotation(l * 0.015625F), 0.5F, 0.0F, 0.5F);
                float m = Mth.sin(k);
                poseStack.rotateAround(Axis.ZP.rotation(m * 0.015625F), 0.5F, 0.0F, 0.5F);
            } else {
                float h = Mth.sin(-g * 3.0F * (float) Math.PI) * 0.125F;
                float k = 1.0F - g;
                poseStack.rotateAround(Axis.YP.rotation(h * k), 0.5F, 0.0F, 0.5F);
            }
        }

        int light = state.lightCoords;
        RenderType baseType = CALCITE_DECORATED_POT_BASE.renderType(RenderTypes::entitySolid);
        TextureAtlasSprite baseSprite = this.sprites.get(CALCITE_DECORATED_POT_BASE);
        collector.submitModelPart(this.neck, poseStack, baseType, light, OverlayTexture.NO_OVERLAY, baseSprite, false, false, -1, null, 0);
        collector.submitModelPart(this.top, poseStack, baseType, light, OverlayTexture.NO_OVERLAY, baseSprite, false, false, -1, null, 0);
        collector.submitModelPart(this.bottom, poseStack, baseType, light, OverlayTexture.NO_OVERLAY, baseSprite, false, false, -1, null, 0);
        PotDecorations sherds = state.decorations;
        this.submitSide(this.front, poseStack, collector, light, getTextureIdFromSherd(sherds.front()));
        this.submitSide(this.back, poseStack, collector, light, getTextureIdFromSherd(sherds.back()));
        this.submitSide(this.left, poseStack, collector, light, getTextureIdFromSherd(sherds.left()));
        this.submitSide(this.right, poseStack, collector, light, getTextureIdFromSherd(sherds.right()));
        poseStack.popPose();
    }

    private void submitSide(ModelPart part, PoseStack poseStack, SubmitNodeCollector collector, int light, SpriteId spriteId) {
        collector.submitModelPart(part, poseStack, spriteId.renderType(RenderTypes::entitySolid), light, OverlayTexture.NO_OVERLAY, this.sprites.get(spriteId), false, false, -1, null, 0);
    }
}
