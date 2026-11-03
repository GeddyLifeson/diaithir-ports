package net.diaithir.druidry.ars.api.spell;


import net.diaithir.druidry.ars.api.particle.timelines.IParticleTimeline;

public interface ITimelined<T extends IParticleTimeline<T>> {
    T getTimeline();
}
