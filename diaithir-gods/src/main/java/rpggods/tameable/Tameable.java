package rpggods.tameable;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class Tameable implements ITameable {

    public static final Tameable EMPTY = new Tameable();

    private Optional<UUID> owner = Optional.empty();
    private boolean sitting;
    private boolean tamed;

    public Tameable() {}

    @Override
    public boolean isTamed() {
        return this != EMPTY && tamed;
    }

    @Override
    public Optional<UUID> getOwnerId() {
        return owner;
    }

    @Override
    public boolean isSitting() {
        return sitting;
    }

    @Override
    public void setTamed(boolean tamed) {
        this.tamed = tamed;
    }

    @Override
    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    @Override
    public void setOwnerId(@Nullable UUID ownerId) {
        this.owner = Optional.ofNullable(ownerId);
    }

    /** Codec used by the NeoForge data attachment that stores tame state on mobs. */
    public static final com.mojang.serialization.MapCodec<ITameable> CODEC = CompoundTag.CODEC.fieldOf("tameable").xmap(tag -> {
        Tameable t = new Tameable();
        t.deserializeNBT(tag);
        return t;
    }, ITameable::serializeNBT);
}
