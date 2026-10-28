package me.desht.pneumaticcraft.common.villages;

/**
 * 26.1 port: villager trades are now data driven; the old trade registration is stashed in port-stash/ (TODO).
 */
public class VillagerTradesRegistration {
    public enum WhichTrades {
        NONE,
        PCB_BLUEPRINT,
        ALL;

        boolean shouldAddBlueprint() {
            return this != NONE;
        }
    }
}
