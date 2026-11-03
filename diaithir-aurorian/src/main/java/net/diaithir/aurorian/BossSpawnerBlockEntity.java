package net.diaithir.aurorian;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class BossSpawnerBlockEntity extends BlockEntity {
    private static final int RANGE = 16;
    /** Extra strength per additional player nearby (upstream defaults). */
    private static final double PER_PLAYER = 0.25;
    private String boss = "";

    public BossSpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirAurorian.SPAWNER_BE.get(), pos, state);
    }

    void setBossFromEgg(ItemStack egg) {
        EntityType<?> t = ((SpawnEggItem) egg.getItem()).getType(egg);
        if (t != null) {
            boss = BuiltInRegistries.ENTITY_TYPE.getKey(t).toString();
            setChanged();
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BossSpawnerBlockEntity be) {
        if (be.boss.isEmpty() || !(level instanceof ServerLevel sl)) {
            return;
        }
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, RANGE, EntitySelector.NO_CREATIVE_OR_SPECTATOR) == null) {
            return;
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.parse(be.boss)).orElse(null);
        if (type == null) {
            return;
        }
        int players = level.getEntitiesOfClass(Player.class, new AABB(pos).inflate(RANGE * 2)).size();
        if (type.spawn(sl, pos.above(), EntitySpawnReason.STRUCTURE) instanceof Mob m && players > 1) {
            double k = 1 + (players - 1) * PER_PLAYER;
            m.getAttribute(Attributes.MAX_HEALTH).setBaseValue(m.getAttribute(Attributes.MAX_HEALTH).getBaseValue() * k);
            m.setHealth(m.getMaxHealth());
            m.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(m.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() * k);
        }
        level.destroyBlock(pos, false);
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("boss", boss);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        boss = in.getStringOr("boss", "");
    }
}
