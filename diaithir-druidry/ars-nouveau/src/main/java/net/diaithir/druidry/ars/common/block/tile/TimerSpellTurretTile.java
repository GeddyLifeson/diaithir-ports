package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.api.item.IWandable;
import net.diaithir.druidry.ars.api.spell.Spell;
import net.diaithir.druidry.ars.common.spell.effect.EffectRedstone;
import net.diaithir.druidry.ars.common.spell.method.MethodProjectile;
import net.diaithir.druidry.ars.common.spell.method.MethodTouch;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TimerSpellTurretTile extends BasicSpellTurretTile implements IWandable {

    private int ticksPerSignal = 20;
    public boolean isLocked;
    public boolean isOff;
    public int ticksElapsed;

    public TimerSpellTurretTile(BlockEntityType<?> p_i48289_1_, BlockPos pos, BlockState state) {
        super(p_i48289_1_, pos, state);
    }

    public TimerSpellTurretTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.TIMER_SPELL_TURRET_TILE.get(), pos, state);
    }

    @Override
    public void tick() {
        ticksElapsed++;
        if(!level.isClientSide() && ticksPerSignal > 0 && !isOff && ticksElapsed >= ticksPerSignal){
            getBlockState().tick((ServerLevel) level, getBlockPos(), getLevel().getRandom());
            ticksElapsed = 0;
        }
    }

    @Override
    public int getManaCost() {
        int cost = super.getManaCost();
        Spell spell = this.spellCaster.getSpell();
        cost -= spell.getInstanceCount(MethodTouch.INSTANCE) * MethodTouch.INSTANCE.getCastingCost();
        cost -= spell.getInstanceCount(EffectRedstone.INSTANCE) * EffectRedstone.INSTANCE.getCastingCost();
        cost -= spell.getInstanceCount(MethodProjectile.INSTANCE) * MethodProjectile.INSTANCE.getCastingCost();
        return Math.max(0, cost);
    }






    @Override
    public void onWanded(Player playerEntity) {
        this.isLocked = !isLocked;
        updateBlock();
    }

    public void addTime(int ticks) {
        ticksPerSignal += ticks;
        ticksPerSignal = Math.max(0, ticksPerSignal);
        ticksElapsed = 0;
        updateBlock();
    }

    @Override
    public void getTooltip(List<Component> tooltip) {
        super.getTooltip(tooltip);
        if (ticksPerSignal <= 0 && !isOff) {
            tooltip.add(Component.translatable("ars_nouveau.tooltip.turned_off"));
        } else {
            tooltip.add(Component.translatable("ars_nouveau.seconds", ticksPerSignal / 20));
        }
        if (isOff)
            tooltip.add(Component.translatable("ars_nouveau.tooltip.turned_off"));
        if (isLocked)
            tooltip.add(Component.translatable("ars_nouveau.locked"));
    }


    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.isLocked = tag.getBooleanOr("locked", false);
        this.ticksPerSignal = tag.getIntOr("time", 0);
        this.isOff = tag.getBooleanOr("off", false);
        this.ticksElapsed = tag.getIntOr("ticksElapsed", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putBoolean("locked", isLocked);
        tag.putInt("time", ticksPerSignal);
        tag.putBoolean("off", isOff);
        tag.putInt("ticksElapsed", ticksElapsed);
    }
}
