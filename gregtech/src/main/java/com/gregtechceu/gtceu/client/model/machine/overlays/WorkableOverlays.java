package com.gregtechceu.gtceu.client.model.machine.overlays;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.Status;
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider;


import com.gregtechceu.gtceu.utils.data.RuntimeExistingFileHelper;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;


import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class WorkableOverlays {/* ported: removed */

    @Getter
    private final Identifier location;

    @Getter
    private final Map<OverlayFace, StatusTextures> textures = new EnumMap<>(OverlayFace.class);

    public WorkableOverlays(Identifier location) {
        this.location = location;
    }

    public enum OverlayFace {

        FRONT,
        BACK,
        TOP,
        BOTTOM,
        SIDE;

        public static final OverlayFace[] VALUES = values();

        public static OverlayFace bySide(Direction side) {
            return switch (side) {
                case DOWN -> BOTTOM;
                case UP -> TOP;
                case NORTH -> FRONT;
                case SOUTH -> BACK;
                case WEST, EAST -> SIDE;
            };
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public static class StatusTextures {

        public static final StatusTextures EMPTY = new StatusTextures();

        private final Map<Status, Identifier> textures = new EnumMap<>(Status.class);
        private final Map<Status, Identifier> emissiveTextures = new EnumMap<>(Status.class);

        public StatusTextures(@Nullable Identifier normalSprite,
                              @Nullable Identifier activeSprite,
                              @Nullable Identifier pausedSprite,
                              @Nullable Identifier normalSpriteEmissive,
                              @Nullable Identifier activeSpriteEmissive,
                              @Nullable Identifier pausedSpriteEmissive) {
            textures.put(Status.IDLE, normalSprite);
            emissiveTextures.put(Status.IDLE, normalSpriteEmissive);

            textures.put(Status.WORKING, activeSprite);
            emissiveTextures.put(Status.WORKING, activeSpriteEmissive);
            textures.put(Status.WAITING, activeSprite);
            emissiveTextures.put(Status.WAITING, activeSpriteEmissive);

            textures.put(Status.SUSPEND, pausedSprite);
            emissiveTextures.put(Status.SUSPEND, pausedSpriteEmissive);
        }

        private StatusTextures() {}

        public @NotNull Identifier getTexture(@NotNull Status status) {
            Identifier value = textures.get(status);
            return value != null ? value : GTModels.BLANK_TEXTURE;
        }

        public @NotNull Identifier getEmissiveTexture(@NotNull Status status) {
            Identifier value = emissiveTextures.get(status);
            return value != null ? value : GTModels.BLANK_TEXTURE;
        }
    }
}
