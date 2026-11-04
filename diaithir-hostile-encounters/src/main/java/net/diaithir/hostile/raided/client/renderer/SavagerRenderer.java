package net.diaithir.hostile.raided.client.renderer;

import net.diaithir.hostile.raided.Raided;
import net.diaithir.hostile.raided.client.ClientHandler;
import net.diaithir.hostile.raided.client.model.SavagerModel;
import net.diaithir.hostile.raided.client.state.SavagerRenderState;
import net.diaithir.hostile.raided.entity.Savager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class SavagerRenderer extends MobRenderer<Savager, SavagerRenderState, SavagerModel> {
	private static final Identifier SAVAGER_LOCATION = Raided.modLoc("textures/entity/illager/savager.png");

	public SavagerRenderer(EntityRendererProvider.Context context) {
		super(context, new SavagerModel(context.bakeLayer(ClientHandler.SAVAGER)), 0.5F);
	}

	@Override
	public SavagerRenderState createRenderState() {
		return new SavagerRenderState();
	}

	@Override
	public void extractRenderState(Savager savager, SavagerRenderState renderState, float partialTicks) {
		super.extractRenderState(savager, renderState, partialTicks);
		renderState.tailAngle = savager.getTarget() != null ? 1.5393804F : (float) (Math.PI / 5);
	}

	@Override
	public Identifier getTextureLocation(SavagerRenderState renderState) {
		return SAVAGER_LOCATION;
	}
}