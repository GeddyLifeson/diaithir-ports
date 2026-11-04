package net.diaithir.hostile.raided.registry;

import net.diaithir.hostile.raided.entity.Electromancer;
import net.diaithir.hostile.raided.entity.Incinerator;
import net.diaithir.hostile.raided.entity.Inquisitor;
import net.diaithir.hostile.raided.entity.Necromancer;
import net.diaithir.hostile.raided.entity.Savager;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class RaidedSetup {
	public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
		event.put(RaidedRegistry.INQUISITOR.getEntityType(), Inquisitor.createAttributes().build());
		event.put(RaidedRegistry.INCINERATOR.getEntityType(), Incinerator.createAttributes().build());
		event.put(RaidedRegistry.SAVAGER.getEntityType(), Savager.createAttributes().build());
		event.put(RaidedRegistry.NECROMANCER.getEntityType(), Necromancer.createAttributes().build());
		event.put(RaidedRegistry.ELECTROMANCER.getEntityType(), Electromancer.createAttributes().build());
	}
}
