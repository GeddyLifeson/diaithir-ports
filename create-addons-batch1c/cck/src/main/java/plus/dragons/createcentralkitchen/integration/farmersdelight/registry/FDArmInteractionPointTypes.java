package plus.dragons.createcentralkitchen.integration.farmersdelight.registry;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.Registry;
import plus.dragons.createcentralkitchen.common.CCKCommon;
import plus.dragons.createcentralkitchen.integration.farmersdelight.mechanicalArm.CookingPotArmInteractionPoint;
import plus.dragons.createcentralkitchen.integration.farmersdelight.mechanicalArm.CuttingBoardArmInteractionPoint;
import plus.dragons.createcentralkitchen.integration.farmersdelight.mechanicalArm.PortionableFoodArmInteractionPoint;
import plus.dragons.createcentralkitchen.integration.farmersdelight.mechanicalArm.SkilletArmInteractionPoint;
import plus.dragons.createcentralkitchen.integration.farmersdelight.mechanicalArm.StoveArmInteractionPoint;

public final class FDArmInteractionPointTypes {
    private FDArmInteractionPointTypes() {}

    public static void register() {
        add("cooking_pot", new CookingPotArmInteractionPoint.Type());
        add("cutting_board", new CuttingBoardArmInteractionPoint.Type());
        add("portionable_food", new PortionableFoodArmInteractionPoint.Type());
        add("skillet", new SkilletArmInteractionPoint.Type());
        add("stove", new StoveArmInteractionPoint.Type());
    }

    private static void add(String path, ArmInteractionPointType type) {
        Registry.register(CreateRegistries.ARM_INTERACTION_POINT_TYPE, CCKCommon.asResource(path), type);
    }
}
