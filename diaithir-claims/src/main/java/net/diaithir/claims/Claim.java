package net.diaithir.claims;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

/** Who holds a chunk, and who they have let in. */
public record Claim(UUID owner, String ownerName, List<UUID> friends) {
    public static final Codec<Claim> CODEC = RecordCodecBuilder.create(i -> i.group(
        UUIDUtil.CODEC.fieldOf("owner").forGetter(Claim::owner),
        Codec.STRING.fieldOf("name").forGetter(Claim::ownerName),
        UUIDUtil.CODEC.listOf().fieldOf("friends").forGetter(Claim::friends)).apply(i, Claim::new));

    public boolean allows(UUID who) {
        return owner.equals(who) || friends.contains(who) || net.diaithir.claims.parties.Parties.sameParty(owner, who);
    }
}
