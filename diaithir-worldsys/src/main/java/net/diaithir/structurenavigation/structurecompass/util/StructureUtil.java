// Ported from Structure Compass (MIT) https://github.com/Mrbysco/StructureCompass
// Modifications: Config accessors renamed, wildcard parsing hardened.
package net.diaithir.structurenavigation.structurecompass.util;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.diaithir.structurenavigation.structurecompass.CompassIds;
import net.diaithir.structurenavigation.structurecompass.config.CompassConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.Tags;

public final class StructureUtil {
    private StructureUtil() {}

    public static List<Identifier> getAvailableStructureList(Level level) {
        List<Identifier> structureList = new ArrayList<>();
        Registry<Structure> registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        registry.asHolderIdMap().forEach(holder -> {
            if (holder.is(Tags.Structures.HIDDEN_FROM_LOCATOR_SELECTION)) return;
            Identifier location = holder.getKey().identifier();
            if (!isBlacklisted(location) && !structureList.contains(location)) {
                structureList.add(location);
            }
        });
        return structureList;
    }

    public static List<Identifier> getAvailableTagList(Level level) {
        List<Identifier> tagList = new ArrayList<>();
        Registry<Structure> registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        registry.getTags().forEach(tag -> {
            if (!tagList.contains(tag.key().location())) {
                tagList.add(tag.key().location());
            }
        });
        tagList.removeIf(tag -> tag.equals(Tags.Structures.HIDDEN_FROM_LOCATOR_SELECTION.location()));
        return tagList;
    }

    public static boolean isBlacklisted(Identifier structureLocation) {
        if (structureLocation == null) {
            CompassIds.LOGGER.error("Checking blacklist but fed location is null!");
            return false;
        }
        List<? extends String> blacklist = CompassConfig.STRUCTURE_BLACKLIST.get();
        if (blacklist.isEmpty()) {
            return false;
        }
        if (blacklist.contains(structureLocation.toString())) {
            return true;
        }
        for (String wildcard : blacklist) {
            if (!wildcard.contains(":") || !wildcard.contains("*")) continue;
            String[] split = wildcard.split(":");
            if (split.length < 2) continue;
            if ((split[0].equals("*") && structureLocation.getPath().equals(split[1]))
                    || (split[1].equals("*") && structureLocation.getNamespace().equals(split[0]))) {
                return true;
            }
        }
        return false;
    }

    public static Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel serverLevel,
                                                                            HolderSet<Structure> structureHolderSet, BlockPos pos, int range, boolean findUnexplored) {
        ChunkGenerator generator = serverLevel.getChunkSource().getGenerator();
        Pair<BlockPos, Holder<Structure>> nearest = generator.findNearestMapStructure(serverLevel, structureHolderSet, pos, range, findUnexplored);
        if (nearest == null) return null;
        return nearest.getFirst().distManhattan(pos) <= CompassConfig.COMPASS_RANGE.get() ? nearest : null;
    }

    public static Component getStructureName(Identifier structureLocation, boolean isTag) {
        if (isTag) {
            return Component.translatableWithFallback(getTagTranslationKey(structureLocation), "#" + structureLocation.toString());
        }
        return Component.translatableWithFallback(structureLocation.toLanguageKey("structure"), structureLocation.toString());
    }

    private static String getTagTranslationKey(Identifier tagIdentifier) {
        Identifier registryIdentifier = Registries.STRUCTURE.identifier();
        return "tag."
                + registryIdentifier.toShortLanguageKey().replace("/", ".")
                + "."
                + tagIdentifier.getNamespace()
                + "."
                + tagIdentifier.getPath().replace("/", ".");
    }
}
