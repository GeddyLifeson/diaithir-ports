package com.diaithir.addons.newage.content.motor.extension;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.world.entity.player.Player;

/** Server side multiplier setting of a motor extension; the value box and board are attached on the client. */
public class MotorExtensionScrollValueBehaviour extends ServerScrollValueBehaviour {
    protected int step;

    public MotorExtensionScrollValueBehaviour(SmartBlockEntity be, int step) {
        super(be);
        this.step = step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getStep() {
        return step;
    }

    public void setRaw(int value) {
        this.value = value;
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int v = Math.max(1, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(v * step);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, Math.abs(value / step));
    }

    public void betweenValidated(int min, int max) {
        this.between(min, max);

        if (value > max) {
            value = max;
        } else if (value < min) {
            value = min;
        }
    }

    @Override
    public String getClipboardKey() {
        return "Stress Multiplier";
    }
}
