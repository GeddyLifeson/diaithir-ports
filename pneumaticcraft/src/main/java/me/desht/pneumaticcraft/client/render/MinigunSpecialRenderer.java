package me.desht.pneumaticcraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import me.desht.pneumaticcraft.client.model.ModelMinigun;
import me.desht.pneumaticcraft.client.model.PNCModelLayers;
import me.desht.pneumaticcraft.client.util.ClientUtils;
import me.desht.pneumaticcraft.common.item.minigun.MinigunItem;
import me.desht.pneumaticcraft.common.minigun.Minigun;
import me.desht.pneumaticcraft.common.registry.ModDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Animated minigun (spinning barrels, aiming) shown while the item is held by a player; otherwise the static item
 * model is used (see assets/pneumaticcraft/items/minigun.json).
 */
public class MinigunSpecialRenderer implements SpecialModelRenderer<Minigun> {
    private final ModelMinigun model;

    public MinigunSpecialRenderer(ModelMinigun model) {
        this.model = model;
    }

    @Override
    public void submit(@Nullable Minigun minigun, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay, boolean hasFoil, int outlineColor) {
        if (minigun == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.scale(1f, -1f, -1f);
        poseStack.translate(0, 0.9, -0.3);
        poseStack.scale(0.8f, 0.8f, 0.8f);
        LegacyBER.Recorder rec = new LegacyBER.Recorder(light);
        model.renderMinigun(poseStack, rec, light, overlay, minigun, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), false);
        rec.replay(collector);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        output.accept(new org.joml.Vector3f(0, 0, 0));
        output.accept(new org.joml.Vector3f(1, 1, 1));
    }

    @Override
    public @Nullable Minigun extractArgument(ItemStack stack) {
        if (stack.getItem() instanceof MinigunItem item && stack.has(ModDataComponents.OWNER_ENTITY_ID)) {
            int id = stack.getOrDefault(ModDataComponents.OWNER_ENTITY_ID, 0);
            if (ClientUtils.getClientLevel().getEntity(id) instanceof Player player) {
                return item.getMinigun(stack, player);
            }
        }
        return null;
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<Minigun> {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public @Nullable SpecialModelRenderer<Minigun> bake(BakingContext ctx) {
            return new MinigunSpecialRenderer(new ModelMinigun(ctx.entityModelSet().bakeLayer(PNCModelLayers.MINIGUN)));
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked<Minigun>> type() {
            return MAP_CODEC;
        }
    }
}
