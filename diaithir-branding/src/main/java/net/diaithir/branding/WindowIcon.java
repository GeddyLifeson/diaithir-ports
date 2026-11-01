package net.diaithir.branding;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryUtil;

/** The hearth on the game window and taskbar. Lives outside the mixin package so its resources resolve from this mod's jar. */
public final class WindowIcon {
    private static final int[] SIZES = { 16, 32, 48, 128, 256 };

    private WindowIcon() {}

    /** False when the icon could not be set here (macOS takes its dock icon another way), so vanilla's stays. */
    public static boolean apply(long window) {
        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_COCOA) return false;
        List<ByteBuffer> pixels = new ArrayList<>();
        try (GLFWImage.Buffer icons = GLFWImage.malloc(SIZES.length)) {
            for (int i = 0; i < SIZES.length; i++) {
                try (InputStream in = WindowIcon.class.getResourceAsStream("/assets/diaithirbranding/icons/icon_" + SIZES[i] + ".png")) {
                    if (in == null) return false;
                    try (NativeImage image = NativeImage.read(in)) {
                        ByteBuffer buffer = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * 4);
                        pixels.add(buffer);
                        buffer.asIntBuffer().put(image.getPixelsABGR());
                        icons.position(i).width(image.getWidth()).height(image.getHeight()).pixels(buffer);
                    }
                }
            }
            GLFW.glfwSetWindowIcon(window, icons.position(0));
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            pixels.forEach(MemoryUtil::memFree);
        }
    }
}
