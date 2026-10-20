package untamedwilds.citadel.client.model.basic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

// Three eras of Model/EntityModel, kept as SIBLING branches rather than nested ones: commenting
// out a branch that itself contains a commented-out branch would close the block comment early.
//
//   <1.21     renderToBuffer takes four float colour channels — the vanilla signature.
//   1.21+     it takes one packed ARGB int; the float form stays this hierarchy's hook and the
//             vanilla entry point unpacks into it.
//   1.21.2+   EntityModel's type parameter became a render state, so the entity-parameterised base
//             moved to client.render.compat.EntityModel, which carries setupAnim/prepareMobModel
//             and the draw call. All that is left here is the parts() traversal.
public abstract class BasicEntityModel<T extends Entity> extends untamedwilds.client.render.compat.EntityModel<T> {
    public int textureWidth = 64;
    public int textureHeight = 32;

    protected BasicEntityModel() {
        super();
    }

    protected BasicEntityModel(Function<Identifier, RenderType> p_102613_) {
        super(p_102613_);
    }

    @Override
    public void renderToBuffer(PoseStack p_103013_, VertexConsumer p_103014_, int p_103015_, int p_103016_, float p_103017_, float p_103018_, float p_103019_, float p_103020_) {
        this.parts().forEach((p_103030_) -> {
            p_103030_.render(p_103013_, p_103014_, p_103015_, p_103016_, p_103017_, p_103018_, p_103019_, p_103020_);
        });
    }

    public abstract Iterable<BasicModelPart> parts();
}
