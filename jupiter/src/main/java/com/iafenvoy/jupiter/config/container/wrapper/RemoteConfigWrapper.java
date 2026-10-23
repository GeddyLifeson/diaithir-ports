package com.iafenvoy.jupiter.config.container.wrapper;

import com.iafenvoy.jupiter.JupiterProxies;
import com.iafenvoy.jupiter.config.ConfigGroup;
import com.iafenvoy.jupiter.config.container.AbstractConfigContainer;
import com.iafenvoy.jupiter.network.payload.ConfigSyncPayload;

public class RemoteConfigWrapper extends AbstractConfigContainer {
    public RemoteConfigWrapper(AbstractConfigContainer parent) {
        super(parent.getConfigId(), parent.getTitle());
        this.configTabs.addAll(parent.getConfigTabs().stream().map(ConfigGroup::copy).toList());
    }

    @Override
    public String getPath() {
        return "Virtual Config File";
    }

    @Override
    public void init() {
    }

    @Override
    public void load() {
    }

    @Override
    public void save() {
        JupiterProxies.CLIENT_NETWORKING.sendToServer(new ConfigSyncPayload(this.getConfigId(), this.serializeNbt()));
    }
}
