package com.diaithir.addons.newage.content.motor;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerKineticScrollValueBehaviour;

/** Server side speed setting of a motor; the wrench value box is attached on the client. */
public class MotorScrollValueBehaviour extends ServerKineticScrollValueBehaviour {

    public MotorScrollValueBehaviour(SmartBlockEntity be) {
        super(be);
    }

    /** Sets the value without notifying the callback (used while building the entity). */
    public void setRaw(int value) {
        this.value = value;
    }

    public void betweenValidated(int min, int max) {
        this.between(min, max);

        if (value > max) {
            value = max;
        } else if (value < min) {
            value = min;
        }
    }
}
