package net.diaithir.bosses;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Remembers which boss the altar wakes, when it was last beaten and how often; applies the profile's respawn rule. */
public class AltarBlockEntity extends BlockEntity {
    public String boss = "";
    public long defeatedAt = -1;
    public int defeats;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(DiaithirBosses.ALTAR_BE.get(), pos, state);
    }

    public static String posTag(BlockPos p) { return BossController.T_ALTAR + p.getX() + "," + p.getY() + "," + p.getZ(); }

    /** Why the altar will not wake right now, or null if it will. */
    public Component refusal(ServerLevel sl, Data.Profile p) {
        if (sl.getDifficulty() == Difficulty.PEACEFUL) return Component.translatable("boss.diaithirbosses.peaceful");
        int age = Ages.current(sl.getServer());
        if (age < p.age) return Component.translatable("boss.diaithirbosses.dormant", Ages.name(p.age));
        String tag = posTag(worldPosition);
        if (!sl.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(worldPosition).inflate(96), m -> m.isAlive() && m.entityTags().contains(tag)).isEmpty())
            return Component.translatable("boss.diaithirbosses.present");
        if (defeats > 0 && "once".equals(p.respawn.mode)) return Component.translatable("boss.diaithirbosses.spent");
        if (defeatedAt >= 0 && "cooldown".equals(p.respawn.mode) && sl.getGameTime() - defeatedAt < p.respawn.ticks)
            return Component.translatable("boss.diaithirbosses.cooldown", (p.respawn.ticks - (sl.getGameTime() - defeatedAt)) / 1200 + 1);
        return null;
    }

    public void activate(Player player) {
        if (!(level instanceof ServerLevel sl)) return;
        Data.Profile p = Data.byId(boss);
        if (p == null) return;
        Component why = refusal(sl, p);
        if (why != null) {
            player.sendSystemMessage(why);
            return;
        }
        Mob m = DiaithirBosses.spawn(sl, p, net.minecraft.world.phys.Vec3.atBottomCenterOf(worldPosition.above(2)), worldPosition);
        if (m != null) player.sendSystemMessage(Component.translatable("boss.diaithirbosses.woken"));
    }

    public void defeated(long time) {
        defeatedAt = time;
        defeats++;
        setChanged();
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.putString("boss", boss);
        out.putLong("defeated_at", defeatedAt);
        out.putInt("defeats", defeats);
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        boss = in.getStringOr("boss", "");
        defeatedAt = in.getLongOr("defeated_at", -1L);
        defeats = in.getIntOr("defeats", 0);
    }
}
