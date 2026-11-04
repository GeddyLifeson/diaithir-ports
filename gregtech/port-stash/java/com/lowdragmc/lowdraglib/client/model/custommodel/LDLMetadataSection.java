package com.lowdragmc.lowdraglib.client.model.custommodel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnlyIn(Dist.CLIENT)
public class LDLMetadataSection {
    public static final String SECTION_NAME = LDLib.MOD_ID;
    private static final Map<Identifier, LDLMetadataSection> METADATA_CACHE = new ConcurrentHashMap<>();
    public static final LDLMetadataSection MISSING = new LDLMetadataSection(false, null);

    public final boolean emissive;
    public final Identifier connection;

    public LDLMetadataSection(boolean emissive, Identifier connection) {
        this.emissive = emissive;
        this.connection = connection;
    }

    public static void clearCache() {
        METADATA_CACHE.clear();
    }

    public boolean isMissing() {
        return this == MISSING;
    }

    @Nonnull
    public static LDLMetadataSection getMetadata(Identifier res) { return null; /* ported stub */ }

    @Nonnull
    public static LDLMetadataSection getMetadata(TextureAtlasSprite sprite) {
        return getMetadata(spriteToAbsolute(sprite.contents().name()));
    }

    public static boolean isEmissive(TextureAtlasSprite sprite) {
        LDLMetadataSection ret = getMetadata(spriteToAbsolute(sprite.contents().name()));
        return ret.emissive;
    }

    @Nullable
    public static TextureAtlasSprite getConnection(TextureAtlasSprite sprite) {
        LDLMetadataSection ret = getMetadata(spriteToAbsolute(sprite.contents().name()));
        return ret.connection == null ? null : ModelFactory.getBlockSprite(ret.connection);
    }

    public static Identifier spriteToAbsolute(Identifier sprite) {
        if (!sprite.getPath().startsWith("textures/")) {
            sprite = Identifier.fromNamespaceAndPath(sprite.getNamespace(), "textures/" + sprite.getPath());
        }
        if (!sprite.getPath().endsWith(".png")) {
            sprite = Identifier.fromNamespaceAndPath(sprite.getNamespace(), sprite.getPath() + ".png");
        }
        return sprite;
    }

    public static class Serializer implements MetadataSectionSerializer<LDLMetadataSection> {
        static Serializer INSTANCE = new Serializer();

        @Override
        @Nonnull
        public String getMetadataSectionName() { return null; /* ported stub */ }

        @Override
        @Nonnull
        public LDLMetadataSection fromJson(@Nonnull JsonObject json) { return null; /* ported stub */ }
    }
}
