package net.diaithir.perf.mixin;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Mixins under mixin.compat.MODID apply only when that mod is installed. */
public final class MixinPlugin implements IMixinConfigPlugin {
    private static final String COMPAT = "net.diaithir.perf.mixin.compat.";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(COMPAT)) return true;
        String modId = mixinClassName.substring(COMPAT.length(), mixinClassName.indexOf('.', COMPAT.length()));
        return FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
