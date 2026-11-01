package com.lowdragmc.lowdraglib.gui.compass.component;

import com.lowdragmc.lowdraglib.client.utils.RenderUtils;
import com.lowdragmc.lowdraglib.gui.compass.ILayoutComponent;
import com.lowdragmc.lowdraglib.gui.compass.LayoutPageWidget;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import com.lowdragmc.lowdraglib.utils.XmlUtils;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.joml.Vector3f;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author KilaBash
 * @date 2022/10/22
 * @implNote SceneComponent
 */
public class SceneComponent extends AbstractComponent {

    protected final List<Map<BlockPos, BlockInfo>> pages = new ArrayList<>();
    protected final List<Map<BlockPos, String>> hoverInfos = new ArrayList<>();
    private boolean draggable = false;
    private boolean scalable = false;
    private boolean ortho = false;
    private float zoom = -1;
    private float yaw = 25;
    private int height = 150;

    @Override
    public ILayoutComponent fromXml(Element element) {
        super.fromXml(element);
        draggable = XmlUtils.getAsBoolean(element, "draggable", draggable);
        scalable = XmlUtils.getAsBoolean(element, "scalable", scalable);
        ortho = !XmlUtils.getAsString(element, "camera", "ortho").equals("perspective");
        zoom = XmlUtils.getAsFloat(element, "zoom", zoom);
        yaw = XmlUtils.getAsFloat(element, "yaw", yaw);
        height = XmlUtils.getAsInt(element, "height", height);
        NodeList nodeList = element.getChildNodes();
        BlockPos offset = BlockPos.ZERO;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element page && page.getNodeName().equals("page")) {
                addPage(page, offset);
                offset = offset.offset(500, 0, 500);
            }
        }
        return this;
    }

    private void addPage(Element element, BlockPos offset) {
        Map<BlockPos, BlockInfo> blocks = new HashMap<>();
        Map<BlockPos, String> contents = new HashMap<>();
        Object2BooleanMap<BlockPos> items = new Object2BooleanOpenHashMap<>();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element block && block.getNodeName().equals("block")) {
                BlockPos pos = XmlUtils.getAsBlockPos(block, "pos", BlockPos.ZERO).offset(offset);
                BlockInfo blockInfo = XmlUtils.getBlockInfo(block);
                boolean itemTips = XmlUtils.getAsBoolean(block, "item-tips", false);
                String content = XmlUtils.getContent(block, true);
                blocks.put(pos, blockInfo);
                contents.put(pos, content);
                items.put(pos, itemTips);
            }

        }
        pages.add(blocks);
        hoverInfos.add(contents);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected LayoutPageWidget addWidgets(LayoutPageWidget currentPage) { return null; /* ported stub */ }

    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int minZ = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    int maxZ = Integer.MIN_VALUE;

    private void setPage(Map<BlockPos, BlockInfo> blocks, SceneWidget sceneWidget) {
        minX = Integer.MAX_VALUE;
        minY = Integer.MAX_VALUE;
        minZ = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        maxY = Integer.MIN_VALUE;
        maxZ = Integer.MIN_VALUE;
        for (BlockPos vPos : blocks.keySet()) {
            minX = Math.min(minX, vPos.getX());
            minY = Math.min(minY, vPos.getY());
            minZ = Math.min(minZ, vPos.getZ());
            maxX = Math.max(maxX, vPos.getX());
            maxY = Math.max(maxY, vPos.getY());
            maxY = Math.max(maxY, vPos.getY());
            maxZ = Math.max(maxZ, vPos.getZ());
        }
        var center = new Vector3f((minX + maxX) / 2f + 0.5F, (minY + maxY) / 2f + 0.5F, (minZ + maxZ) / 2f + 0.5F);

        if (zoom > 0) {
            sceneWidget.setZoom(zoom);
        } else {
            sceneWidget.setZoom((float) (15 * Math.sqrt(Math.max(Math.max(Math.max(maxX - minX + 1, maxY - minY + 1), maxZ - minZ + 1), 1))));
        }

        sceneWidget.setCenter(center);
        sceneWidget.setCameraYawAndPitch(yaw, sceneWidget.getRotationPitch());
    }

}
