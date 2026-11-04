package net.diaithir.arms.gadgets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Sentry turret (survey part 2 turrets, clean-room from the behaviour of Jungdab's Turrets): a block that shoots the nearest hostile
 * mob in line of sight within {@link #RANGE} blocks every {@link #PERIOD} ticks. Ammo is a block-state level 0..{@link #MAX}: an arrow
 * loads {@link #PER_ARROW} shots, a spectral arrow loads twice as many. Right-click with an empty hand reports the level. Players and
 * tamed or passive mobs are never targeted. The loop is a scheduled tick, so it needs no block entity and survives restarts.
 */
public class SentryTurret extends Block {
    public static final int MAX = 32, PER_ARROW = 4, RANGE = 14, PERIOD = 12;
    public static final float DAMAGE = 4.0F;
    public static final IntegerProperty AMMO = IntegerProperty.create("ammo", 0, MAX);

    public SentryTurret(Properties p) {
        super(p);
        registerDefaultState(stateDefinition.any().setValue(AMMO, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(AMMO); }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!level.isClientSide() && !old.is(this)) level.scheduleTick(pos, this, PERIOD);
    }

    /** Ammo after loading n arrows of the given weight; pure for tests. */
    public static int loaded(int ammo, int arrows, int weight) { return Math.min(MAX, ammo + arrows * PER_ARROW * weight); }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int weight = stack.is(Items.ARROW) ? 1 : stack.is(Items.SPECTRAL_ARROW) ? 2 : 0;
        if (weight == 0) return InteractionResult.TRY_WITH_EMPTY_HAND;
        int ammo = state.getValue(AMMO);
        if (ammo >= MAX) return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            int need = (int) Math.ceil((MAX - ammo) / (double) (PER_ARROW * weight));
            int n = Math.min(stack.getCount(), need);
            if (!player.getAbilities().instabuild) stack.shrink(n);
            level.setBlock(pos, state.setValue(AMMO, loaded(ammo, n, weight)), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof net.minecraft.server.level.ServerPlayer sp) sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("Sentry turret: " + state.getValue(AMMO) + " of " + MAX + " shots"), true);
        return InteractionResult.SUCCESS;
    }

    static boolean hostile(LivingEntity e) { return e instanceof Enemy && e.isAlive(); }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource rnd) {
        level.scheduleTick(pos, this, PERIOD);
        int ammo = state.getValue(AMMO);
        if (ammo <= 0) return;
        Vec3 from = Vec3.atCenterOf(pos).add(0, 0.6, 0);
        LivingEntity best = null; double bd = RANGE * RANGE + 1.0;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(RANGE), SentryTurret::hostile)) {
            double d = e.distanceToSqr(from);
            if (d >= bd) continue;
            Vec3 to = e.getEyePosition();
            HitResult r = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, e));
            if (r.getType() != HitResult.Type.MISS && r.getLocation().distanceToSqr(to) > 1.0) continue;
            best = e; bd = d;
        }
        if (best == null) return;
        level.setBlock(pos, state.setValue(AMMO, ammo - 1), Block.UPDATE_CLIENTS);
        best.hurtServer(level, level.damageSources().arrow(null, null), DAMAGE);
        Vec3 to = best.getEyePosition();
        for (int i = 1; i <= 8; i++) {
            Vec3 p = from.lerp(to, i / 8.0);
            level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
        }
        level.playSound(null, pos, SoundEvents.ARROW_SHOOT, SoundSource.BLOCKS, 0.8F, 1.2F);
    }
}
