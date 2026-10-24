package com.zurrtum.create.platform;

import net.neoforged.fml.ModList;

public final class ModContainer {
    private final String id;

    ModContainer(String id) {
        this.id = id;
    }

    public ModMetadata getMetadata() {
        return new ModMetadata(id);
    }

    public static final class ModMetadata {
        private final String id;

        ModMetadata(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return ModList.get() == null ? id : ModList.get().getModContainerById(id.equals("create") ? "diaithir_create" : id).map(c -> c.getModInfo().getDisplayName()).orElse(id);
        }

        public Version getVersion() {
            String v = ModList.get() == null ? "0" : ModList.get().getModContainerById(id.equals("create") ? "diaithir_create" : id).map(c -> c.getModInfo().getVersion().toString()).orElse("0");
            return new Version(v);
        }
    }

    public static final class Version {
        private final String value;

        Version(String value) {
            this.value = value;
        }

        public String getFriendlyString() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
