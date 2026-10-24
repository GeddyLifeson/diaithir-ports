package com.zurrtum.create.client.vanillin;

import com.zurrtum.create.platform.FabricLoader;

public class VanillinXplatImpl implements VanillinXplat {
    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
