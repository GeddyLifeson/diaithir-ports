package com.ldtteam.blockui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Dropped in the 26.1.2 port: per-mod gui atlases need the rewritten atlas system. Sprites come from the vanilla gui atlas.
 */
public class AtlasManager
{
    public static final AtlasManager INSTANCE = new AtlasManager();

    private AtlasManager()
    {}

    public void addAtlas(final Consumer<PreparableReloadListener> resourceRegistry, final String modId)
    {}

    public TextureAtlasSprite getSprite(final Identifier resLoc)
    {
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.GUI).getSprite(resLoc);
    }

    public void dumpAtlases(final Path dumpingFolder)
    {}
}
