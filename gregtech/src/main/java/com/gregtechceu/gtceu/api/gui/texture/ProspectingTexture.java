package com.gregtechceu.gtceu.api.gui.texture;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.misc.PacketProspecting;
import com.gregtechceu.gtceu.api.gui.misc.ProspectorMode;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.utils.ColorUtils;

import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import lombok.Getter;
import org.joml.Matrix4f;

import java.io.IOException;
import java.lang.reflect.Array;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

@SuppressWarnings({ "rawtypes", "unchecked" })
@OnlyIn(Dist.CLIENT)
public class ProspectingTexture extends AbstractTexture {

    public static final String SELECTED_ALL = "[all]";
    private static final ResourceTexture ARROW = GuiTextures.UP.copy().setColor(ColorPattern.RED.color);

    @Getter
    private String selected = SELECTED_ALL;
    private boolean darkMode;
    @Getter
    private final int imageWidth;
    @Getter
    private final int imageHeight;
    public final Object[][][] data;
    private final int playerXGui;
    private final int playerYGui;
    private final float direction;
    private final int playerChunkX;
    private final int playerChunkZ;
    private final ProspectorMode mode;
    private final int radius;

    public ProspectingTexture(int playerChunkX, int playerChunkZ, int posX, int posZ, float direction,
                              ProspectorMode mode, int radius, boolean darkMode) {
        this.darkMode = darkMode;
        this.radius = radius;
        this.mode = mode;
        this.data = (Object[][][]) Array.newInstance(mode.getItemClass(), (radius * 2 - 1) * mode.cellSize,
                (radius * 2 - 1) * mode.cellSize, 0);
        this.imageWidth = (radius * 2 - 1) * 16;
        this.imageHeight = (radius * 2 - 1) * 16;
        this.playerChunkX = playerChunkX;
        this.playerChunkZ = playerChunkZ;
        this.direction = (direction + 180) % 360;
        this.playerXGui = posX - (playerChunkX - this.radius + 1) * 16 + (posX > 0 ? 1 : 0);
        playerYGui = posZ - (playerChunkZ - this.radius + 1) * 16 + (posX > 0 ? 1 : 0);
    }

    public void updateTexture(PacketProspecting packet) {
        int ox;
        if ((packet.chunkX > 0 && playerChunkX > 0) || (packet.chunkX < 0 && playerChunkX < 0)) {
            ox = Math.abs(Math.abs(packet.chunkX) - Math.abs(playerChunkX));
        } else {
            ox = Math.abs(playerChunkX) + Math.abs(packet.chunkX);
        }
        if (playerChunkX > packet.chunkX) {
            ox = -ox;
        }

        int oy;
        if ((packet.chunkZ > 0 && playerChunkZ > 0) || (packet.chunkZ < 0 && playerChunkZ < 0)) {
            oy = Math.abs(Math.abs(packet.chunkZ) - Math.abs(playerChunkZ));
        } else {
            oy = Math.abs(playerChunkZ) + Math.abs(packet.chunkZ);
        }
        if (playerChunkZ > packet.chunkZ) {
            oy = -oy;
        }

        int currentColumn = (this.radius - 1) + ox;
        int currentRow = (this.radius - 1) + oy;
        if (currentRow < 0) {
            return;
        }

        for (int x = 0; x < mode.cellSize; x++) {
            System.arraycopy(packet.data[x], 0, data[x + currentColumn * mode.cellSize], currentRow * mode.cellSize,
                    mode.cellSize);
        }
        load();
    }

    private NativeImage getImage() { return null; /* ported stub */ }

    /**
     * The resulting color of this operation is stored as least to most significant bits.
     */
    public static int combine(int alpha, int blue, int green, int red) {
        return (alpha & 0xFF) << 24 | (blue & 0xFF) << 16 | (green & 0xFF) << 8 | (red & 0xFF) << 0;
    }

    public void load() {
        doLoad(getImage());
    }

    private void doLoad(NativeImage image) {  /* ported stub */ }

    public void draw(GuiGraphics graphics, int x, int y) {  /* ported stub */ }

    
    @Override
    public void load(ResourceManager resourceManager) throws IOException {}

    public void setDarkMode(boolean darkMode) {
        if (this.darkMode != darkMode) {
            this.darkMode = darkMode;
            load();
        }
    }

    public void setSelected(String uniqueID) {
        if (!this.selected.equals(uniqueID)) {
            this.selected = uniqueID;
            load();
        }
    }
}
