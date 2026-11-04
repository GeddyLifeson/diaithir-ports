package net.diaithir.steamrails;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * A steam whistle. Touching a boiler, a redstone signal blows it (and repeats while the signal stays). Each blast spends a little steam, and
 * the pitch rises with boiler pressure. On a moving train the boiler blows it when the train pulls out.
 */
public final class WhistleBlock extends Block {
    public static final MapCodec<WhistleBlock> CODEC = simpleCodec(WhistleBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 12, 12);
    /** Steam spent per blast and the pressure below which the whistle only hisses. */
    public static final float COST = 0.8F;
    public static final float MIN_PRESSURE = 1.5F;

    public WhistleBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), 3);
            if (powered) {
                blow(level, pos);
                level.scheduleTick(pos, this, 30);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            if (level.hasNeighborSignal(pos)) {
                blow(level, pos);
                level.scheduleTick(pos, this, 30);
            } else {
                level.setBlock(pos, state.setValue(POWERED, false), 3);
            }
        }
    }

    /** Blows from a block in the world. True when the boiler had the steam. */
    public static boolean blow(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel sl)) return false;
        BoilerBlockEntity boiler = null;
        for (Direction d : Direction.values()) {
            boiler = BoilerBlockEntity.masterOf(level, pos.relative(d));
            if (boiler != null) break;
        }
        boolean ok = boiler != null && boiler.draw(COST, MIN_PRESSURE);
        float frac = boiler == null || boiler.layout(level) == null ? 0 : boiler.state.pressure / boiler.layout(level).rating;
        sound(sl, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, ok, frac);
        return ok;
    }

    /** The audible and visible part, shared with whistles on a train. */
    public static void sound(ServerLevel level, double x, double y, double z, boolean ok, float pressureFraction) {
        if (ok) {
            float pitch = 0.75F + 0.6F * Math.min(1F, pressureFraction);
            level.playSound(null, x, y, z, SteamBlocks.WHISTLE_SOUND.get(), SoundSource.BLOCKS, 3.0F, pitch);
            level.sendParticles(ParticleTypes.CLOUD, x, y + 0.4, z, 12, 0.12, 0.2, 0.12, 0.04);
        } else {
            level.playSound(null, x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4F, 1.6F);
        }
    }
}
