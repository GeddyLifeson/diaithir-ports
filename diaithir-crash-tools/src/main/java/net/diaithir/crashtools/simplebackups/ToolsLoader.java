package net.diaithir.crashtools.simplebackups;

import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ToolsLoader {

    private static final Path TOOLS_DIR = FMLPaths.CONFIGDIR.get().resolve(SimpleBackups.MODID).resolve("external-dependencies");
    public static final Path RELATIVE_TOOLS_DIR = FMLPaths.GAMEDIR.get().relativize(TOOLS_DIR);
    private static final Logger LOGGER = LoggerFactory.getLogger(ToolsLoader.class);
    // Defaults to the mod's own classloader so libraries bundled via JarJar (the -zstd build
    // variant) are still found when external-dependencies provides nothing. Replaced with a
    // URLClassLoader (parented to this same classloader) only when external jars are present.
    private static ClassLoader classLoader = ToolsLoader.class.getClassLoader();

    public static void init() {
        if (!Files.isDirectory(ToolsLoader.TOOLS_DIR)) {
            try {
                Files.createDirectories(ToolsLoader.TOOLS_DIR);
            } catch (IOException e) {
                LOGGER.error("Failed to create external-dependencies directory", e);
            }

            return;
        }

        List<URL> urls = new ArrayList<>();
        try (Stream<Path> stream = Files.list(ToolsLoader.TOOLS_DIR)) {
            stream.filter(p -> p.toString().endsWith(".jar")).forEach(jar -> {
                try {
                    urls.add(jar.toUri().toURL());
                    LOGGER.info("Loaded tools jar: {}", jar.getFileName());
                } catch (Exception e) {
                    LOGGER.warn("Failed to load tools jar: {}", jar.getFileName(), e);
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Failed to scan external-dependencies directory", e);
            return;
        }

        if (!urls.isEmpty()) {
            classLoader = new URLClassLoader(urls.toArray(URL[]::new), ToolsLoader.class.getClassLoader());
        }
    }

    public static boolean isLzmaAvailable() {
        return ToolsLoader.isClassAvailable("org.tukaani.xz.XZ");
    }

    public static boolean isZstdAvailable() {
        return ToolsLoader.isClassAvailable("com.github.luben.zstd.Zstd");
    }

    public static boolean isClassAvailable(String className) {
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Wraps the given input stream in a {@code ZstdInputStream} loaded from the
     * tools classloader.
     *
     * @param in source stream of zstd-compressed data
     * @return decompressing wrapper stream
     * @throws IOException if zstd-jni is not available or instantiation fails
     */
    public static InputStream wrapZstdInput(InputStream in) throws IOException {
        try {
            Class<?> clazz = classLoader.loadClass("com.github.luben.zstd.ZstdInputStream");
            return (InputStream) clazz.getConstructor(InputStream.class).newInstance(in);
        } catch (ReflectiveOperationException e) {
            throw new IOException("Failed to create ZstdInputStream", e);
        }
    }

    /**
     * Wraps the given stream in a ZstdOutputStream loaded from the tools classloader.
     * The returned stream is safe to cast to {@link OutputStream} since ZstdOutputStream extends it.
     *
     * @param level ZSTD compression level (1–19), or -1 for the ZSTD default (3)
     */
    public static OutputStream wrapWithZstd(OutputStream out, int level) throws IOException {
        try {
            Class<?> clazz = classLoader.loadClass("com.github.luben.zstd.ZstdOutputStream");
            if (level < 0) {
                return (OutputStream) clazz.getConstructor(OutputStream.class).newInstance(out);
            }
            return (OutputStream) clazz.getConstructor(OutputStream.class, int.class).newInstance(out, level);
        } catch (ReflectiveOperationException e) {
            throw new IOException("Failed to create ZstdOutputStream", e);
        }
    }

    /**
     * Wraps the given stream in an {@code XZOutputStream} (LZMA2) loaded from the tools
     * classloader. The xz-java classes are referenced reflectively so the mod's own
     * classloader never needs them on its classpath (mirrors {@link #wrapWithZstd}).
     *
     * @param preset LZMA2 preset 0–9; clamped into range
     */
    public static OutputStream wrapWithXz(OutputStream out, int preset) throws IOException {
        try {
            Class<?> optsClass = classLoader.loadClass("org.tukaani.xz.LZMA2Options");
            Object opts = optsClass.getConstructor().newInstance();
            try {
                optsClass.getMethod("setPreset", int.class).invoke(opts, Math.clamp(preset, 0, 9));
            } catch (InvocationTargetException ignored) {
                // unsupported preset → keep the LZMA2Options default
            }
            Class<?> filterOptions = classLoader.loadClass("org.tukaani.xz.FilterOptions");
            Class<?> xzOut = classLoader.loadClass("org.tukaani.xz.XZOutputStream");
            return (OutputStream) xzOut.getConstructor(OutputStream.class, filterOptions).newInstance(out, opts);
        } catch (ReflectiveOperationException e) {
            throw new IOException("Failed to create XZOutputStream", e);
        }
    }

    /**
     * Wraps the given input stream in an {@code XZInputStream} loaded from the tools classloader.
     *
     * @param in source stream of xz-compressed data
     * @return decompressing wrapper stream
     */
    public static InputStream wrapXzInput(InputStream in) throws IOException {
        try {
            Class<?> xzIn = classLoader.loadClass("org.tukaani.xz.XZInputStream");
            return (InputStream) xzIn.getConstructor(InputStream.class).newInstance(in);
        } catch (ReflectiveOperationException e) {
            throw new IOException("Failed to create XZInputStream", e);
        }
    }
}
