package net.diaithir.arms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.MapCodec;
import java.io.InputStreamReader;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Historical siege engines: one placeable, data-driven block per design (historical_siege.json). Right-click with the ammo to load it,
 *  right-click with anything else to fire along your line of sight. */
public final class Siege {
    /** One entry of historical_siege.json (written by tools/gen_historical_siege.py). */
    public record Design(String id, String kind, String ammo, int ammoCount, float damage, float power, float speed, int volley, boolean breaks, boolean fire) {}

    public static final List<Design> DESIGNS = load();

    private static List<Design> load() {
        try (var r = new InputStreamReader(Siege.class.getResourceAsStream("/data/" + DiaithirArms.ID + "/historical_siege.json"))) {
            return new Gson().fromJson(r, new TypeToken<List<Design>>() {}.getType());
        } catch (Exception e) {
            throw new IllegalStateException("historical_siege.json missing", e);
        }
    }

    public static final class Engine extends Block {
        public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
        public static final BooleanProperty LOADED = BooleanProperty.create("loaded");
        public static final MapCodec<Engine> CODEC = simpleCodec(p -> new Engine(p, DESIGNS.get(0)));
        private final Design design;

        public Engine(BlockBehaviour.Properties p, Design design) {
            super(p);
            this.design = design;
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LOADED, false));
        }

        @Override protected MapCodec<? extends Block> codec() { return CODEC; }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) { b.add(FACING, LOADED); }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext c) { return defaultBlockState().setValue(FACING, c.getHorizontalDirection().getOpposite()); }

        private Item ammoItem() { return design.ammo() == null ? null : BuiltInRegistries.ITEM.getValue(Identifier.parse(design.ammo())); }

        @Override
        protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            Item ammo = ammoItem();
            if (ammo == null || !stack.is(ammo) || state.getValue(LOADED)) return InteractionResult.TRY_WITH_EMPTY_HAND;
            if (stack.getCount() < design.ammoCount() && !player.hasInfiniteMaterials()) return InteractionResult.FAIL;
            if (!level.isClientSide()) {
                if (!player.hasInfiniteMaterials()) stack.shrink(design.ammoCount());
                level.setBlockAndUpdate(pos, state.setValue(LOADED, true));
                level.playSound(null, pos, SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.BLOCKS, 1.0F, 0.7F);
            }
            return InteractionResult.SUCCESS;
        }

        @Override
        protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
            if (!(level instanceof ServerLevel sl)) return InteractionResult.SUCCESS;
            boolean ram = design.kind().equals("ram");
            if (!ram && !state.getValue(LOADED)) return InteractionResult.PASS;
            if (!ram) level.setBlockAndUpdate(pos, state.setValue(LOADED, false));
            fire(sl, pos, state, player);
            return InteractionResult.SUCCESS;
        }

        private void fire(ServerLevel sl, BlockPos pos, BlockState state, Player player) {
            Vec3 look = player.getLookAngle();
            Vec3 from = Vec3.atCenterOf(pos).add(0, 0.7, 0).add(look.scale(1.2));
            switch (design.kind()) {
                case "ram" -> ram(sl, pos, state.getValue(FACING).getOpposite(), player);
                case "flame" -> flame(sl, from, look, player);
                default -> {
                    ItemStack shown = new ItemStack(design.ammo() == null ? net.minecraft.world.item.Items.COBBLESTONE : ammoItem());
                    for (int i = 0; i < design.volley(); i++) {
                        Thrown.Shot shot = new Thrown.Shot(DiaithirArms.THROWN.get(), sl, player, shown, design.damage(), design.power(), design.breaks(), design.fire());
                        shot.setPos(from.x, from.y, from.z);
                        float spread = design.volley() > 1 ? 6.0F : 0.3F;
                        Projectile.spawnProjectileUsingShoot(shot, sl, shown, from.x + look.x, from.y + look.y, from.z + look.z, design.speed(), spread);
                    }
                    sl.playSound(null, pos, design.power() > 0 ? SoundEvents.GENERIC_EXPLODE.value() : SoundEvents.CROSSBOW_SHOOT, SoundSource.BLOCKS, 1.5F, 0.8F);
                }
            }
        }

        private void flame(ServerLevel sl, Vec3 from, Vec3 look, Player owner) {
            AABB box = new AABB(from, from.add(look.scale(8))).inflate(1.5);
            for (Entity e : sl.getEntities(owner, box)) {
                if (e instanceof LivingEntity && e.position().subtract(from).normalize().dot(look) > 0.8) e.igniteForSeconds(6);
            }
            sl.playSound(null, BlockPos.containing(from), SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 0.6F);
        }

        private void ram(ServerLevel sl, BlockPos pos, Direction dir, Player owner) {
            for (int i = 1; i <= 2; i++) {
                BlockPos p = pos.relative(dir, i);
                BlockState s = sl.getBlockState(p);
                float hardness = s.getDestroySpeed(sl, p);
                if (!s.isAir() && hardness >= 0 && hardness <= 5.0F) sl.destroyBlock(p, true, owner);
            }
            for (Entity e : sl.getEntities(owner, new AABB(pos.relative(dir, 1)).inflate(1.0))) {
                if (e instanceof LivingEntity) e.hurt(sl.damageSources().playerAttack(owner), design.damage());
            }
            sl.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 1.0F, 0.5F);
        }
    }

    private Siege() {}
}
