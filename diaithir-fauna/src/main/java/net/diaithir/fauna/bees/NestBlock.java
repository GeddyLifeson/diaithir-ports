package net.diaithir.fauna.bees;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** A wild nest of one species. Now and then it lets out a bee of its species, up to three of that species within 24 blocks. Comes up in the world; breaking it drops a bee jar's worth of nothing but the nest itself. */
public final class NestBlock extends Block {
    public static final int MAX_NEARBY = 3, RANGE = 24;
    private final String species;

    public NestBlock(Properties props, String species) {
        super(props.randomTicks());
        this.species = species;
    }

    public String species() { return species; }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) != 0 || level.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos.above()) < 8) return;
        spawn(level, pos);
    }

    /** Lets a bee out if fewer than MAX_NEARBY of this species are near. Returns whether one came out. */
    public boolean spawn(ServerLevel level, BlockPos pos) {
        int near = level.getEntitiesOfClass(Bee.class, new net.minecraft.world.phys.AABB(pos).inflate(RANGE), b -> Bees.speciesOf(b).equals(species)).size();
        return room(near) && BeeJarItem.release(level, Genes.of(species, 0, 0), pos.above()) != null;
    }

    static boolean room(int nearby) { return nearby < MAX_NEARBY; }
}
