package com.iafenvoy.jupiter;

public interface Platform {
    String resolveModName(String id);

    boolean isModLoaded(String id);
}
