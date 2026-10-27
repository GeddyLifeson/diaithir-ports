package folk.sisby.surveyor.packet;

import folk.sisby.surveyor.SurveyorNetworking;
import net.minecraft.core.RegistryAccess;

public interface C2SPacket extends SurveyorPacket {
	default void send(RegistryAccess registryManager) {
		SurveyorNetworking.C2S_SENDER.accept(registryManager, this);
	}
}
