package rpggods.favor;

import com.google.common.collect.Iterables;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import rpggods.util.INBTSerializable;
import rpggods.RPGGods;
import rpggods.deity.Offering;
import rpggods.deity.Sacrifice;
import rpggods.util.FavorChangedEvent;
import rpggods.util.Nbt;
import rpggods.deity.Cooldown;
import rpggods.perk.Patron;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public interface IFavor extends INBTSerializable<CompoundTag> {

    Identifier REGISTRY_NAME = Identifier.fromNamespaceAndPath(RPGGods.MODID, "favor");

    String ENABLED = "Enabled";
    String FAVOR_LEVELS = "FavorLevels";
    String PATRON = "Patron";
    String NAME = "Name";
    String FAVOR = "Favor";
    String PERK_COOLDOWN = "PerkCooldown";
    String OFFERING_COOLDOWN = "OfferingCooldown";
    String SACRIFICE_COOLDOWN = "SacrificeCooldown";
    String TIMESTAMP = "Timestamp";

    /**
     * Gets the FavorLevel for the given Deity
     *
     * @param deity the IDeity
     * @return the FavorLevel associated with the given IDeity
     */
    FavorLevel getFavor(final Identifier deity);

    /**
     * Updates the favor info for the given diety
     *
     * @param deity      the IDeity
     * @param favorLevel the new FavorLevel
     */
    void setFavor(final Identifier deity, final FavorLevel favorLevel);

    /**
     * @return a map of all Deity and favor info objects
     **/
    Map<Identifier, FavorLevel> getAllFavor();

    /**
     * @return a map of all cooldown categories and amounts
     */
    Map<String, Long> getPerkCooldownMap();

    /**
     * @return a map of all offering IDs and cooldowns
     */
    Map<Identifier, Cooldown> getOfferingCooldownMap();

    /**
     * @return a map of all sacrifice IDs and cooldowns
     */
    Map<Identifier, Cooldown> getSacrificeCooldownMap();

    /**
     * @return true if favor is enabled for this player
     **/
    boolean isEnabled();

    /**
     * @param enabledIn true if favor should be enabled
     **/
    void setEnabled(boolean enabledIn);

    /**
     * @return the time (in ticks) of the last cooldown calculation
     */
    long getCooldownTimestamp();

    /**
     * @param currentTime the current time
     */
    void setCooldownTimestamp(long currentTime);

    /**
     * @return the Patron deity of this favor holder
     */
    Optional<Identifier> getPatron();

    /**
     * @param patron the updated Patron deity of this favor holder, or an empty optional
     */
    void setPatron(Optional<Identifier> patron);

    /**
     * Updates the player's patron deity by removing the previous patron, if any, and adjusting favor decay
     * @param player the player
     * @param patron the new patron deity, or empty to remove the current patron
     * @return true if the patron deity was changed
     */
    default boolean setPatron(final Player player, final Patron patron) {
        Optional<Identifier> old = getPatron();
        // attempt to change patron (if no existing patron OR given patron is empty OR existing and given are different)
        if(!old.isPresent() || !patron.getDeity().isPresent() || !patron.getDeity().get().equals(old.get())) {
            // remove old patron
            if (old.isPresent()) {
                // reset favor decay and perk bonus
                FavorLevel level = getFavor(old.get());
                level.setDecayRate(0);
                level.setPerkBonus(0);
                // add favor to old patron
                level.addFavor(player, old.get(), patron.getFavorPenalty(), FavorChangedEvent.Source.PERK);
            }
            // set new patron
            setPatron(patron.getDeity());
            // add favor decay and perk bonus multipliers
            if(patron.getDeity().isPresent()) {
                FavorLevel level = getFavor(patron.getDeity().get());
                level.setDecayRate(level.getDecayRate() + patron.getFavorDecayModifier());
                level.setPerkBonus(level.getPerkBonus() + patron.getPerkChanceModifier());
            }
            return true;
        }
        return false;
    }

    /**
     * @param key the cooldown category
     * @return the amount of cooldown left for the given category, in ticks
     **/
    default long getPerkCooldown(final String key) {
        return getPerkCooldownMap().getOrDefault(key, 0L);
    }

    /**
     * @param key the cooldown category
     * @param cooldown the amount of cooldown for the given category, in ticks
     **/
    default void setPerkCooldown(final String key, long cooldown) {
        getPerkCooldownMap().put(key, cooldown);
    }

    default void reset() {
        getAllFavor().clear();
        setPatron(Optional.empty());
        resetCooldowns();
    }

    default void resetCooldowns() {
        getPerkCooldownMap().clear();
        getOfferingCooldownMap().clear();
        getSacrificeCooldownMap().clear();
    }

    /**
     * @param key the perk category
     * @return true if the time is greater than the timestamp+cooldown
     */
    default boolean hasNoPerkCooldown(final String key) {
        return getPerkCooldownMap().getOrDefault(key, 0L) <= 0;
    }

    /**
     * Sets the cooldown for a given offering
     * @param id the offering id
     * @param cooldown the cooldown to apply
     */
    default void setOfferingCooldown(final Identifier id, final Cooldown cooldown) {
        getOfferingCooldownMap().put(id, cooldown);
    }

    /**
     * @param id the offering id
     * @return the cooldown tracker for the given offering
     **/
    default Cooldown getOfferingCooldown(final Identifier id) {
        return getOfferingCooldownMap().computeIfAbsent(id, r -> RPGGods.OFFERING_MAP.getOrDefault(r, Offering.EMPTY).createCooldown());
    }

    /**
     * Sets the cooldown for a given sacrifice
     * @param id the sacrifice id
     * @param cooldown the cooldown to apply
     */
    default void setSacrificeCooldown(final Identifier id, final Cooldown cooldown) {
        getSacrificeCooldownMap().put(id, cooldown);
    }

    /**
     * @param id the sacrifice id
     * @return the cooldown tracker for the given offering
     **/
    default Cooldown getSacrificeCooldown(final Identifier id) {
        return getSacrificeCooldownMap().computeIfAbsent(id, r -> RPGGods.SACRIFICE_MAP.getOrDefault(r, Sacrifice.EMPTY).createCooldown());
    }

    /**
     * Reduces all perk, offering, and sacrifice cooldown categories by the given amount
     * @param currentTime the current time in ticks
     */
    default void tickCooldown(final long currentTime) {
        long timeSinceLastTick = currentTime - getCooldownTimestamp();
        setCooldownTimestamp(currentTime);
        // iterate over perk cooldown map
        for(Entry<String, Long> entry : getPerkCooldownMap().entrySet()) {
            if(entry.getValue() > 0) {
                entry.setValue(entry.getValue() - timeSinceLastTick);
            }
        }
        // iterate over offering and sacrifice cooldown maps
        Iterable<Cooldown> cooldowns = Iterables.concat(getOfferingCooldownMap().values(), getSacrificeCooldownMap().values());
        for(Cooldown cooldown : cooldowns) {
            if(cooldown.getCooldown() > 0) {
                cooldown.addCooldown(-timeSinceLastTick);
            }
        }
    }

    /**
     * Triggers favor decay for each deity
     * @param player the player whose favor is decayed, or null for global favor
     */
    default void depleteFavor(final Player player) {
        final int amount = RPGGods.CONFIG.getFavorDecayAmount();
        for(Entry<Identifier, FavorLevel> entry : getAllFavor().entrySet()) {
            entry.getValue().depleteFavor(player, entry.getKey(), amount, FavorChangedEvent.Source.DECAY, false);
        }
    }

    @Override
    default CompoundTag serializeNBT() {
        final CompoundTag nbt = new CompoundTag();
        final ListTag deities = new ListTag();
        // write favor levels
        for (final Entry<Identifier, FavorLevel> entry : getAllFavor().entrySet()) {
            final CompoundTag deityTag = new CompoundTag();
            deityTag.putString(NAME, entry.getKey().toString());
            deityTag.put(FAVOR, entry.getValue().serializeNBT());
            deities.add(deityTag);
        }
        nbt.put(FAVOR_LEVELS, deities);
        // write patron
        getPatron().ifPresent(p -> nbt.putString(PATRON, p.toString()));
        // write enabled
        nbt.putBoolean(ENABLED, isEnabled());
        // write perk cooldowns
        CompoundTag perkCooldowns = new CompoundTag();
        for(Entry<String, Long> entry : getPerkCooldownMap().entrySet()) {
            perkCooldowns.putLong(entry.getKey(), entry.getValue());
        }
        nbt.put(PERK_COOLDOWN, perkCooldowns);
        // write offering cooldowns
        CompoundTag offeringCooldowns = new CompoundTag();
        for(Entry<Identifier, Cooldown> entry : getOfferingCooldownMap().entrySet()) {
            offeringCooldowns.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        nbt.put(OFFERING_COOLDOWN, offeringCooldowns);
        // write sacrifice cooldowns
        CompoundTag sacrificeCooldowns = new CompoundTag();
        for(Entry<Identifier, Cooldown> entry : getSacrificeCooldownMap().entrySet()) {
            sacrificeCooldowns.put(entry.getKey().toString(), entry.getValue().serializeNBT());
        }
        nbt.put(SACRIFICE_COOLDOWN, sacrificeCooldowns);
        // write cooldown timestamp
        nbt.putLong(TIMESTAMP, getCooldownTimestamp());
        return nbt;
    }

    @Override
    default void deserializeNBT(final CompoundTag nbt) {
        final ListTag deities = Nbt.getList(nbt, FAVOR_LEVELS);
        for (int i = 0, l = deities.size(); i < l; i++) {
            final CompoundTag deity = deities.getCompoundOrEmpty(i);
            final String name = Nbt.getString(deity, NAME);
            if (deity.contains(FAVOR)) {
                setFavor(Identifier.parse(name), new FavorLevel(Nbt.getCompound(deity, FAVOR)));
            }
        }
        setEnabled((!nbt.contains(ENABLED)) || Nbt.getBoolean(nbt, ENABLED));
        if(nbt.contains(PATRON)) {
            setPatron(Optional.ofNullable(Identifier.tryParse(Nbt.getString(nbt, PATRON))));
        }
        // read perk cooldowns
        CompoundTag perks = Nbt.getCompound(nbt, PERK_COOLDOWN);
        for(String key : perks.keySet()) {
            setPerkCooldown(key, Nbt.getLong(perks, key));
        }
        // read offering cooldowns
        CompoundTag offerings = Nbt.getCompound(nbt, OFFERING_COOLDOWN);
        for(String key : offerings.keySet()) {
            setOfferingCooldown(Identifier.tryParse(key), new Cooldown(Nbt.getCompound(offerings, key)));
        }
        // read sacrifice cooldowns
        CompoundTag sacrifices = Nbt.getCompound(nbt, SACRIFICE_COOLDOWN);
        for(String key : sacrifices.keySet()) {
            setSacrificeCooldown(Identifier.tryParse(key), new Cooldown(Nbt.getCompound(sacrifices, key)));
        }
        // read timestamp
        setCooldownTimestamp(Nbt.getLong(nbt, TIMESTAMP));
    }
}
