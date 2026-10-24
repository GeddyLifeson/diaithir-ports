package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.pack.DynamicPack;
import com.zurrtum.create.foundation.pack.RuntimeDataGenerator;
import com.zurrtum.create.platform.FabricLoader;
import com.zurrtum.create.platform.ModContainer;
import com.zurrtum.create.platform.ModContainer.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.repository.Pack.Metadata;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(BuiltInPackSource.class)
public class BuiltInPackSourceMixin {
    @Inject(method = "loadPacks(Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void addDataPack(Consumer<Pack> consumer, CallbackInfo ci) {
        if ((Object) this instanceof ServerPacksSource) {
            DynamicPack dynamicPack = new DynamicPack(
                "create:dynamic_data",
                Component.translatable("advancement.create.root"),
                PackType.SERVER_DATA
            );
            RuntimeDataGenerator.insertIntoPack(dynamicPack);
            if (!dynamicPack.isEmpty()) {
                addDataPack(consumer, false, dynamicPack);
            }
        }
    }

    @Unique
    private static PackLocationInfo createInfo(String id, String name, String version) {
        return new PackLocationInfo(
            id,
            Component.nullToEmpty(name),
            PackSource.BUILT_IN,
            Optional.of(new KnownPack(id, "data", version))
        );
    }

    @Unique
    private static void addDataPack(
        Consumer<Pack> consumer,
        PackLocationInfo info,
        List<Path> paths,
        Component title,
        String directory,
        String... namespace
    ) {
        ResourceMetadata metadataMap = ResourceMetadata.of(
            PackMetadataSection.SERVER_TYPE,
            new PackMetadataSection(
                title,
                SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minorRange()
            )
        );
        VanillaPackResourcesBuilder builder = new VanillaPackResourcesBuilder().setMetadata(metadataMap)
            .exposeNamespace(namespace);
        for (Path path : paths) {
            builder.pushAssetPath(PackType.SERVER_DATA, path.resolve(directory));
        }
        addDataPack(consumer, true, builder.build(info));
    }

    @Unique
    private static void addDataPack(Consumer<Pack> consumer, boolean required, PackResources pack) {
        Pack.ResourcesSupplier packFactory = new Pack.ResourcesSupplier() {
            @Override
            @NonNull
            public PackResources openPrimary(@Nullable PackLocationInfo info) {
                return pack;
            }

            @Override
            @NonNull
            public PackResources openFull(@NonNull PackLocationInfo info, @NonNull Metadata metadata) {
                return pack;
            }
        };
        PackSelectionConfig position = new PackSelectionConfig(required, Pack.Position.BOTTOM, false);
        consumer.accept(Pack.readMetaAndCreate(pack.location(), packFactory, PackType.SERVER_DATA, position));
    }
}
