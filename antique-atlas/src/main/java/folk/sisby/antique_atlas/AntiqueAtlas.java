package folk.sisby.antique_atlas;

import folk.sisby.antique_atlas.gui.AtlasScreen;
import folk.sisby.antique_atlas.gui.core.ScreenState;
import folk.sisby.antique_atlas.item.AtlasItem;
import folk.sisby.antique_atlas.reloader.BiomeTileProviders;
import folk.sisby.antique_atlas.reloader.MarkerTextures;
import folk.sisby.antique_atlas.reloader.StructureTileProviders;
import folk.sisby.antique_atlas.reloader.TileTextures;
import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClient;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import folk.sisby.antique_atlas.waypoint.WaypointCommands;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client entry point of the NeoForge port of Antique Atlas.
 * Replaces the Fabric ClientModInitializer: Fabric API events, keybind and reload-listener helpers
 * are swapped for their NeoForge equivalents.
 */
@Mod(value = AntiqueAtlasMod.ID, dist = Dist.CLIENT)
public class AntiqueAtlas {
	public static final String ID = AntiqueAtlasMod.ID;
	public static final String NAME = "Antique Atlas";

	public static final Logger LOGGER = LogManager.getLogger(NAME);

	public static final AntiqueAtlasConfig CONFIG = AntiqueAtlasConfig.createToml(FMLPaths.CONFIGDIR.get(), "", "antique-atlas", AntiqueAtlasConfig.class);
	public static final ScreenState<AtlasScreen> lastState = new ScreenState<>();

	public static Identifier id(String path) {
		return path.contains(":") ? Identifier.tryParse(path) : Identifier.fromNamespaceAndPath(ID, path);
	}

	public static AtlasScreen openAtlasScreen() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen == null && mc.player != null && (!CONFIG.requireItem || hasHandheldAtlas(mc.player))) {
			AtlasScreen screen = new AtlasScreen();
			screen.init(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
			screen.prepareToOpen();
			screen.tick();
			mc.setScreen(screen);
			return screen;
		}
		return null;
	}

	public static boolean isHandheldAtlas(ItemStack stack) {
		return stack.is(AntiqueAtlasMod.ATLAS.get());
	}

	public static boolean hasHandheldAtlas(Player player) {
		if (isHandheldAtlas(player.getOffhandItem())) return true;
		for (ItemStack itemStack : player.getInventory().getNonEquipmentItems()) {
			if (isHandheldAtlas(itemStack)) return true;
		}
		return false;
	}

	public static Map<UUID, PlayerSummary> getOrderedFriends() {
		Map<UUID, PlayerSummary> friends = SurveyorClient.getFriends();
		PlayerSummary playerSummary = friends.remove(SurveyorClient.getClientUuid());
		Map<UUID, PlayerSummary> orderedFriends = new LinkedHashMap<>(friends);
		if (playerSummary != null) orderedFriends.put(SurveyorClient.getClientUuid(), playerSummary);
		return orderedFriends;
	}

	public AntiqueAtlas(IEventBus modBus) {
		AtlasItem.OPENER = AntiqueAtlas::openAtlasScreen;
		folk.sisby.antique_atlas.gui.AtlasRenderer.registerOverlay(folk.sisby.antique_atlas.gui.ClaimsOverlay.ID, new folk.sisby.antique_atlas.gui.ClaimsOverlay());
		modBus.addListener(AntiqueAtlasKeybindings::register);
		modBus.addListener((net.neoforged.neoforge.client.event.RegisterGuiLayersEvent e) -> e.registerAboveAll(folk.sisby.antique_atlas.gui.MinimapHud.ID, new folk.sisby.antique_atlas.gui.MinimapHud()));
		modBus.addListener((AddClientReloadListenersEvent e) -> {
			e.addListener(TileTextures.ID, TileTextures.getInstance());
			e.addListener(StructureTileProviders.ID, StructureTileProviders.getInstance());
			e.addListener(BiomeTileProviders.ID, BiomeTileProviders.getInstance());
			e.addListener(MarkerTextures.ID, MarkerTextures.getInstance());
			e.addDependency(TileTextures.ID, StructureTileProviders.ID);
			e.addDependency(TileTextures.ID, BiomeTileProviders.ID);
			e.addDependency(MarkerTextures.ID, StructureTileProviders.ID);
		});
		NeoForge.EVENT_BUS.addListener(AntiqueAtlasKeybindings::onClientTick);
		NeoForge.EVENT_BUS.addListener((RegisterClientCommandsEvent e) -> WaypointCommands.register(e.getDispatcher()));

		SurveyorClientEvents.Register.terrainUpdated(id("world_data"), (s, k) -> WorldAtlasData.getOrCreate(s.dimension()).onTerrainUpdated(s, k));
		SurveyorClientEvents.Register.structuresAdded(id("world_data"), (s, k) -> WorldAtlasData.getOrCreate(s.dimension()).onStructuresAdded(s, k));
		SurveyorClientEvents.Register.landmarksAdded(id("world_data"), (s, k) -> WorldAtlasData.getOrCreate(s.dimension()).onLandmarksAdded(s, k));
		SurveyorClientEvents.Register.landmarksRemoved(id("world_data"), (s, k) -> WorldAtlasData.getOrCreate(s.dimension()).onLandmarksRemoved(s, k));
		NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.level == null || mc.getConnection() == null) return;
			SurveyorClient.getSummaries(mc.getConnection()).values().forEach(s -> {
				if (s != null) WorldAtlasData.getOrCreate(s.dimension()).tick(s);
			});
		});
		NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent e) -> {
			if (e.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED) {
				if (e.getLookupProvider() instanceof net.minecraft.core.RegistryAccess access) {
					BiomeTileProviders.getInstance().registerFallbacks(access.lookupOrThrow(Registries.BIOME));
				}
			}
		});
		NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> {
			BiomeTileProviders.getInstance().clearFallbacks();
			WorldAtlasData.WORLDS.clear();
		});

		WorldSummary.enableTerrain();
		WorldSummary.enableStructures();
		WorldSummary.enableLandmarks();
	}
}
