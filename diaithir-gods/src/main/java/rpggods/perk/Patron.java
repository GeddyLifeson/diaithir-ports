package rpggods.perk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import java.util.Optional;

public final class Patron {

    public static final Patron EMPTY = new Patron(Optional.empty(), 0, 0, 0);

    public static final Codec<Patron> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("deity").forGetter(Patron::getDeity),
            Codec.LONG.optionalFieldOf("penalty", 0L).forGetter(Patron::getFavorPenalty),
            Codec.FLOAT.optionalFieldOf("decay", 0.0F).forGetter(Patron::getFavorDecayModifier),
            Codec.FLOAT.optionalFieldOf("perk_chance", 0.0F).forGetter(Patron::getPerkChanceModifier)
    ).apply(instance, Patron::new));

    private final Optional<Identifier> deity;
    private final long favorPenalty;
    private final float favorDecayModifier;
    private final float perkChanceModifier;

    public Patron(Optional<Identifier> deity, long favorPenalty, float favorDecayModifier, float perkChanceModifier) {
        this.deity = deity;
        this.favorPenalty = favorPenalty;
        this.favorDecayModifier = favorDecayModifier;
        this.perkChanceModifier = perkChanceModifier;
    }

    /** @return the deity to assign as Patron, or empty to remove current patron **/
    public Optional<Identifier> getDeity() {
        return deity;
    }

    /** @return the amount of favor to add or remove from the previous patron, if any **/
    public long getFavorPenalty() {
        return favorPenalty;
    }

    /** @return the amount of favor decay to apply, from 0 to 1 **/
    public float getFavorDecayModifier() {
        return favorDecayModifier;
    }

    /** @return the amount of perk bonus to apply, from 0 to 1 **/
    public float getPerkChanceModifier() {
        return perkChanceModifier;
    }

}
