package net.diaithir.claims.graves;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** A grave. Its owner (or anyone with permission level 2) opens it and gets everything back; nobody else can break or open it until the grace period has passed. */
public class GraveBlock extends BaseEntityBlock {
    public static final MapCodec<GraveBlock> CODEC = simpleCodec(GraveBlock::new);
    /** Ticks after death before strangers may loot a grave: 20 minutes. */
    public static final long PROTECTED_TICKS = 20L * 60 * 20;

    public GraveBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GraveEntity(pos, state);
    }

    static boolean mayOpen(GraveEntity g, Player p, Level level) {
        if (g.owner == null || g.owner.equals(p.getUUID())) return true;
        if (p.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER)) return true;
        return level.getGameTime() - g.died > PROTECTED_TICKS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof GraveEntity g)) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (!mayOpen(g, player, level)) {
            player.sendOverlayMessage(Component.literal("This is the grave of " + g.ownerName + ". It is theirs for now."));
            return InteractionResult.SUCCESS_SERVER;
        }
        Graves.empty((ServerLevel) level, pos, g, (ServerPlayer) player);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof GraveEntity g && player.level() != null && !mayOpen(g, player, player.level())) return 0F;
        return super.getDestroyProgress(state, player, level, pos);
    }
}
