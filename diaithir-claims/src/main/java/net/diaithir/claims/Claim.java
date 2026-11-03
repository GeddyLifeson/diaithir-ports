package net.diaithir.claims;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/** Who holds a chunk, who they have let in, whether it stays loaded, and its public flags. */
public record Claim(UUID owner, String ownerName, List<UUID> friends, boolean forced, int flags) {
    public static final int PUBLIC_USE = 1, ALLOW_EXPLOSIONS = 2;

    public static final Codec<Claim> CODEC = RecordCodecBuilder.create(i -> i.group(
        UUIDUtil.CODEC.fieldOf("owner").forGetter(Claim::owner),
        Codec.STRING.fieldOf("name").forGetter(Claim::ownerName),
        UUIDUtil.CODEC.listOf().fieldOf("friends").forGetter(Claim::friends),
        Codec.BOOL.optionalFieldOf("forced", false).forGetter(Claim::forced),
        Codec.INT.optionalFieldOf("flags", 0).forGetter(Claim::flags)).apply(i, Claim::new));

    public Claim(UUID owner, String ownerName, List<UUID> friends) {
        this(owner, ownerName, friends, false, 0);
    }

    /** Owner, a per-chunk friend, or a member of the owner's team. */
    public boolean allows(UUID who) {
        return owner.equals(who) || friends.contains(who) || Teams.sameTeam(owner, who);
    }

    public boolean has(int flag) {
        return (flags & flag) != 0;
    }

    public Claim withForced(boolean f) {
        return new Claim(owner, ownerName, friends, f, flags);
    }

    public Claim withFriends(List<UUID> f) {
        return new Claim(owner, ownerName, f, forced, flags);
    }

    public Claim withFlags(int f) {
        return new Claim(owner, ownerName, friends, forced, f);
    }
}
