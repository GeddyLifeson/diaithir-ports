package net.diaithir.hostile.raided.client.renderer;

import net.diaithir.hostile.raided.Raided;
import net.diaithir.hostile.raided.client.ClientHandler;
import net.diaithir.hostile.raided.client.model.NecromancerModel;
import net.diaithir.hostile.raided.client.state.NecromancerRenderState;
import net.diaithir.hostile.raided.entity.Necromancer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.item.CrossbowItem;

public class NecromancerRenderer extends MobRenderer<Necromancer, NecromancerRenderState, NecromancerModel> {
	private static final Identifier TEXTURE = Raided.modLoc("textures/entity/illager/necromancer.png");

	public NecromancerRenderer(EntityRendererProvider.Context context) {
		super(context, new NecromancerModel(context.bakeLayer(ClientHandler.NECROMANCER)), 0.4F);
		this.addLayer(new ItemInHandLayer<>(this));
	}

	@Override
	public NecromancerRenderState createRenderState() {
		return new NecromancerRenderState();
	}

	@Override
	public void extractRenderState(Necromancer necromancer, NecromancerRenderState renderState, float partialTick) {
		super.extractRenderState(necromancer, renderState, partialTick);
		ArmedEntityRenderState.extractArmedEntityRenderState(necromancer, renderState, this.itemModelResolver, partialTick);
		renderState.isRiding = necromancer.isPassenger();
		renderState.mainArm = necromancer.getMainArm();
		renderState.armPose = necromancer.getArmPose();
		renderState.maxCrossbowChargeDuration = renderState.armPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE
				? CrossbowItem.getChargeDuration(necromancer.getUseItem(), necromancer)
				: 0;
		renderState.ticksUsingItem = necromancer.getTicksUsingItem();
		renderState.attackAnim = necromancer.getAttackAnim(partialTick);
		renderState.isAggressive = necromancer.isAggressive();
	}

	@Override
	public Identifier getTextureLocation(NecromancerRenderState renderState) {
		return TEXTURE;
	}
}
