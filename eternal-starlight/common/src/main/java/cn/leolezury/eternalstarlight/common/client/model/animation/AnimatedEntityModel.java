package cn.leolezury.eternalstarlight.common.client.model.animation;

import cn.leolezury.eternalstarlight.common.client.compat.ESModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public abstract class AnimatedEntityModel<E extends Entity> extends ESModel<E> implements AnimatedModel {
	protected AnimatedEntityModel(ModelPart root) {
		super(root);
	}

	protected AnimatedEntityModel(ModelPart root, Function<Identifier, RenderType> renderType) {
		super(root, renderType);
	}
}
