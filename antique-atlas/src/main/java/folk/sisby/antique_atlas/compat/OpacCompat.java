package folk.sisby.antique_atlas.compat;

import folk.sisby.antique_atlas.AntiqueAtlas;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Read-only bridge to Open Parties and Claims' public client API (xaero.pac.client.api.OpenPACClientAPI).
 * Everything goes through reflection on the API interfaces, so OPAC stays an optional dependency and
 * a changed API only disables the overlay instead of crashing.
 */
public final class OpacCompat {
	public record Claim(UUID owner, int argb, Component name) {
	}

	public record Member(UUID id, String name, @Nullable Identifier dimension, double x, double z) {
	}

	private static Boolean available;
	private static Method apiGet, getClaimsManager, claimsGet, claimsGetColor, claimsGetFullName, getPartyStorage, getParty, memberStream, getDynStorage, dynAll;
	private static Method claimOwner, memberUuid, memberName, dynId, dynX, dynZ, dynDim;

	private OpacCompat() {
	}

	public static boolean available() {
		if (available == null) {
			available = false;
			if (ModList.get().isLoaded("openpartiesandclaims")) {
				try {
					Class<?> api = Class.forName("xaero.pac.client.api.OpenPACClientAPI");
					apiGet = api.getMethod("get");
					getClaimsManager = api.getMethod("getClaimsManager");
					getPartyStorage = api.getMethod("getClientPartyStorage");
					Class<?> claims = Class.forName("xaero.pac.client.claims.api.IClientClaimsManagerAPI");
					Class<?> chunkClaim = Class.forName("xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI");
					claimsGet = claims.getMethod("get", Identifier.class, int.class, int.class);
					claimsGetColor = claims.getMethod("getColor", chunkClaim, Identifier.class);
					claimsGetFullName = claims.getMethod("getFullName", chunkClaim);
					claimOwner = chunkClaim.getMethod("getPlayerId");
					Class<?> storage = Class.forName("xaero.pac.client.parties.party.api.IClientPartyStorageAPI");
					getParty = storage.getMethod("getParty");
					getDynStorage = storage.getMethod("getPartyMemberDynamicInfoSyncableStorage");
					Class<?> party = Class.forName("xaero.pac.client.parties.party.api.IClientPartyAPI");
					memberStream = party.getMethod("getMemberInfoStream");
					Class<?> member = Class.forName("xaero.pac.common.parties.party.member.api.IPartyMemberAPI");
					memberUuid = member.getMethod("getUUID");
					memberName = member.getMethod("getUsername");
					Class<?> dynStorage = Class.forName("xaero.pac.client.parties.party.api.IClientPartyMemberDynamicInfoSyncableStorageAPI");
					dynAll = dynStorage.getMethod("getAllStream");
					Class<?> dyn = Class.forName("xaero.pac.common.parties.party.api.IPartyMemberDynamicInfoSyncableAPI");
					dynId = dyn.getMethod("getPlayerId");
					dynX = dyn.getMethod("getX");
					dynZ = dyn.getMethod("getZ");
					dynDim = dyn.getMethod("getDimension");
					available = true;
				} catch (ReflectiveOperationException | LinkageError e) {
					AntiqueAtlas.LOGGER.warn("[Antique Atlas] Open Parties and Claims is present but its client API changed, claim overlay disabled", e);
				}
			}
		}
		return available;
	}

	private static Object claimsManager() throws ReflectiveOperationException {
		return getClaimsManager.invoke(apiGet.invoke(null));
	}

	/** The claim covering a chunk, or null when unclaimed or the data is not synced yet. */
	public static @Nullable Claim claimAt(Identifier dimension, int chunkX, int chunkZ) {
		if (!available()) return null;
		try {
			Object manager = claimsManager();
			Object claim = claimsGet.invoke(manager, dimension, chunkX, chunkZ);
			if (claim == null) return null;
			int color = (Integer) claimsGetColor.invoke(manager, claim, dimension);
			Component name = (Component) claimsGetFullName.invoke(manager, claim);
			return new Claim((UUID) claimOwner.invoke(claim), 0xFF000000 | color, name);
		} catch (ReflectiveOperationException | RuntimeException e) {
			return null;
		}
	}

	/** Members of the local player's OPAC party with the last synced position, excluding nobody. */
	public static List<Member> partyMembers() {
		List<Member> out = new ArrayList<>();
		if (!available()) return out;
		try {
			Object storage = getPartyStorage.invoke(apiGet.invoke(null));
			Object party = getParty.invoke(storage);
			if (party == null) return out;
			java.util.Map<UUID, String> names = new java.util.HashMap<>();
			try (Stream<Object> members = (Stream<Object>) memberStream.invoke(party)) {
				for (Object m : (Iterable<Object>) members::iterator) names.put((UUID) memberUuid.invoke(m), (String) memberName.invoke(m));
			}
			Object dynStorage = getDynStorage.invoke(storage);
			try (Stream<Object> all = (Stream<Object>) dynAll.invoke(dynStorage)) {
				for (Object d : (Iterable<Object>) all::iterator) {
					UUID id = (UUID) dynId.invoke(d);
					out.add(new Member(id, names.getOrDefault(id, "?"), (Identifier) dynDim.invoke(d), (Double) dynX.invoke(d), (Double) dynZ.invoke(d)));
				}
			}
		} catch (ReflectiveOperationException | RuntimeException ignored) {
		}
		return out;
	}
}
