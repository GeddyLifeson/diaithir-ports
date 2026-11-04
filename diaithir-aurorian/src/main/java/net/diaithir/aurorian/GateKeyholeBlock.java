package net.diaithir.aurorian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** A key-hole: the matching key (or lockpicks, 1 in 3 per try, where allowed) opens every dungeon gate within 3 blocks. */
public class GateKeyholeBlock extends Block {
    private static final int RADIUS = 3;
    private static final TagKey<Block> GATES = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(DiaithirAurorian.ID, "dungeon_gates"));
    private final Identifier key;
    private final boolean lockpick;

    public GateKeyholeBlock(Properties p, Identifier key, boolean lockpick) {
        super(p);
        this.key = key;
        this.lockpick = lockpick;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean isKey = BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(key);
        boolean isPick = lockpick && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(Identifier.fromNamespaceAndPath(DiaithirAurorian.ID, "lockpicks"));
        if (!isKey && !isPick) {
            player.sendOverlayMessage(Component.translatable(lockpick ? "string.theaurorian.gate_key_lockpick" : "string.theaurorian.gate_key"));
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        stack.hurtAndBreak(1, player, hand);
        if (isPick && player.getRandom().nextDouble() < 0.66) {
            return InteractionResult.FAIL;
        }
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-RADIUS, -RADIUS, -RADIUS), pos.offset(RADIUS, RADIUS, RADIUS))) {
            if (level.getBlockState(p).is(GATES)) {
                level.destroyBlock(p, false);
            }
        }
        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1, 1);
        level.destroyBlock(pos, false);
        return InteractionResult.SUCCESS;
    }
}
