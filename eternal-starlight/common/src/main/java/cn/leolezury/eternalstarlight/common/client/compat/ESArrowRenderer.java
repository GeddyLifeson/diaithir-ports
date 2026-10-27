package cn.leolezury.eternalstarlight.common.client.compat;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

public abstract class ESArrowRenderer<T extends AbstractArrow> extends ArrowRenderer<T, ESArrowRenderer.State> {
	public static class State extends ArrowRenderState {
		public AbstractArrow entity;
	}

	protected ESArrowRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public State createRenderState() {
		return new State();
	}

	@Override
	public void extractRenderState(T entity, State state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.entity = entity;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected final Identifier getTextureLocation(State state) {
		return getTextureLocation((T) state.entity);
	}

	public abstract Identifier getTextureLocation(T entity);
}
