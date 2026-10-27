package folk.sisby.surveyor.packet;

import com.google.common.collect.Multimap;
import folk.sisby.surveyor.Surveyor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Map;

public record C2SKnownStructuresPacket(Map<ResourceKey<Level>, Multimap<ResourceKey<Structure>, ChunkPos>> starts) implements C2SPacket {
	public static final Type<C2SKnownStructuresPacket> ID = new Type<>(Surveyor.id("c2s_known_structures"));
	public static final StreamCodec<RegistryFriendlyByteBuf, C2SKnownStructuresPacket> CODEC = SurveyorPacketCodecs.STRUCTURE_KEYS.map(C2SKnownStructuresPacket::new, C2SKnownStructuresPacket::starts);

	@Override
	public Type<C2SKnownStructuresPacket> type() {
		return ID;
	}
}
