package rpggods.client.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import rpggods.RPGGods;
import rpggods.entity.PriestEntity;

/** Reuses the vanilla villager model with a newly drawn texture **/
public class PriestRenderer extends MobRenderer<PriestEntity, VillagerRenderState, VillagerModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(RPGGods.MODID, "textures/entity/priest.png");

    public PriestRenderer(final EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return TEXTURE;
    }
}
