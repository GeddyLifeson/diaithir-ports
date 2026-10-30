package rpggods.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector3f;
import rpggods.RPGGods;
import rpggods.client.screen.AltarScreen;
import rpggods.entity.AltarEntity;
import rpggods.util.altar.AltarPose;
import rpggods.util.altar.HumanoidPart;

import java.util.HashMap;
import java.util.Map;

public class AltarRenderer extends LivingEntityRenderer<AltarEntity, AltarRenderState, AltarModel> {

    protected static final Identifier STATUE_TEXTURE = Identifier.fromNamespaceAndPath(RPGGods.MODID, "textures/altar/statue.png");
    protected static final Identifier STATUE_SLIM_TEXTURE = Identifier.fromNamespaceAndPath(RPGGods.MODID, "textures/altar/statue_slim.png");

    public static final ModelLayerLocation ALTAR_MODEL_RESOURCE = new ModelLayerLocation(Identifier.fromNamespaceAndPath(RPGGods.MODID, "altar"), "altar");
    public static final ArmorModelSet<ModelLayerLocation> ALTAR_INNER_ARMOR_RESOURCE = armorSet("inner_armor");
    public static final ArmorModelSet<ModelLayerLocation> ALTAR_OUTER_ARMOR_RESOURCE = armorSet("outer_armor");

    private static ArmorModelSet<ModelLayerLocation> armorSet(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(RPGGods.MODID, "altar");
        return new ArmorModelSet<>(new ModelLayerLocation(id, name + "_head"), new ModelLayerLocation(id, name + "_chest"),
                new ModelLayerLocation(id, name + "_legs"), new ModelLayerLocation(id, name + "_feet"));
    }

    private final Map<Identifier, Identifier> deityTextures = new HashMap<>();

    public AltarRenderer(final EntityRendererProvider.Context context) {
        super(context, new AltarModel(context.bakeLayer(ALTAR_MODEL_RESOURCE)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ALTAR_INNER_ARMOR_RESOURCE, context.getModelSet(), AltarArmorModel::new),
                ArmorModelSet.bake(ALTAR_OUTER_ARMOR_RESOURCE, context.getModelSet(), AltarArmorModel::new),
                context.getEquipmentRenderer()));
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new WingsLayer<>(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
    }

    @Override
    public AltarRenderState createRenderState() {
        return new AltarRenderState();
    }

    @Override
    public void extractRenderState(AltarEntity entity, AltarRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        net.minecraft.client.renderer.entity.HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        state.pose = entity.getAltarPose();
        state.female = entity.isFemale();
        state.slim = entity.isSlim();
        final ItemStack blockStack = entity.getBlockBySlot();
        state.hasBlock = !blockStack.isEmpty();
        if (state.hasBlock) {
            this.itemModelResolver.updateForNonLiving(state.blockItem, blockStack, ItemDisplayContext.NONE, entity);
        } else {
            state.blockItem.clear();
        }
        state.texture = findTexture(entity);
    }

    @Override
    protected void setupRotations(AltarRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        // use the same orientation as the original altar model
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        // render the base block, if any
        final float baseHeight = state.hasBlock ? 0.0F : -0.5F;
        if (state.hasBlock) {
            poseStack.pushPose();
            poseStack.translate(-0.5F, 0.0F, -0.5F);
            blockCollector.submit(state, poseStack);
            poseStack.popPose();
        }
        // rotate around body and translate according to pose offsets
        final AltarPose pose = state.pose;
        final Vector3f offset = pose.get(HumanoidPart.OFFSET);
        final Vector3f rotation = pose.get(HumanoidPart.BODY);
        final double scale = 1.0D / Math.PI;
        poseStack.translate(offset.x() * scale, offset.y() * scale, offset.z() * scale);
        if (rotation.z() != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotation(rotation.z()));
        }
        if (rotation.y() != 0.0F) {
            poseStack.mulPose(Axis.YP.rotation(rotation.y()));
        }
        if (rotation.x() != 0.0F) {
            poseStack.mulPose(Axis.XP.rotation(rotation.x()));
        }
        poseStack.translate(0.0F, 0.5F + baseHeight, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    }

    private interface BlockCollector {
        void submit(AltarRenderState state, PoseStack poseStack);
    }

    /** the node collector of the current frame, set in {@link #submit} **/
    private BlockCollector blockCollector = (s, p) -> {};

    @Override
    public void submit(AltarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.blockCollector = (s, p) -> s.blockItem.submit(p, collector, s.lightCoords, OverlayTexture.NO_OVERLAY, s.outlineColor);
        super.submit(state, poseStack, collector, camera);
        this.blockCollector = (s, p) -> {};
    }

    @Override
    protected boolean shouldShowName(AltarEntity entity, double distanceToCameraSq) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AltarScreen) {
            return false;
        }
        return super.shouldShowName(entity, distanceToCameraSq) && mc.crosshairPickEntity == entity;
    }

    @Override
    public Identifier getTextureLocation(final AltarRenderState state) {
        return state.texture;
    }

    private Identifier findTexture(final AltarEntity entity) {
        final Identifier fallback = entity.isSlim() ? STATUE_SLIM_TEXTURE : STATUE_TEXTURE;
        // return deity texture, if it exists
        if (entity.getDeity().isPresent() && !entity.getDeity().get().toString().isEmpty()) {
            return deityTextures.computeIfAbsent(entity.getDeity().get(), deity -> {
                final Identifier id = Identifier.fromNamespaceAndPath(deity.getNamespace(), "textures/altar/" + deity.getPath() + ".png");
                return Minecraft.getInstance().getResourceManager().getResource(id).isPresent() ? id : fallback;
            });
        }
        // return player texture
        if (entity.getPlayerProfile() != null) {
            return Minecraft.getInstance().playerSkinRenderCache().getOrDefault(entity.getPlayerProfile()).playerSkin().body().texturePath();
        }
        return fallback;
    }
}
