/*
 * This file is part of pnc-repressurized.
 *
 *     pnc-repressurized is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with pnc-repressurized.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.common.thirdparty;

import me.desht.pneumaticcraft.common.config.subconfig.ThirdPartyConfig;
import me.desht.pneumaticcraft.lib.Log;
import me.desht.pneumaticcraft.lib.ModIds;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;

import java.util.*;
import java.util.function.Supplier;

public enum ThirdPartyManager {
    INSTANCE;

    private static final GenericIntegrationHandler GENERIC = new GenericIntegrationHandler();

    // for those mods which initialise implicitly (generally annotation-based)
    private static final IThirdParty IMPLICIT_INIT = new IThirdParty() {};

    private final List<IThirdParty> thirdPartyMods = new ArrayList<>();
    private IDocsProvider docsProvider = IDocsProvider.None.INSTANCE;
    private ITemperatureProvider temperatureProvider = ITemperatureProvider.None.INSTANCE;
    private final Map<ModType, List<IThirdParty>> loadedModTypes = new EnumMap<>(ModType.class);

    public static ThirdPartyManager instance() {
        return INSTANCE;
    }

    public IDocsProvider getDocsProvider() {
        return docsProvider;
    }

    public ITemperatureProvider getTemperatureProvider() {
        return temperatureProvider;
    }

    private void discoverMods() {
        Map<String, Supplier<? extends IThirdParty>> thirdPartyClasses = new HashMap<>();
        try {
            // all third party integrations (JEI, CC, Curios, Create, Mekanism, ...) were removed in the 26.1.2 port; see PORT_NOTES.md
        } catch (Throwable e) {
            Log.error("A class loader loaded a class where we didn't expect it to do so! Please report, as third party content is broken.");
            e.printStackTrace();
        }

        ThirdPartyConfig.setupDefaults(thirdPartyClasses.keySet());

        List<String> modNames = new ArrayList<>();
        for (Map.Entry<String, Supplier<? extends IThirdParty>> entry : thirdPartyClasses.entrySet()) {
            if (ThirdPartyConfig.isEnabled(entry.getKey()) && ModList.get().isLoaded(entry.getKey())) {
                IThirdParty mod = entry.getValue().get();
                thirdPartyMods.add(mod);
                if (mod.modType() != null) {
                    loadedModTypes.computeIfAbsent(mod.modType(), k -> new ArrayList<>()).add(mod);
                }
                // TODO: priority system or selectable in config?  right now, last provider found wins
                switch (mod) {
                    case IDocsProvider doc -> docsProvider = doc;
                    case ITemperatureProvider temp -> temperatureProvider = temp;
                    default -> {}
                }
                modNames.add(entry.getKey());
            }
        }

        Log.info("Third party integration activated for [{}]", String.join(",", modNames));
    }

    public void preInit(IEventBus modBus) {
        discoverMods();

        GENERIC.preInit(modBus);
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.preInit(modBus);
            } catch (Throwable e) {
                logError(e, thirdParty.getClass(), "PreInit");
            }
        }
    }

    public void init() {
        GENERIC.init();
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.init();
            } catch (Throwable e) {
                logError(e, thirdParty.getClass(), "Init");
            }
        }
    }

    public void postInit() {
        GENERIC.postInit();
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.postInit();
            } catch (Throwable e) {
                logError(e, thirdParty.getClass(), "PostInit");
            }
        }
    }

    public void clientPreInit(IEventBus modBus) {
        GENERIC.clientPreInit(modBus);
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.clientPreInit(modBus);
            } catch (Throwable e) {
                logError(e, thirdParty.getClass(), "PreInit");
            }
        }
    }

    public void clientInit() {
        GENERIC.clientInit();
        for (IThirdParty thirdParty : thirdPartyMods) {
            try {
                thirdParty.clientInit();
            } catch (Throwable e) {
                logError(e, thirdParty.getClass(), "Client Init");
            }
        }
    }

    private void logError(Throwable e, Class<?> cls, String when) {
        Log.error("Third party integration error: class: {}, phase: {}", cls.getName(), when);
        e.printStackTrace();
    }

    public boolean isModTypeLoaded(ModType modType) {
        return loadedModTypes.containsKey(modType);
    }

    /**
     * Collection of mod types; categories of mod which can be used to control whether blocks/items etc. should be
     * made visible to players (blocks/items are *always* registered, but might need to be hidden if their functionality
     * doesn't make sense without certain other mods present - e.g. the Drone Interface needs a computer mod loaded)
     */
    public enum ModType {
        COMPUTER,
        DOCUMENTATION,
        TEMPERATURE
    }
}
