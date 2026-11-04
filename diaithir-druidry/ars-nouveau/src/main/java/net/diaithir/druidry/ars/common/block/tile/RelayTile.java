package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.api.client.ITooltipProvider;
import net.diaithir.druidry.ars.api.item.IWandable;
import net.diaithir.druidry.ars.api.source.AbstractSourceMachine;
import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.api.util.NBTUtil;
import net.diaithir.druidry.ars.client.particle.ColorPos;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.common.block.ITickable;
import net.diaithir.druidry.ars.common.items.DominionWand;
import net.diaithir.druidry.ars.common.util.PortUtil;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class RelayTile extends AbstractSourceMachine implements ITooltipProvider, IWandable, ITickable {

    public RelayTile(BlockPos pos, BlockState state) {
        super(BlockRegistry.ARCANE_RELAY_TILE.get(), pos, state);
    }

    public RelayTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public BlockPos getToPos() {
        return toPos;
    }

    public void setToPos(BlockPos toPos) {
        this.toPos = toPos;
    }

    public BlockPos getFromPos() {
        return fromPos;
    }

    public void setFromPos(BlockPos fromPos) {
        this.fromPos = fromPos;
    }

    private BlockPos toPos;
    private BlockPos fromPos;
    public boolean disabled;

    public boolean setTakeFrom(BlockPos pos) {
        if (BlockUtil.distanceFrom(pos, this.worldPosition) > getMaxDistance() || pos.equals(getBlockPos())) {
            return false;
        }
        this.fromPos = pos;
        updateBlock();
        return true;
    }

    public boolean setSendTo(BlockPos pos) {
        if (BlockUtil.distanceFrom(pos, this.worldPosition) > getMaxDistance() || pos.equals(getBlockPos()) || !(level.getBlockEntity(pos) instanceof AbstractSourceMachine)) {
            return false;
        }
        this.toPos = pos;
        updateBlock();
        return true;
    }

    public int getMaxDistance() {
        return 30;
    }

    public void clearPos() {
        this.toPos = null;
        this.fromPos = null;
        updateBlock();
    }

    @Override
    public int getTransferRate() {
        return 1000;
    }

    @Override
    public int getMaxSource() {
        return 1000;
    }

    public boolean closeEnough(BlockPos pos) {
        return BlockUtil.distanceFrom(pos, this.worldPosition) <= getMaxDistance() && !pos.equals(getBlockPos());
    }

    @Override
    public void onFinishedConnectionFirst(@Nullable BlockPos storedPos, @Nullable LivingEntity storedEntity, Player playerEntity) {
        if (storedPos == null || level.isClientSide() || storedPos.equals(getBlockPos()) || !(level.getBlockEntity(storedPos) instanceof AbstractSourceMachine))
            return;
        // Let relays take from us, no action needed.
        if (this.setSendTo(storedPos.immutable())) {
            PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.connections.send", DominionWand.getPosString(storedPos)));
            ParticleUtil.beam(storedPos, worldPosition, level);
        } else {
            PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.connections.fail"));
        }
    }

    @Override
    public void onFinishedConnectionLast(@Nullable BlockPos storedPos, @Nullable LivingEntity storedEntity, Player playerEntity) {
        if (storedPos == null || storedPos.equals(getBlockPos()) || level.getBlockEntity(storedPos) instanceof RelayTile || !(level.getBlockEntity(storedPos) instanceof AbstractSourceMachine))
            return;

        if (this.setTakeFrom(storedPos.immutable())) {
            PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.connections.take", DominionWand.getPosString(storedPos)));
        } else {
            PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.connections.fail"));
        }
    }

    @Override
    public void onWanded(Player playerEntity) {
        this.clearPos();
        PortUtil.sendMessage(playerEntity, Component.translatable("ars_nouveau.connections.cleared"));
    }

    @Override
    public List<ColorPos> getWandHighlight(List<ColorPos> list) {
        if (toPos != null) {
            list.add(ColorPos.centered(toPos, ParticleColor.TO_HIGHLIGHT));
        }
        if(fromPos != null){
            list.add(ColorPos.centered(fromPos, ParticleColor.FROM_HIGHLIGHT));
        }
        return list;
    }


    @Override
    public void tick() {
        if (level.isClientSide() || disabled) {
            return;
        }
        if (level.getGameTime() % 20 != 0)
            return;

        if (fromPos != null && level.isLoaded(fromPos)) {
            // Block has been removed
            if (!(level.getBlockEntity(fromPos) instanceof AbstractSourceMachine)) {
                fromPos = null;
                updateBlock();
                return;
            } else if (level.getBlockEntity(fromPos) instanceof AbstractSourceMachine fromTile) {
                // Transfer mana fromPos to this
                if (transferSource(fromTile, this) > 0) {
                    updateBlock();
                    ParticleUtil.spawnFollowProjectile(level, fromPos, worldPosition, this.getColor());
                }
            }
        }

        if (toPos != null && level.isLoaded(toPos)) {
            if (!(level.getBlockEntity(toPos) instanceof AbstractSourceMachine)) {
                toPos = null;
                updateBlock();
                return;
            }
            AbstractSourceMachine toTile = (AbstractSourceMachine) this.level.getBlockEntity(toPos);
            if (transferSource(this, toTile) > 0) {
                ParticleUtil.spawnFollowProjectile(level, worldPosition, toPos, this.getColor());
            }
        }
    }

    String TO = "to_";
    String FROM = "from";

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.toPos = null;
        this.fromPos = null;
        
        if (tag.read(TO, net.minecraft.core.BlockPos.CODEC).isPresent()) {
            this.toPos = tag.read(TO, net.minecraft.core.BlockPos.CODEC).orElse(net.minecraft.core.BlockPos.ZERO);
        }
        if (tag.read(FROM, net.minecraft.core.BlockPos.CODEC).isPresent()) {
            this.fromPos = tag.read(FROM, net.minecraft.core.BlockPos.CODEC).orElse(net.minecraft.core.BlockPos.ZERO);
        }
        this.disabled = tag.getBooleanOr("disabled", false);
    }

    @Override
    public void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (toPos != null) {
            tag.store(TO, net.minecraft.core.BlockPos.CODEC, toPos.immutable());
        } else {
            tag.discard(TO);
        }

        if (fromPos != null) {
            tag.store(FROM, net.minecraft.core.BlockPos.CODEC, fromPos.immutable());
        } else {
            tag.discard(FROM);
        }
        tag.putBoolean("disabled", disabled);
    }

    @Override
    public void getTooltip(List<Component> tooltip) {
        if (toPos == null) {
            tooltip.add(Component.translatable("ars_nouveau.relay.no_to"));
        } else {
            tooltip.add(Component.translatable("ars_nouveau.relay.one_to", 1));
        }
        if (fromPos == null) {
            tooltip.add(Component.translatable("ars_nouveau.relay.no_from"));
        } else {
            tooltip.add(Component.translatable("ars_nouveau.relay.one_from", 1));
        }

        if (disabled) {
            tooltip.add(Component.translatable("ars_nouveau.tooltip.turned_off"));
        }
    }








}
