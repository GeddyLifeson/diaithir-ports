package rpggods.util;

import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;


import net.neoforged.bus.api.Event;
import rpggods.favor.FavorLevel;

import javax.annotation.Nullable;

/**
 * This event is fired on the server when favor is about to change.
 * This event is not cancellable.

 * This event is fired on the NeoForge event bus.
 */
public class FavorChangedEvent extends Event {

    protected final Player player;
    protected final Identifier deity;
    protected final long oldFavor;
    protected final int oldLevel;
    protected final Source source;
    protected final long newFavor;
    protected final int newLevel;
    protected final boolean isLevelChange;

    /**
     * @param playerIn  the player whose favor is changing, or null for global favor
     * @param deityIn   the deity whose favor is changing
     * @param prevFavor the amount of favor before this event
     * @param curFavor  the amount of favor after this event
     * @param sourceIn  the source of the change in favor
     */
    private FavorChangedEvent(@Nullable final Player playerIn, final Identifier deityIn,
                              final long prevFavor, final long curFavor, final Source sourceIn) {
        this.player = playerIn;
        deity = deityIn;
        oldFavor = prevFavor;
        newFavor = curFavor;
        source = sourceIn;
        oldLevel = FavorLevel.calculateLevel(prevFavor);
        newLevel = FavorLevel.calculateLevel(curFavor);
        isLevelChange = oldLevel != newLevel;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the Deity whose favor is changing
     **/
    public Identifier getDeity() {
        return deity;
    }

    /**
     * @return the amount of favor from before this event
     **/
    public long getOldFavor() {
        return oldFavor;
    }

    /**
     * @return the favor level from before this event
     **/
    public int getOldLevel() {
        return oldLevel;
    }

    /**
     * @return the new favor amount
     **/
    public long getNewFavor() {
        return newFavor;
    }

    /**
     * @return the new favor level
     **/
    public int getNewLevel() {
        return newLevel;
    }

    /**
     * @return the Source of the change in favor
     **/
    public Source getSource() {
        return source;
    }

    /**
     * @return true if this change in favor also changes the favor level
     **/
    public boolean isLevelChange() {
        return isLevelChange;
    }


    public static class Pre extends FavorChangedEvent {

        protected long newFavorToApply;
        protected int newLevelToApply;
        protected boolean isLevelChangeToApply;

        /**
         * @param playerIn  the player whose favor is changing, or null for global favor
         * @param deityIn   the deity whose favor is changing
         * @param prevFavor the amount of favor before this event
         * @param curFavor  the amount of favor after this event
         * @param sourceIn  the source of the change in favor
         */
        public Pre(@Nullable Player playerIn, Identifier deityIn, long prevFavor, long curFavor, Source sourceIn) {
            super(playerIn, deityIn, prevFavor, curFavor, sourceIn);
            setNewFavor(curFavor);
        }

        /**
         * @param favor the favor amount that should be applied instead
         */
        public void setNewFavor(final long favor) {
            newFavorToApply = favor;
            newLevelToApply = FavorLevel.calculateLevel(newFavorToApply);
            isLevelChangeToApply = newLevelToApply != oldLevel;
        }

        /**
         * @return the amount of favor that will be applied after this event
         **/
        @Override
        public long getNewFavor() {
            return newFavorToApply;
        }

        /**
         * @return the new favor level
         **/
        @Override
        public int getNewLevel() {
            return newLevelToApply;
        }

        /**
         * @return true if this change in favor also changes the favor level
         **/
        @Override
        public boolean isLevelChange() {
            return isLevelChangeToApply;
        }
    }


    public static class Post extends FavorChangedEvent {

        /**
         * @param playerIn  the player whose favor is changing, or null for global favor
         * @param deityIn   the deity whose favor is changing
         * @param prevFavor the amount of favor before this event
         * @param curFavor  the amount of favor after this event
         * @param sourceIn  the source of the change in favor
         */
        public Post(@Nullable Player playerIn, Identifier deityIn, long prevFavor, long curFavor, Source sourceIn) {
            super(playerIn, deityIn, prevFavor, curFavor, sourceIn);
        }
    }


    /**
     * This is used to indicate why the favor is changing
     *
     * @see FavorChangedEvent
     */
    public static enum Source implements StringRepresentable {
        DECAY("decay"),
        OFFERING("offering"),
        SACRIFICE("sacrifice"),
        PERK("perk"),
        COMMAND("command"),
        OTHER("other");

        private final String name;

        private Source(final String sourceName) {
            name = sourceName;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
