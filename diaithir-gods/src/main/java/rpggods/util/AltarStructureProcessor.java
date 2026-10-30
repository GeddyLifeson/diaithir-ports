package rpggods.util;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import rpggods.RGRegistry;
import rpggods.RPGGods;
import rpggods.deity.Altar;
import rpggods.entity.AltarEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AltarStructureProcessor extends StructureProcessor {

    public static final AltarStructureProcessor PROCESSOR = new AltarStructureProcessor();
    public static final MapCodec<AltarStructureProcessor> CODEC = MapCodec.unit(() -> PROCESSOR);

    public AltarStructureProcessor() {
        super();
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return RGRegistry.ALTAR_STRUCTURE_PROCESSOR.get();
    }

    @Override
    public StructureTemplate.StructureEntityInfo processEntity(LevelReader level, BlockPos seedPos,
                                                               StructureTemplate.StructureEntityInfo rawEntityInfo,
                                                               StructureTemplate.StructureEntityInfo entityInfo,
                                                               StructurePlaceSettings placementSettings,
                                                               StructureTemplate template) {
        // read entity type from tag
        CompoundTag tag = entityInfo.nbt.copy();
        String entityId = tag.getStringOr("id", "");
        // ensure entity is altar
        if (RGRegistry.ALTAR_TYPE.getId().toString().equals(entityId)) {
            // determine random instance
            final long seed = placementSettings.getBoundingBox().hashCode();
            RandomSource random = RandomSource.create(seed);
            // create list of altars with deities
            List<Tuple<Identifier, Altar>> altarList = new ArrayList<>();
            for(Map.Entry<Identifier, Altar> entry : RPGGods.ALTAR_MAP.entrySet()) {
                if(entry.getValue().getDeity().isPresent()) {
                    altarList.add(new Tuple<>(entry.getKey(), entry.getValue()));
                }
            }
            // attempt to choose random altar from list
            if (!altarList.isEmpty()) {
                Tuple<Identifier, Altar> selected;
                // sort list, then choose random element
                altarList.sort((tuple1, tuple2) -> tuple1.getA().compareNamespaced(tuple2.getA()));
                selected = altarList.get(random.nextInt(altarList.size()));
                // attempt to write deity to altar
                CompoundTag modified = AltarEntity.writeAltarProperties(level.registryAccess(), selected.getA(), selected.getB());
                tag.merge(modified);
            }
            // create modified entity info
            return new StructureTemplate.StructureEntityInfo(entityInfo.pos, entityInfo.blockPos, tag);
        }
        return entityInfo;
    }
}
