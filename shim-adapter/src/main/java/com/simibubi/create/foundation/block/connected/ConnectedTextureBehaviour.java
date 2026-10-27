package com.simibubi.create.foundation.block.connected;

/** Inert CT behaviour API (CT rendering is client-only and not bridged yet); lets addon registration code run on any dist. */
public interface ConnectedTextureBehaviour {
    abstract class Base implements ConnectedTextureBehaviour {
        public Base() {}
        public boolean buildContextForOccludedDirections() { return false; }
    }
}
