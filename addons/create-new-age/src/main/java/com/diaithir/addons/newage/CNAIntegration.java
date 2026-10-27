package com.diaithir.addons.newage;

import com.zurrtum.create.api.boiler.BoilerHeater;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import com.zurrtum.create.content.decoration.encasing.EncasingRegistry;
import com.diaithir.addons.newage.content.electricity.generation.magnet.MagnetPlacementHelper;
import com.diaithir.addons.newage.content.heat.heater.HeaterBlock;

/** Hooks into the Create port that need the registries to be frozen. */
public class CNAIntegration {
    private static int magnetPlacementHelperId = -1;

    public static int magnetPlacementHelperId() {
        return magnetPlacementHelperId;
    }

    public static void commonSetup() {
        magnetPlacementHelperId = PlacementHelpers.register(new MagnetPlacementHelper());
        BoilerHeater.REGISTRY.register(CNABlocks.HEATER.get(),
                (level, pos, state) -> state.getValue(HeaterBlock.STRENGTH).ordinal() - 1);
        EncasingRegistry.addVariant(CNABlocks.HEAT_PIPE.get(), CNABlocks.ENCASED_HEAT_PIPE.get());
        EncasingRegistry.addVariant(CNABlocks.HEAT_PIPE.get(), CNABlocks.REACTOR_ENCASED_HEAT_PIPE.get());
    }
}
