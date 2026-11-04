package com.misterd.agritechevolved.client.ber;

import com.misterd.agritechevolved.AgritechEvolved;
import com.misterd.agritechevolved.block.custom.PlanterBlock;
import com.misterd.agritechevolved.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritechevolved.util.RegistryHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public class PlanterBlockEntityRenderer
        implements BlockEntityRenderer<PlanterBlockEntity, PlanterBlockEntityRenderer.RenderState> {

    public static final StandaloneModelKey<QuadCollection> CLOCHE_DOME_KEY = new StandaloneModelKey<>(
            () -> "agritechevolved: cloche_dome"
    );

    private static final Identifier WATER_STILL = Identifier.fromNamespaceAndPath("minecraft", "block/water_still");
    static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final ItemModelResolver itemModelResolver;
    private final BlockModelResolver blockModelResolver;

    public PlanterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.blockModelResolver = context.blockModelResolver();
    }

    public static class RenderState extends BlockEntityRenderState {
        public boolean cloched = false;
        public ItemStack soilStack = ItemStack.EMPTY;
        public ItemStack plantStack = ItemStack.EMPTY;
        public float growthProgress = 0f;
        public int growthStage = 0;
        public boolean soilIsWater = false;
        public boolean isTree = false;
        public double distanceSq = 0.0;
        final ItemStackRenderState soilRenderState = new ItemStackRenderState();
        final BlockModelRenderState plantModel = new BlockModelRenderState();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(PlanterBlockEntity be, RenderState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);

        state.cloched = be.getBlockState().getValue(PlanterBlock.CLOCHED);
        state.soilStack = be.getStack(1).copy();
        state.plantStack = be.getStack(0).copy();
        state.growthProgress = be.getGrowthProgress();
        state.growthStage = be.getGrowthStage();
        state.isTree = isTreePlant(state.plantStack);
        state.distanceSq = cameraPos.distanceToSqr(Vec3.atCenterOf(be.getBlockPos()));
        state.soilIsWater = !state.soilStack.isEmpty() && RegistryHelper.getItemId(state.soilStack).equals("minecraft:water_bucket");

        populateRenderState(state, state.soilStack, state.soilIsWater, state.plantStack,
                state.isTree, state.growthStage, be.getLevel(), itemModelResolver, blockModelResolver);
    }

    @Override
    public void submit(RenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        submitShared(state.cloched, state.distanceSq,
                state.soilStack, state.soilIsWater, state.soilRenderState,
                state.plantStack, state.isTree, state.plantModel,
                state.growthProgress, state.lightCoords,
                poseStack, collector);
    }

    static void populateRenderState(
            RenderState state,
            ItemStack soilStack, boolean soilIsWater, ItemStack plantStack,
            boolean isTree, int growthStage, @Nullable Level level,
            ItemModelResolver itemModelResolver, BlockModelResolver blockModelResolver) {

        if (!soilStack.isEmpty() && !soilIsWater) {
            itemModelResolver.updateForTopItem(state.soilRenderState, soilStack, ItemDisplayContext.FIXED, level, null, 0);
        } else {
            state.soilRenderState.clear();
        }

        state.plantModel.clear();
        if (!plantStack.isEmpty() && !soilStack.isEmpty() && plantStack.getItem() instanceof BlockItem) {
            if (isTree) {
                BlockState saplingState = ((BlockItem) plantStack.getItem()).getBlock().defaultBlockState();
                blockModelResolver.update(state.plantModel, saplingState, BLOCK_DISPLAY_CONTEXT);
            } else {
                BlockState cropState = getCropBlockState(plantStack, growthStage);
                if (cropState != null) {
                    blockModelResolver.update(state.plantModel, cropState, BLOCK_DISPLAY_CONTEXT);
                }
            }
        }
    }

    public static void submitShared(
            boolean cloched, double distanceSq,
            ItemStack soilStack, boolean soilIsWater, ItemStackRenderState soilRenderState,
            ItemStack plantStack, boolean isTree, BlockModelRenderState plantModel,
            float growthProgress, int light,
            PoseStack poseStack, SubmitNodeCollector collector) {

        if (cloched && distanceSq <= 256.0) {
            QuadCollection dome = Minecraft.getInstance().getModelManager().getStandaloneModel(CLOCHE_DOME_KEY);
            if (dome != null) {
                collector.submitCustomGeometry(poseStack,
                        RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS),
                        (pose, consumer) -> {
                            QuadInstance qi = new QuadInstance();
                            qi.setLightCoords(light);
                            qi.setOverlayCoords(OverlayTexture.NO_OVERLAY);
                            for (BakedQuad quad : dome.getAll()) {
                                consumer.putBakedQuad(pose, quad, qi);
                            }
                        });
            }
        }

        if (!soilStack.isEmpty()) {
            if (soilIsWater) {
                submitWater(poseStack, collector, light);
            } else {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.4, 0.5);
                poseStack.scale(1.3f, 0.65f, 1.3f);
                soilRenderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }

        if (!plantModel.isEmpty()) {
            poseStack.pushPose();
            if (isTree) {
                float scale = 0.3f + growthProgress * 0.4f;
                poseStack.translate(0.5, 0.58, 0.5);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0.0, -0.5);
            } else {
                poseStack.translate(0.1725, 0.58, 0.1725);
                poseStack.scale(0.65f, 0.65f, 0.65f);
            }
            plantModel.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    public static void onRegisterStandaloneModels(ModelEvent.RegisterStandalone event) {
        event.register(
                CLOCHE_DOME_KEY, SimpleUnbakedStandaloneModel.quadCollection(Identifier.fromNamespaceAndPath(AgritechEvolved.MODID, "block/cloche_dome"))
        );
    }

    static boolean isTreePlant(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem bi)) return false;
        BlockState def = bi.getBlock().defaultBlockState();
        return def.getProperties().stream().noneMatch(p -> p.getName().equals("age"));
    }

    private static void submitWater(PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(WATER_STILL);

        float y = 0.57f;
        float xMin = 0.175f, xMax = 0.825f;
        float zMin = 0.175f, zMax = 0.825f;
        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, consumer) -> {
            addWaterVertex(consumer, pose, xMin, y, zMin, u0, v0, light);
            addWaterVertex(consumer, pose, xMin, y, zMax, u0, v1, light);
            addWaterVertex(consumer, pose, xMax, y, zMax, u1, v1, light);
            addWaterVertex(consumer, pose, xMax, y, zMin, u1, v0, light);
        });
    }

    private static void addWaterVertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
        c.addVertex(pose, x, y, z)
                .setColor(0x3F, 0x76, 0xE4, 0xA0)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0f, 1f, 0f);
    }

    static @Nullable BlockState getCropBlockState(ItemStack stack, int age) {
        if (!(stack.getItem() instanceof BlockItem bi)) return null;
        BlockState def = bi.getBlock().defaultBlockState();
        for (Property<?> prop : def.getProperties()) {
            if (prop instanceof IntegerProperty ip && prop.getName().equals("age")) {
                int max = ip.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(7);
                return def.setValue(ip, Math.min(age, max));
            }
        }
        if (def.hasProperty(BlockStateProperties.AGE_7)) return def.setValue(BlockStateProperties.AGE_7, Math.min(age, 7));
        if (def.hasProperty(BlockStateProperties.AGE_3)) return def.setValue(BlockStateProperties.AGE_3, Math.min(age, 3));
        if (def.hasProperty(BlockStateProperties.AGE_5)) return def.setValue(BlockStateProperties.AGE_5, Math.min(age, 5));
        if (def.hasProperty(BlockStateProperties.AGE_15)) return def.setValue(BlockStateProperties.AGE_15, Math.min(age, 15));
        if (def.hasProperty(BlockStateProperties.AGE_25)) return def.setValue(BlockStateProperties.AGE_25, Math.min(age, 25));
        return def;
    }
}