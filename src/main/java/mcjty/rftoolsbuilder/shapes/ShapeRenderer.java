package mcjty.rftoolsbuilder.shapes;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import mcjty.lib.varia.Check32;
import mcjty.lib.varia.SafeClientTools;
import mcjty.rftoolsbuilder.modules.builder.items.ShapeCardItem;
import mcjty.rftoolsbuilder.modules.scanner.network.PacketRequestShapeData;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Keeps a shape's preview data in step with the server (block count, preview message). The 3D previews in the shape
 * GUIs and the projector holograms drew with immediate GL and cached vertex buffers, which 26.1 removed; they are not
 * drawn in this port.
 */
public class ShapeRenderer {

    private int prevX = -1;
    private int prevY = -1;

    private float scale = 3.0f;
    private float dx = 230.0f;
    private float dy = 100.0f;
    private float xangle = 25.0f;
    private float yangle = 25.0f;

    private ShapeID shapeID;

    private int waitForNewRequest = 0;

    public ShapeRenderer(ShapeID shapeID) {
        this.shapeID = shapeID;
    }

    public void setShapeID(ShapeID shapeID) {
        this.shapeID = shapeID;
    }

    public ShapeID getShapeID() {
        return shapeID;
    }

    public int getCount() {
        RenderData data = ShapeDataManagerClient.getRenderData(shapeID);
        if (data != null) {
            return data.getBlockCount();
        }
        return 0;
    }

    public static RenderData getRenderDataAndCreate(ShapeID shapeID) {
        RenderData data = ShapeDataManagerClient.getRenderDataAndCreate(shapeID);
        data.touch();
        return data;
    }

    public static void setRenderData(ShapeID id, int checksum, @Nullable RenderData.RenderPlane plane, int offsetY, int dy, String msg) {
        RenderData data = getRenderDataAndCreate(id);
        if (data.getChecksum() != checksum) {
            return;
        }
        data.setPlaneData(plane, offsetY, dy);
        data.previewMessage = msg;
        data.markRequestProgress();
        if (offsetY >= dy - 1) {
            data.clearRequest();
        }
    }

    public void initView(int dx, int dy) {
        Minecraft mc = Minecraft.getInstance();

        Window mainWindow = mc.getWindow();
        int xScale = mainWindow.getGuiScaledWidth();
        int yScale = mainWindow.getGuiScaledHeight();
        int sx = (dx + 84) * mainWindow.getScreenWidth() / xScale;
        int sy = (mainWindow.getScreenHeight()) - (dy + 136) * mainWindow.getScreenHeight() / yScale;
        int sw = 161 * mainWindow.getScreenWidth() / xScale;
        int sh = 130 * mainWindow.getScreenHeight() / yScale;
        int vx = sx + sw/2;
        int vy = sy + sh/2;

        this.dx = (float) (vx/mainWindow.getGuiScale());
        this.dy = (float) (vy/mainWindow.getGuiScale());
    }

    public void handleShapeDragging(int x, int y, boolean[] buttons) {
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        if (x >= 100 && y <= 120) {
            if (SafeClientTools.isSneaking()) {
                if (prevX != -1 && buttons[0]) {
                    dx += (x - prevX);
                    dy += (y - prevY);
                }
            } else {
                if (prevX != -1 && buttons[0]) {
                    yangle -= (x - prevX);
                    xangle += (y - prevY);
                }
            }
            prevX = x;
            prevY = y;
        }

        if (buttons[2]) {
            xangle = 0.0f;
            yangle = 0.0f;
        }
    }

    public void handleMouseWheel(double dwheelX, double dwheelY) {
        float dwheel = (float) dwheelY;
        if (dwheel < 0) {
            scale *= .6;
            if (scale <= 0.1) {
                scale = .1f;
            }
        } else if (dwheel > 0) {
            scale *= 1.4;
        }
    }

    private int calculateChecksum(ItemStack stack) {
        Check32 crc = new Check32();
        if (!stack.isEmpty()) {
            ShapeCardItem.getFormulaCheckClient(stack, crc);
        }
        return crc.get();
    }

    private RenderData requestRenderData(ItemStack stack) {
        RenderData data = getRenderDataAndCreate(shapeID);
        int check = calculateChecksum(stack);
        if (check != data.getChecksum()) {
            data.clearRequest();
            data.clearData();
            data.setChecksum(check);
            data.setWantData(true);
        } else if (data.isRequestTimedOut()) {
            data.clearRequest();
            data.setWantData(true);
        }

        if ((data.isWantData() || !data.hasData()) && !data.isRequestInFlight()) {
            if (waitForNewRequest <= 0) {
                RFToolsBuilderMessages.sendToServer(PacketRequestShapeData.create(stack, shapeID, check));
                waitForNewRequest = 20;
                data.setWantData(false);
                data.markRequestSent();
            } else {
                waitForNewRequest--;
            }
        }
        return data;
    }

    public boolean renderShapeInWorld(PoseStack poseStack, ItemStack stack, float offset, float scale, float angle,
                                      boolean scan, ShapeID shape, boolean renderBlockModels) {
        requestRenderData(stack);
        return false;
    }

    public void renderShape(GuiGraphicsExtractor graphics, IShapeParentGui gui, ItemStack stack, int x, int y, boolean showAxis, boolean showOuter, boolean showScan, boolean showGuidelines) {
        RenderData data = requestRenderData(stack);
        if (!data.previewMessage.isEmpty()) {
            graphics.text(Minecraft.getInstance().font, data.previewMessage, x + 10, y + 10, 0xffffffff, false);
        }
    }
}
