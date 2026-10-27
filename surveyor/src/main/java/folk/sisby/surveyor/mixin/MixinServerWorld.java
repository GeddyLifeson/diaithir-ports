package folk.sisby.surveyor.mixin;

import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import java.util.Objects;
import java.util.Optional;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class MixinServerWorld {
	@Inject(method = "updatePOIOnBlockStateChange", at = @At("HEAD"))
	public void onPoiChange(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
		Optional<Holder<PoiType>> oldType = PoiTypes.forState(oldState);
		Optional<Holder<PoiType>> newType = PoiTypes.forState(newState);
		if (Objects.equals(oldType, newType)) return;
		BlockPos immutable = pos.immutable();
		ServerLevel self = (ServerLevel) (Object) this;
		self.getServer().execute(() -> {
			oldType.ifPresent(t -> onPointOfInterestRemoved(immutable, t));
			newType.ifPresent(t -> onPointOfInterestAdded(immutable, t));
		});
	}

	@Unique
	private void onPointOfInterestAdded(BlockPos blockPos, Holder<PoiType> poiType) {
		ServerLevel self = (ServerLevel) (Object) this;
		WorldLandmarks landmarks = WorldLandmarks.of(self);
		if (landmarks == null) return;
		if (poiType.unwrapKey().isEmpty() || !Surveyor.CONFIG.builtins.poiLandmarks.contains(poiType.unwrapKey().get().identifier().toString())) return;
		Identifier poi = poiType.unwrapKey().get().identifier();
		landmarks.put(Landmark.global(
			Identifier.fromNamespaceAndPath(poi.getNamespace(), "poi/%s/%s/%s/%s".formatted(poi.getPath(), blockPos.getX(), blockPos.getY(), blockPos.getZ())),
			builder -> LandmarkComponentTypes.forBlock(builder, self, blockPos)
		));
	}

	@Unique
	private void onPointOfInterestRemoved(BlockPos blockPos, Holder<PoiType> oldPoiType) {
		ServerLevel self = (ServerLevel) (Object) this;
		WorldLandmarks landmarks = WorldLandmarks.of(self);
		if (landmarks == null) return;
		landmarks.removeAll(l -> l.owner().equals(WorldLandmarks.GLOBAL)
			&& l.id().getPath().startsWith("poi")
			&& l.components().contains(LandmarkComponentTypes.POS)
			&& l.components().get(LandmarkComponentTypes.POS).equals(blockPos)
		);
	}
}
