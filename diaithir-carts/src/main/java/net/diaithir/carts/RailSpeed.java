package net.diaithir.carts;

/** Implemented by every rail block of this mod: the speed cap a cart gets while it rides it. */
public interface RailSpeed {
    double maxSpeed();

    /** The gauge of the track. Rails that carry no gauge (station, routing) take any vehicle. */
    default Gauge gauge() {
        return Gauge.ANY;
    }
}
