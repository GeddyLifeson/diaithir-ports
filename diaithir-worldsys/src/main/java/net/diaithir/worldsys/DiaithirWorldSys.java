package net.diaithir.worldsys;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/** World interaction, villagers, admin, quests, skills, accessories and tuning, one module. Clean-room. */
@Mod(DiaithirWorldSys.ID)
public final class DiaithirWorldSys {
    public static final String ID = "diaithirworldsys";

    public DiaithirWorldSys(IEventBus bus, net.neoforged.fml.ModContainer container) {
        net.diaithir.worldsys.alternatecurrent.Feature.init(bus, container);
        net.diaithir.structurenavigation.structurecompass.Feature.init(bus, container);
        net.diaithir.worldsys.admincommands.AdminCommandsFeature.register(bus);
        net.diaithir.worldsys.economy.EconomyFeature.register(bus);
        net.diaithir.worldsys.aircrafttuning.AircraftTuningFeature.register(bus);
        net.diaithir.worldsys.calmpiglins.CalmPiglinsFeature.register(bus);
        net.diaithir.worldsys.gemrings.GemRingsFeature.register(bus);
        net.diaithir.worldsys.gridkeys.GridKeysFeature.register(bus);
        net.diaithir.worldsys.itemmagnets.ItemMagnetsFeature.register(bus);
        net.diaithir.worldsys.mobbraintuning.MobBraintuningFeature.register(bus);
        net.diaithir.worldsys.packversioncheck.PackVersionCheckFeature.register(bus);
        net.diaithir.worldsys.pocketbench.PocketbenchFeature.register(bus);
        net.diaithir.worldsys.portallinkfix.PortalLinkFixFeature.register(bus);
        net.diaithir.worldsys.questbook.QuestBookFeature.register(bus);
        net.diaithir.worldsys.rankperms.RankPermsFeature.register(bus);
        net.diaithir.worldsys.skilltrees.SkillTreesFeature.register(bus);
        net.diaithir.worldsys.structurecompass.StructureCompassFeature.register(bus);
        net.diaithir.worldsys.utilityblocks.UtilityBlocksFeature.register(bus);
        net.diaithir.worldsys.vehicletuning.VehicleTuningFeature.register(bus);
        net.diaithir.worldsys.veinbreaker.VeinBreakerFeature.register(bus);
        net.diaithir.worldsys.villageguards.VillageGuardsFeature.register(bus);
        net.diaithir.worldsys.villagercare.VillagerCareFeature.register(bus);
        net.diaithir.worldsys.worldqol.WorldQolFeature.register(bus);
        net.diaithir.worldsys.legacy.LegacyFeature.register(bus);
        net.diaithir.worldsys.villagercare2.Villagercare2Feature.register(bus);
        net.diaithir.worldsys.stationsticks.StationsticksFeature.register(bus);
        net.diaithir.worldsys.jewelrypowers.JewelrypowersFeature.register(bus);
        net.diaithir.worldsys.villagercare3.Villagercare3Feature.register(bus);
    }
}
