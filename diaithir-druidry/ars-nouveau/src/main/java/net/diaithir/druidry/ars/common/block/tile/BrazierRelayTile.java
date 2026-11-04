package net.diaithir.druidry.ars.common.block.tile;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.diaithir.druidry.ars.api.item.inv.InventoryManager;
import net.diaithir.druidry.ars.api.util.BlockUtil;
import net.diaithir.druidry.ars.client.particle.GlowParticleData;
import net.diaithir.druidry.ars.client.particle.ParticleColor;
import net.diaithir.druidry.ars.client.particle.ParticleUtil;
import net.diaithir.druidry.ars.setup.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.diaithir.druidry.ars.common.block.RitualBrazierBlock.LIT;

public class BrazierRelayTile extends RitualBrazierTile{

    int ticksToLightOff = 0;
    public BlockPos brazierPos;

    private static List<BlockPos> relayingTraversed = new ArrayList<>();

    public BrazierRelayTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public BrazierRelayTile(BlockPos p, BlockState s) {
        super(BlockRegistry.BRAZIER_RELAY_TILE.get(), p, s);
    }

    @Override
    public void tick() {
        if (isDecorative && level.isClientSide()) {
            makeParticle(color.transition((int) level.getGameTime() * 10), color.transition((int) level.getGameTime() * 10), 5);
        }

        if(!level.isClientSide()){
            ticksToLightOff--;
            if(ticksToLightOff <= 0){
                ticksToLightOff = 0;
                if(!this.isDecorative && level.getBlockState(worldPosition).getValue(LIT)) {
                    level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(LIT, false));
                }
            }
            if( !level.getBlockState(worldPosition).getValue(LIT) && ticksToLightOff > 0){
                level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition).setValue(LIT, true));
            }
        }
    }

    @Override
    public void makeParticle(ParticleColor centerColor, ParticleColor outerColor, int intensity) {
        Level world = getLevel();
        BlockPos pos = getBlockPos();
        double xzOffset = 0.25;
        for (int i = 0; i < intensity; i++) {
            world.addParticle(
                    GlowParticleData.createData(centerColor.transition((int) level.getGameTime() * 20)),
                    pos.getX() + 0.5 + ParticleUtil.inRange(-xzOffset / 2, xzOffset / 2), pos.getY() + 0.2 + ParticleUtil.inRange(-0.05, 0.2), pos.getZ() + 0.5 + ParticleUtil.inRange(-xzOffset / 2, xzOffset / 2),
                    0, ParticleUtil.inRange(0.0, 0.05f), 0);
        }
        for (int i = 0; i < intensity; i++) {
            world.addParticle(
                    GlowParticleData.createData(outerColor.transition((int) level.getGameTime() * 20)),
                    pos.getX() + 0.5 + ParticleUtil.inRange(-xzOffset, xzOffset), pos.getY() + 0.2 + ParticleUtil.inRange(0, 0.7), pos.getZ() + 0.5 + ParticleUtil.inRange(-xzOffset, xzOffset),
                    0, ParticleUtil.inRange(0.0, 0.05f), 0);
        }
    }

    @Override
    public void onFinishedConnectionFirst(@Nullable BlockPos storedPos, @Nullable LivingEntity storedEntity, Player playerEntity) {

    }

    @Override
    public void onFinishedConnectionLast(@Nullable BlockPos storedPos, @Nullable LivingEntity storedEntity, Player playerEntity) {
        if (storedPos != null && level != null && level.getBlockEntity(storedPos) instanceof RitualBrazierTile brazierTile) {
            if (BlockUtil.distanceFrom(getBlockPos(), storedPos) > 16) {
                return;
            }
            brazierPos = storedPos;
            updateBlock();
        }
    }

    @Override
    public void onWanded(Player playerEntity) {

    }


    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        tag.putInt("ticksToLightOff", ticksToLightOff);
        if (this.brazierPos != null) {
            tag.putLong("brazierPos", this.brazierPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        this.ticksToLightOff = tag.getIntOr("ticksToLightOff", 0);
        if (tag.read("brazierPos", com.mojang.serialization.Codec.PASSTHROUGH).isPresent()) {
            this.brazierPos = BlockPos.of(tag.getLongOr("brazierPos", 0L));
        }
    }

    @Override
    public InventoryManager getInventoryManager() {
        if (this.brazierPos != null && level != null && relayingTraversed.size() < 256 && !relayingTraversed.contains(brazierPos) && level.isLoaded(this.brazierPos) && level.getBlockEntity(this.brazierPos) instanceof RitualBrazierTile brazierTile) {
            relayingTraversed.add(brazierPos);
            InventoryManager brazierInv = brazierTile.getInventoryManager();
            if (!brazierInv.getInventory().isEmpty()) {
                relayingTraversed.clear();
                return brazierInv;
            }
        }
        relayingTraversed.clear();
        return super.getInventoryManager();
    }
}
