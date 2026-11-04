package folk.sisby.surveyor.mixin;

import com.mojang.authlib.GameProfile;
import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.ServerSummary;
import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.SurveyorPlayer;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.util.TextUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinServerPlayerEntity implements SurveyorPlayer {
	@Unique
	PlayerSummary.ServerPlayerEntitySummary surveyor$summary = null;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions, CallbackInfo ci) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		surveyor$summary = new PlayerSummary.ServerPlayerEntitySummary(self);
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	public void writeSurveyorData(ValueOutput view, CallbackInfo ci) {
		ServerPlayer self = (ServerPlayer) (Object) this;
		surveyor$summary.writeNbt(view);
		ServerSummary.of(((AccessServerPlayerEntity) self).getServer()).updatePlayer(Surveyor.getUuid(self), view, false);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readSurveyorData(ValueInput view, CallbackInfo ci) {
		surveyor$summary.read(view);
	}

	@Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;recheckStatus()V"))
	public void onDeath(DamageSource damageSource, CallbackInfo ci) {
		if (!Surveyor.CONFIG.builtins.playerDeathWaypoints) return;
		ServerPlayer self = (ServerPlayer) (Object) this;
		WorldLandmarks landmarks = WorldLandmarks.of(self.level());
		if (landmarks == null) return;
		landmarks.put(Landmark.createIncremental(landmarks, Surveyor.getUuid(self), Surveyor.id("grave"), builder -> builder
			.add(LandmarkComponentTypes.POS, self.blockPosition())
			.add(LandmarkComponentTypes.NAME, TextUtil.stripInteraction(self.getCombatTracker().getDeathMessage()))
			.add(LandmarkComponentTypes.TIME, self.level().getGameTime())
			.add(LandmarkComponentTypes.SEED, self.getRandom().nextInt())
		));
	}

	@Inject(method = "restoreFrom", at = @At("TAIL"))
	public void copyFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
		surveyor$summary.copyFrom(PlayerSummary.of(oldPlayer));
	}

	@Override
	public PlayerSummary surveyor$getSummary() {
		return surveyor$summary;
	}
}
