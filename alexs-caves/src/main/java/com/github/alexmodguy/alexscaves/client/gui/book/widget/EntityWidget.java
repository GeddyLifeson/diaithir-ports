package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class EntityWidget extends BookWidget {

    @Expose
    @SerializedName("entity_id")
    private String entityId;
    @Expose
    private String nbt;
    @Expose
    private boolean sepia;
    @Expose
    @SerializedName("rot_x")
    private float rotX;
    @Expose
    @SerializedName("rot_y")
    private float rotY;
    @Expose
    @SerializedName("rot_z")
    private float rotZ;

    private transient Entity actualRenderEntity = null;
    private transient boolean failed = false;

    public EntityWidget(int displayPage, String entityId, boolean sepia, String entityNBT, int x, int y, float scale) {
        this(displayPage, Type.ENTITY, entityId, sepia, entityNBT, x, y, scale);
    }

    public EntityWidget(int displayPage, Type type, String entityId, boolean sepia, String entityNBT, int x, int y, float scale) {
        super(displayPage, type, x, y, scale);
        this.entityId = entityId;
        this.sepia = sepia;
        this.nbt = entityNBT;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, PageContext ctx, float partialTicks) {
        renderEntityAt(graphics, ctx, getX(), getY(), 0, 0);
    }

    protected void renderEntityAt(GuiGraphicsExtractor graphics, PageContext ctx, float pageX, float pageY, float offX, float offY) {
        if (failed) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (actualRenderEntity == null) {
            try {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(entityId));
                actualRenderEntity = type.create(mc.level, EntitySpawnReason.LOAD);
            } catch (Throwable t) {
                t.printStackTrace();
            }
            if (actualRenderEntity == null) {
                failed = true;
                return;
            }
        }
        try {
            // upstream: 100 * scale model units per block, shrunk for entities bigger than a block
            float blockPx = 100.0F * getScale();
            float bb = Math.max(actualRenderEntity.getBbWidth(), actualRenderEntity.getBbHeight());
            if (bb > 1.0F) {
                blockPx /= bb * 1.5F;
            }
            float px = blockPx * ctx.pageScale();
            float cx = ctx.originX() + (pageX + offX) * ctx.pageScale();
            float cy = ctx.originY() + (pageY + offY) * ctx.pageScale();
            EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
            var renderer = dispatcher.getRenderer(actualRenderEntity);
            @SuppressWarnings({"unchecked", "rawtypes"})
            EntityRenderState state = ((net.minecraft.client.renderer.entity.EntityRenderer) renderer).createRenderState(actualRenderEntity, 1.0F);
            state.shadowPieces.clear();
            state.outlineColor = 0;
            if (state instanceof LivingEntityRenderState living) {
                living.bodyRot = 180.0F;
                living.yRot = 0.0F;
                living.xRot = 0.0F;
                living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
                living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
                living.scale = 1.0F;
            }
            Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI)
                    .rotateX((float) Math.toRadians(rotX))
                    .rotateY((float) Math.toRadians(rotY))
                    .rotateZ((float) Math.toRadians(rotZ));
            Vector3f translation = new Vector3f(0.0F, state.boundingBoxHeight / 2.0F, 0.0F);
            float half = px * Math.max(1.0F, bb) * 1.1F + 8;
            graphics.entity(state, px, translation, rotation, null, Math.round(cx - half), Math.round(cy - half), Math.round(cx + half), Math.round(cy + half));
        } catch (Throwable t) {
            t.printStackTrace();
            failed = true;
        }
    }

    protected boolean isSepia() {
        return sepia;
    }
}
