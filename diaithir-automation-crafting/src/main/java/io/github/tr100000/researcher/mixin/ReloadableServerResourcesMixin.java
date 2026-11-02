package io.github.tr100000.researcher.mixin;

import io.github.tr100000.researcher.ResearchManager;
import io.github.tr100000.researcher.api.ResearchManagerGetter;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin implements ResearchManagerGetter {
    @Unique
    private ResearchManager researchManager;

    @Inject(method = "<init>", at = @At(("TAIL")))
    private void init(
            LayeredRegistryAccess<RegistryLayer> fullLayers, HolderLookup.Provider loadingContext, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, List<Registry.PendingTags<?>> postponedTags, PermissionSet functionCompilationPermissions, List<DataComponentInitializers.PendingComponents<?>> newComponents, CallbackInfo ci
    ) {
        researchManager = new ResearchManager(loadingContext, (ReloadableServerResources)(Object)this);
    }

    @Inject(method = "listeners", at = @At("RETURN"), cancellable = true)
    private void modifyListeners(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        List<PreparableReloadListener> contents = new ObjectArrayList<>(cir.getReturnValue());
        contents.add(researchManager);
        cir.setReturnValue(contents);
    }

    @Override @Unique
    public ResearchManager researcher$getServerManager() {
        return researchManager;
    }
}
