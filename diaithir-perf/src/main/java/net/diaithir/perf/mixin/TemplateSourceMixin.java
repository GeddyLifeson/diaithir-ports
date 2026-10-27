package net.diaithir.perf.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.diaithir.perf.StructureUpgradeCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.structure.templatesystem.loader.TemplateSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TemplateSource.class)
abstract class TemplateSourceMixin {
    @WrapOperation(method = "readStructure(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplate;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/datafix/DataFixTypes;updateToCurrentVersion(Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag diaithirperf$cachedUpgrade(DataFixTypes type, DataFixer fixer, CompoundTag tag, int version, Operation<CompoundTag> original) {
        return StructureUpgradeCache.upgrade(tag, version, t -> original.call(type, fixer, t, version));
    }
}
