package net.diaithir.bestiary.client;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.diaithir.bestiary.Creature;
import net.diaithir.bestiary.CreatureDef;
import net.diaithir.bestiary.DiaithirBestiary;
import net.minecraft.resources.Identifier;

/** Picks each creature's model, texture and animations from its CreatureDef. A creature whose kind has not arrived yet draws as the fallback. */
final class CreatureModel extends GeoModel<Creature> {
    private static final DataTicket<Identifier> KIND = DataTicket.create(DiaithirBestiary.ID + "_kind", Identifier.class);
    private static final DataTicket<CreatureDef> DEF = DataTicket.create(DiaithirBestiary.ID + "_def", CreatureDef.class);
    private static final Identifier FALLBACK = Identifier.fromNamespaceAndPath(DiaithirBestiary.ID, "bumblesnout");

    @Override
    public void addAdditionalStateData(Creature creature, Object relatedObject, GeoRenderState state) {
        if (creature.def() == null) return;
        state.addGeckolibData(KIND, creature.kind());
        state.addGeckolibData(DEF, creature.def());
    }

    @Override
    public Identifier getModelResource(GeoRenderState state) {
        return state.hasGeckolibData(DEF) ? state.getGeckolibData(DEF).modelId(state.getGeckolibData(KIND)) : FALLBACK.withPrefix("entity/");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState state) {
        return state.hasGeckolibData(DEF) ? state.getGeckolibData(DEF).textureId(state.getGeckolibData(KIND)) : FALLBACK.withPath("textures/entity/bumblesnout.png");
    }

    @Override
    public Identifier getAnimationResource(Creature creature) {
        return creature.def() != null ? creature.def().animationId(creature.kind()) : FALLBACK.withPrefix("entity/");
    }
}
