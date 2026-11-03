package net.diaithir.fauna.bees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** /fauna beetest: the species table (every species reachable from wild ones, outputs exist), crossing, the jar, and an apiary making combs under flowers. */
public final class BeeTest {
    private BeeTest() {}

    public static List<String> run(MinecraftServer server) {
        List<String> fails = new ArrayList<>();
        ServerLevel level = server.overworld();

        Set<String> have = new HashSet<>();
        for (BeeSpecies s : BeeSpecies.ALL) if (s.wild() > 0) have.add(s.id());
        for (boolean grew = true; grew; ) {
            grew = false;
            for (BeeSpecies s : BeeSpecies.ALL) if (s.isCross() && !have.contains(s.id()) && have.contains(s.parentA()) && have.contains(s.parentB())) { have.add(s.id()); grew = true; }
        }
        for (BeeSpecies s : BeeSpecies.ALL) {
            if (!have.contains(s.id())) fails.add(s.id() + " cannot be bred from wild bees");
            if (!BuiltInRegistries.ITEM.containsKey(Identifier.parse(s.outItem()))) fails.add(s.id() + " output " + s.outItem() + " missing");
            if (!s.id().equals(BeeSpecies.DEFAULT) && !Bees.COMBS.containsKey(s.id())) fails.add(s.id() + " has no comb item");
        }
        if (BeeSpecies.crossOf("muddy", "coal") == null || BeeSpecies.crossOf("coal", "muddy") != BeeSpecies.crossOf("muddy", "coal")) fails.add("cross order matters");

        BlockPos at = new BlockPos(500, 100, 500);
        level.getChunk(at);
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) level.setBlock(at.offset(dx, -1, dz), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        level.setBlock(at, Bees.APIARY.get().defaultBlockState(), 3);
        for (int i = 0; i < 4; i++) level.setBlock(at.offset(2, 0, i - 1), Blocks.DANDELION.defaultBlockState(), 3);
        if (!(level.getBlockEntity(at) instanceof ApiaryEntity be)) { fails.add("no apiary entity"); return fails; }

        // crossing through the real inheritance code
        Bee a = EntityType.BEE.create(level, EntitySpawnReason.COMMAND), b = EntityType.BEE.create(level, EntitySpawnReason.COMMAND), child = EntityType.BEE.create(level, EntitySpawnReason.COMMAND);
        a.setData(Bees.SPECIES.get(), "meadow");
        b.setData(Bees.SPECIES.get(), "sandy");
        Bees.inherit(a, b, child);
        if (!"coal".equals(Bees.speciesOf(child))) fails.add("meadow x sandy gave '" + Bees.speciesOf(child) + "'");
        child.setData(Bees.SPECIES.get(), "");
        Bees.inherit(a, a, child);
        if (!"meadow".equals(Bees.speciesOf(child))) fails.add("same parents gave '" + Bees.speciesOf(child) + "'");

        // jar: a captured bee's species survives in the jar and comes back out
        var jar = BeeJarItem.filled("coal");
        if (!"coal".equals(BeeJarItem.species(jar))) fails.add("jar lost its species");
        if (!be.addBee("coal") || !be.addBee("iron") || !be.addBee("meadow")) fails.add("apiary refused bees");
        if (be.addBee("gold")) fails.add("apiary took a fourth bee");

        be.ripen();
        be.serverTick(level, 20);
        if (be.combCount("coal") != 1 || be.combCount("iron") != 1 || be.combCount("meadow") != 1) fails.add("combs not made: " + be.status());

        // no flowers, no combs
        for (int i = 0; i < 4; i++) level.setBlock(at.offset(2, 0, i - 1), Blocks.AIR.defaultBlockState(), 3);
        be.countFlowersForTest(level);
        be.ripen();
        be.serverTick(level, 20);
        if (be.combCount("coal") != 1) fails.add("made a comb without flowers");

        net.minecraft.nbt.CompoundTag tag = be.saveWithoutMetadata(server.registryAccess());
        if (!tag.getStringOr("bees", "").contains("coal") || tag.getIntOr("comb_coal", 0) != 1) fails.add("apiary state not saved: " + tag);

        if (be.takeBees().size() != 3) fails.add("takeBees");
        level.setBlock(at, Blocks.AIR.defaultBlockState(), 3);
        return fails;
    }
}
