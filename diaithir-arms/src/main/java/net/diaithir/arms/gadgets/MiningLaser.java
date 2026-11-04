package net.diaithir.arms.gadgets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Mining laser (survey part 2, clean-room from the behaviour of Mining Gadgets): use to cut the block in the crosshair up to
 * {@link #RANGE} blocks away. Blocks harder than {@link #MAX_HARDNESS} (obsidian and up) and unbreakable blocks are refused; each cut costs one
 * point of durability and the item cools down for {@link #COOLDOWN} ticks. Drops follow the normal loot table, as if mined by hand.
 */
public class MiningLaser extends Item {
    public static final int RANGE = 16, COOLDOWN = 8;
    public static final float MAX_HARDNESS = 10.0F;

    public MiningLaser(Properties p) { super(p); }

    /** Pure rule: may the laser cut a block of this hardness? (negative hardness is unbreakable) */
    public static boolean canCut(float hardness) { return hardness >= 0.0F && hardness <= MAX_HARDNESS; }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(RANGE));
        BlockHitResult r = level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (r.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel sl)) return InteractionResult.SUCCESS;
        BlockPos pos = r.getBlockPos();
        BlockState st = level.getBlockState(pos);
        if (!canCut(st.getDestroySpeed(level, pos))) return InteractionResult.FAIL;
        if (!player.mayBuild() && !player.getAbilities().mayBuild) return InteractionResult.FAIL;
        for (int i = 1; i <= 10; i++) {
            Vec3 p = eye.add(0, -0.2, 0).lerp(Vec3.atCenterOf(pos), i / 10.0);
            sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        level.destroyBlock(pos, !player.isCreative(), player);
        level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.6F, 2.0F);
        stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        player.getCooldowns().addCooldown(stack, COOLDOWN);
        return InteractionResult.SUCCESS_SERVER;
    }
}
