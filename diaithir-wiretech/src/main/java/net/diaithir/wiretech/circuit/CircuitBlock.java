package net.diaithir.wiretech.circuit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.diaithir.wiretech.Machines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A circuit block. FACING is the way the player looked when placing: the output is on the far side, the input on the near side. */
public final class CircuitBlock extends BaseEntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final MapCodec<CircuitBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(propertiesCodec(),
            Codec.INT.fieldOf("kind").forGetter(b -> b.kind.ordinal()), Codec.INT.fieldOf("tier").forGetter(b -> b.tier.ordinal()))
            .apply(i, (p, k, t) -> new CircuitBlock(p, CircuitKind.values()[k], Tier.values()[t])));

    public final CircuitKind kind;
    public final Tier tier;

    public CircuitBlock(Properties properties, CircuitKind kind, Tier tier) {
        super(properties);
        this.kind = kind;
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitNodeBE(pos, state);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return kind == CircuitKind.GATE;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return kind == CircuitKind.GATE && state.getValue(LIT) ? 15 : 0;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (level.getBlockEntity(pos) instanceof CircuitNodeBE be && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            sp.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) -> new CircuitMenu(id, inv, pos),
                    Component.literal(be.kind.display)), buf -> CircuitMenu.write(buf, be));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * Circuit control for any block: true unless a circuit gate touches pos and is off. Machines and other modules call this to obey a gate;
     * a gate also gives redstone power when on, so anything that reads redstone obeys it too.
     */
    public static boolean gatesAllow(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            BlockState s = level.getBlockState(pos.relative(d));
            if (s.getBlock() instanceof CircuitBlock cb && cb.kind == CircuitKind.GATE && !s.getValue(LIT)) return false;
        }
        return true;
    }
}
