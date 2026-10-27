package com.diaithir.addons.newage.content;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;

/** Scroll value that can be preset without firing its callback. */
public class RawScrollValue extends ServerScrollValueBehaviour {
    public RawScrollValue(SmartBlockEntity be) {
        super(be);
    }

    public void setRaw(int value) {
        this.value = value;
    }
}
