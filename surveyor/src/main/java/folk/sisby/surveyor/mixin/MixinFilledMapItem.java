package folk.sisby.surveyor.mixin;

import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.SurveyorMapIntegration;
import folk.sisby.surveyor.config.SurveyorConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MapItem.class)
public class MixinFilledMapItem {
	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	private void addExplorationOnSneakUse(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
		MapItemSavedData mapState = MapItem.getSavedData(context.getItemInHand(), context.getLevel());
		if (mapState != null && context.getPlayer() instanceof ServerPlayer spe && spe.isShiftKeyDown()) {
			boolean didThing = false;
			BlockState state = context.getLevel().getBlockState(context.getClickedPos());
			if (Surveyor.CONFIG.builtins.recordFromMapItems && state.is(SurveyorMapIntegration.RECORD_FROM_MAP)) {
				SurveyorMapIntegration.recordMapData(spe, mapState);
				didThing = true;
			}
			if (!mapState.locked && Surveyor.CONFIG.builtins.recordToMapItems != SurveyorConfig.Builtins.RecordStyle.NONE && state.is(SurveyorMapIntegration.RECORD_TO_MAP)) {
				SurveyorMapIntegration.applyMapData(spe, mapState);
				didThing = true;
			}
			if (didThing) {
				context.getLevel().playSound(null, context.getPlayer(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, context.getPlayer().getSoundSource(), 1.0F, 0.7F);
				cir.setReturnValue(InteractionResult.SUCCESS);
			}
		}
	}
}
