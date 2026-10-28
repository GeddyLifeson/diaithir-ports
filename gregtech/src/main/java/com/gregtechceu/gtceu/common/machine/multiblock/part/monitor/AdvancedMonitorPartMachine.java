package com.gregtechceu.gtceu.common.machine.multiblock.part.monitor;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AdvancedMonitorPartMachine extends MonitorPartMachine {

    @Getter
    @SaveField
    private double clickPosX;
    @Getter
    @SaveField
    private double clickPosY;
    @Getter
    @SaveField
    private boolean clicked;
    @SaveField
    private boolean resetClickedNextTick = false;

    @Nullable
    private TickableSubscription clickResetSubscription;

    public AdvancedMonitorPartMachine(BlockEntityCreationInfo info) {
        super(info);
    }

    @Override
    public InteractionResult onUse(ExtendedUseOnContext context) { return null; /* ported stub */ }

    public void resetClicked() {
        resetClickedNextTick = true;
    }

    private void unsetClicked() {
        if (resetClickedNextTick) {
            clicked = false;
        }
        resetClickedNextTick = false;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        clickResetSubscription = subscribeServerTick(this::unsetClicked);
    }

    @Override
    public void onUnload() {
        unsubscribe(clickResetSubscription);
    }
}
