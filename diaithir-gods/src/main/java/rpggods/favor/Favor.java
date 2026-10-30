package rpggods.favor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import rpggods.RPGGods;
import rpggods.deity.Cooldown;
import rpggods.deity.Deity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Favor implements IFavor {

    public static final Favor EMPTY = new Favor(false);

    protected final Map<Identifier, FavorLevel> favorMap = new HashMap<>();
    protected final Map<String, Long> perkCooldownMap = new HashMap<>();
    protected final Map<Identifier, Cooldown> offeringCooldownMap = new HashMap<>();
    protected final Map<Identifier, Cooldown> sacrificeCooldownMap = new HashMap<>();
    private Optional<Identifier> patron = Optional.empty();
    private boolean enabled = true;
    private long timestamp = 0L;

    public Favor() {
    }

    private Favor(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public FavorLevel getFavor(final Identifier deityId) {
        return favorMap.computeIfAbsent(deityId, id -> {
            final FavorLevel level = new FavorLevel(0);
            Optional<Deity> deity = Optional.ofNullable(RPGGods.DEITY_MAP.get(deityId));
            deity.ifPresent(d -> {
                level.setEnabled(d.isUnlocked() && d.isEnabled());
                level.setLevelBounds(d.getMinLevel(), d.getMaxLevel());
            });
            return level;
        });
    }

    @Override
    public void setFavor(Identifier deity, FavorLevel favorLevel) {
        favorMap.put(deity, favorLevel);
    }

    @Override
    public Map<Identifier, FavorLevel> getAllFavor() {
        return favorMap;
    }

    @Override
    public Map<String, Long> getPerkCooldownMap() {
        return perkCooldownMap;
    }

    @Override
    public Map<Identifier, Cooldown> getOfferingCooldownMap() {
        return offeringCooldownMap;
    }

    @Override
    public Map<Identifier, Cooldown> getSacrificeCooldownMap() {
        return sacrificeCooldownMap;
    }

    @Override
    public boolean isEnabled() {
        return RPGGods.CONFIG.isFavorEnabled() && enabled;
    }

    @Override
    public void setEnabled(boolean enabledIn) {
        enabled = enabledIn;
    }

    @Override
    public long getCooldownTimestamp() {
        return timestamp;
    }

    @Override
    public void setCooldownTimestamp(long currentTime) {
        timestamp = currentTime;
    }

    @Override
    public Optional<Identifier> getPatron() {
        return this.patron;
    }

    @Override
    public void setPatron(Optional<Identifier> patron) {
        this.patron = patron;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder("Favor:");
        b.append(" enabled[").append(enabled).append("]");
        b.append("\nfavorMap[").append(getAllFavor().toString()).append("]");
        b.append("\ncooldownMap[").append(getPerkCooldownMap().toString()).append("]");
        return b.toString();
    }

    /** Codec used by the NeoForge data attachment that stores favor on players. */
    public static final com.mojang.serialization.MapCodec<IFavor> CODEC = CompoundTag.CODEC.fieldOf("favor").xmap(tag -> {
        Favor favor = new Favor();
        favor.deserializeNBT(tag);
        return favor;
    }, IFavor::serializeNBT);
}
