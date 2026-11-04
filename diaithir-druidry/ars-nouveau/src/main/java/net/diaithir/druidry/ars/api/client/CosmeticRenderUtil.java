package net.diaithir.druidry.ars.api.client;

import net.diaithir.druidry.ars.api.entity.IDecoratable;
import net.diaithir.druidry.ars.api.item.ICosmeticItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;


public class CosmeticRenderUtil {

    /**
     * The GeckoLib bone based custom item rendering has been removed in the port
     * (GeoBone / RenderUtil / ItemRenderer.renderStatic no longer exist).
     * The bone parameter is kept as a generic object so callers keep compiling.
     */
    public static <T extends LivingEntity & IDecoratable> void renderCosmetic(Object bone, PoseStack matrix, MultiBufferSource buffer, T entity, int packedLightIn) {
        ItemStack stack = entity.getCosmeticItem();
        if (!(stack.getItem() instanceof ICosmeticItem cosmetic)) return;
        //checks should have already been made, but pattern variables ftw
        matrix.pushPose();

        Vec3 translations = cosmetic.getTranslations(entity);
        Vec3 scaling = cosmetic.getScaling(entity);
        matrix.translate(translations.x, translations.y, translations.z);
        matrix.scale((float) scaling.x, (float) scaling.y, (float) scaling.z);
        // Custom item rendering removed (no equivalent immediate-mode ItemRenderer in 26.1).

        matrix.popPose();
    }

}
