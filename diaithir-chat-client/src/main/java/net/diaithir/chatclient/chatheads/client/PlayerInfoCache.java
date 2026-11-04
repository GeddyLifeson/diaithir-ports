// Ported from Chat Heads (MPL-2.0) https://github.com/dzwdz/chat_heads
// Modifications: extracted from ChatHeads into its own class; aliases come from the NeoForge config.
package net.diaithir.chatclient.chatheads.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.diaithir.chatclient.chatheads.config.ChatHeadsConfig;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerInfoCache {
    private static final Pattern FORMAT_REGEX = Pattern.compile("\u00a7.");

    private final ClientPacketListener connection;
    private final Map<String, PlayerInfo> playerInfos = new HashMap<>();
    private boolean collectedProfileNames = false;
    private boolean collectedEverything = false;

    public PlayerInfoCache(@NotNull ClientPacketListener connection) {
        this.connection = connection;
    }

    // TAB plugin's layout feature adds a bunch of fake PlayerInfos with names |slot_11 - |slot_90
    private Stream<PlayerInfo> getRealOnlinePlayers() {
        return connection.getOnlinePlayers().stream().filter(p -> !p.getProfile().name().startsWith("|slot_"));
    }

    public void collectProfileNames() {
        if (collectedProfileNames) return;
        collectedProfileNames = true;
        getRealOnlinePlayers().forEach(this::addProfileName);
    }

    public void addProfileName(PlayerInfo playerInfo) {
        // plugins can change profile names to contain illegal characters like formatting codes
        String profileName = FORMAT_REGEX.matcher(playerInfo.getProfile().name()).replaceAll("");
        if (profileName.isEmpty()) return;
        playerInfos.put(profileName, playerInfo);
    }

    public void collectAllNames() {
        if (collectedEverything) return;
        collectedEverything = true;

        collectProfileNames();
        getRealOnlinePlayers().forEach(this::addDisplayName);
        addNameAliases();
    }

    private void addNameAliases() {
        for (var entry : ChatHeadsConfig.nameAliases().entrySet()) {
            PlayerInfo playerInfo = playerInfos.get(entry.getValue());
            if (playerInfo != null) {
                playerInfos.putIfAbsent(entry.getKey(), playerInfo);
            }
        }
    }

    private void addDisplayName(PlayerInfo playerInfo) {
        if (playerInfo.getTabListDisplayName() != null) {
            String displayName = FORMAT_REGEX.matcher(playerInfo.getTabListDisplayName().getString()).replaceAll("");
            if (displayName.isEmpty() || displayName.equals(" ")) return;
            playerInfos.putIfAbsent(displayName, playerInfo);
        }
    }

    public void add(PlayerInfo playerInfo) {
        addProfileName(playerInfo);
        addDisplayName(playerInfo);
        addNameAliases();
    }

    public Map<Integer, List<String>> createNamesByFirstCharacterMap() {
        Map<Integer, List<String>> byFirst = new HashMap<>();
        for (String name : playerInfos.keySet()) {
            byFirst.computeIfAbsent(name.codePointAt(0), k -> new ArrayList<>()).add(name);
        }
        return byFirst;
    }

    @Nullable
    public PlayerInfo get(@NotNull String name) {
        return playerInfos.get(name);
    }

    public Set<String> getNames() {
        return playerInfos.keySet();
    }
}
