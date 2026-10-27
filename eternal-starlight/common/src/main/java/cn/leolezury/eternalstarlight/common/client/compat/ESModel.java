package cn.leolezury.eternalstarlight.common.client.compat;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

/**
 * Bridges the entity based setupAnim signature of older versions onto the render state based API.
 */
public abstract class ESModel<T extends Entity> extends EntityModel<ESRenderState> {
	protected boolean young;
	protected boolean riding;
	protected float partialTick;

	protected ESModel(ModelPart root) {
		super(root);
	}

	protected ESModel(ModelPart root, Function<Identifier, RenderType> renderType) {
		super(root, renderType);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setupAnim(ESRenderState state) {
		super.setupAnim(state);
		this.young = state.isBaby;
		this.partialTick = state.partialTicks;
		this.riding = state.entity != null && state.entity.isPassenger();
		if (state.entity != null) {
			prepareMobModel((T) state.entity, state.walkAnimationPos, state.walkAnimationSpeed, state.partialTicks);
			setupAnim((T) state.entity, state.walkAnimationPos, state.walkAnimationSpeed, state.ageInTicks, state.yRot, state.xRot);
		}
	}

	public void copyPropertiesTo(ESModel<?> other) {
		other.young = this.young;
		other.riding = this.riding;
	}

	public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
	}

	public abstract void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);
}
