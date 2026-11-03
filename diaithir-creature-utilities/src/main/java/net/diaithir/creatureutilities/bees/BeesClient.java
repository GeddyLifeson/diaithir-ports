package net.diaithir.creatureutilities.bees;

import net.diaithir.creatureutilities.CreatureUtilities;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.bee.Bee;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Draws species bees with the vanilla bee model and the species' own skin (babies keep the vanilla baby skin). */
@EventBusSubscriber(modid = CreatureUtilities.ID, value = Dist.CLIENT)
public final class BeesClient {
    private BeesClient() {}

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(Bees.SPECIES_BEE.get(), SpeciesBeeRenderer::new);
    }

    public static final class State extends BeeRenderState {
        public String species = BeeSpecies.DEFAULT;
    }

    public static final class SpeciesBeeRenderer extends BeeRenderer {
        public SpeciesBeeRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public BeeRenderState createRenderState() {
            return new State();
        }

        @Override
        public void extractRenderState(Bee entity, BeeRenderState state, float partialTicks) {
            super.extractRenderState(entity, state, partialTicks);
            if (state instanceof State s && entity instanceof SpeciesBee bee) {
                s.species = bee.species();
            }
        }

        @Override
        public Identifier getTextureLocation(BeeRenderState state) {
            if (state instanceof State s && !state.isBaby && !state.isAngry) {
                return Identifier.fromNamespaceAndPath(CreatureUtilities.ID, "textures/entity/bee/" + s.species + ".png");
            }
            return super.getTextureLocation(state);
        }
    }
}
