package com.gregtechceu.gtceu.client.model.machine.overlays;

import net.minecraft.resources.Identifier;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;/* ported: removed */


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class HPCAOverlay {/* ported: removed */
    // spotless:on

    public static final HPCAOverlay EMPTY = new HPCAOverlay();

    private final Map<Status, Identifier> textures = new EnumMap<>(Status.class);
    private final Map<Status, Identifier> emissiveTextures = new EnumMap<>(Status.class);

    public HPCAOverlay(@Nullable Identifier normalSprite,
                       @Nullable Identifier activeSprite,
                       @Nullable Identifier damagedSprite,
                       @Nullable Identifier damagedActiveSprite,
                       @Nullable Identifier normalSpriteEmissive,
                       @Nullable Identifier activeSpriteEmissive,
                       @Nullable Identifier damagedSpriteEmissive,
                       @Nullable Identifier damagedActiveSpriteEmissive) {
        textures.put(Status.IDLE, normalSprite);
        emissiveTextures.put(Status.IDLE, normalSpriteEmissive);

        textures.put(Status.WORKING, activeSprite);
        emissiveTextures.put(Status.WORKING, activeSpriteEmissive);

        textures.put(Status.WAITING, damagedActiveSprite);
        emissiveTextures.put(Status.WAITING, damagedActiveSpriteEmissive);
        textures.put(Status.SUSPEND, damagedSprite);
        emissiveTextures.put(Status.SUSPEND, damagedSpriteEmissive);
    }

    private HPCAOverlay() {}

    private static Status getStatus(boolean active, boolean damaged) {
        if (damaged && active) return Status.WAITING;
        else if (damaged) return Status.SUSPEND;
        else if (active) return Status.WORKING;
        else return Status.IDLE;
    }

    public @NotNull Identifier getTexture(boolean active, boolean damaged) { return null; /* ported stub */ }

    public @NotNull Identifier getEmissiveTexture(boolean active, boolean damaged) { return null; /* ported stub */ }
}
