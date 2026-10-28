package folk.sisby.surveyor.mixin;

import folk.sisby.surveyor.SurveyorExploration;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerChunkSender.class)
public class MixinChunkDataSender {
	@Inject(method = "sendChunk", at = @At("HEAD"))
	private static void sendChunkData(ServerGamePacketListenerImpl handler, ServerLevel world, LevelChunk chunk, CallbackInfo ci) {
		SurveyorExploration.of(handler.getPlayer()).addChunk(chunk.getLevel().dimension(), chunk.getPos(), false);
	}
}
