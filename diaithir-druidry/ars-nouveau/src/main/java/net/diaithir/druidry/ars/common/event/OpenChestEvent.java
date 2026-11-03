package net.diaithir.druidry.ars.common.event;

import net.diaithir.druidry.ars.api.ANFakePlayer;
import net.diaithir.druidry.ars.api.event.ITimedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.common.util.FakePlayer;

public class OpenChestEvent implements ITimedEvent {
    ServerLevel level;
    public int duration;
    public BlockPos pos;
    FakePlayer fakePlayer;
    private int containerCounter = 0;

    public OpenChestEvent(ServerLevel level, BlockPos pos, int duration) {
        this.duration = duration;
        this.level = level;
        this.pos = pos;
        fakePlayer = ANFakePlayer.getPlayer(level);
    }

    public void open() {
        try {
            if (level.getBlockEntity(pos) instanceof ChestBlockEntity chestBlockEntity) {
                containerCounter = containerCounter % 100 + 1;
                fakePlayer.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                fakePlayer.containerMenu = chestBlockEntity.createMenu(containerCounter, fakePlayer.getInventory(), fakePlayer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void attemptClose() {
        try {
            if (level.getBlockEntity(pos) instanceof ChestBlockEntity) {
                fakePlayer.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                fakePlayer.containerMenu = fakePlayer.inventoryMenu;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tick(boolean serverSide) {
        duration--;
        if (duration <= 0) {
            attemptClose();
        }
    }

    @Override
    public boolean isExpired() {
        return duration <= 0;
    }
}
