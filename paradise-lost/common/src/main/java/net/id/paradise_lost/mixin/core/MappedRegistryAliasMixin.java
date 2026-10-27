package net.id.paradise_lost.mixin.core;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.id.paradise_lost.util.ParadiseLostAliasFix;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryAliasMixin {

    @SuppressWarnings("rawtypes")
    private MappedRegistry paradiseLost$registry() {
        return (MappedRegistry) (Object) this;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ModifyReturnValue(method = "getValue(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;", at = @At("RETURN"))
    private Object paradiseLost$aliasGetValueByLocation(Object original, Identifier id) {
        if (original != null || id == null) return original;
        Identifier mapped = ParadiseLostAliasFix.resolve(id);
        return mapped == null ? original : paradiseLost$registry().getValue(mapped);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ModifyReturnValue(method = "get(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;", at = @At("RETURN"))
    private Optional<?> paradiseLost$aliasGetByLocation(Optional<?> original, Identifier id) {
        if (original == null || !original.isEmpty() || id == null) return original;
        Identifier mapped = ParadiseLostAliasFix.resolve(id);
        return mapped == null ? original : paradiseLost$registry().get(mapped);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ModifyReturnValue(method = "containsKey(Lnet/minecraft/resources/Identifier;)Z", at = @At("RETURN"))
    private boolean paradiseLost$aliasContainsByLocation(boolean original, Identifier id) {
        if (original || id == null) return original;
        Identifier mapped = ParadiseLostAliasFix.resolve(id);
        return mapped != null && paradiseLost$registry().containsKey(mapped);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ModifyReturnValue(method = "getValue(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;", at = @At("RETURN"))
    private Object paradiseLost$aliasGetValueByKey(Object original, ResourceKey<?> key) {
        if (original != null || key == null) return original;
        Identifier mapped = ParadiseLostAliasFix.resolve(key.identifier());
        return mapped == null ? original : paradiseLost$registry().getValue(mapped);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ModifyReturnValue(method = "get(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;", at = @At("RETURN"))
    private Optional<?> paradiseLost$aliasGetByKey(Optional<?> original, ResourceKey<?> key) {
        if (original == null || !original.isEmpty() || key == null) return original;
        Identifier mapped = ParadiseLostAliasFix.resolve(key.identifier());
        return mapped == null ? original : paradiseLost$registry().get(mapped);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ModifyReturnValue(method = "containsKey(Lnet/minecraft/resources/ResourceKey;)Z", at = @At("RETURN"))
    private boolean paradiseLost$aliasContainsByKey(boolean original, ResourceKey<?> key) {
        if (original || key == null) return original;
        Identifier mapped = ParadiseLostAliasFix.resolve(key.identifier());
        return mapped != null && paradiseLost$registry().containsKey(mapped);
    }
}
