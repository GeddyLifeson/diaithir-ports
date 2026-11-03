package net.diaithir.gellings;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

/** A slime of one {@link Species}: splits and bounces like vanilla, drops its own gel. The kind is the entity type. */
public class Gelling extends Slime {
    public Gelling(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    public Species species() {
        return GellingMod.speciesOf(getType());
    }
}
