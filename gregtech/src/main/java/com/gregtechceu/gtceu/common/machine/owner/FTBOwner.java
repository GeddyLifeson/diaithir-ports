package com.gregtechceu.gtceu.common.machine.owner;

import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.network.chat.Component;




import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public non-sealed class FTBOwner extends MachineOwner {

    private static final Component displayName = Component.translatable("gtceu.ownership.name.ftb");

    public FTBOwner(UUID playerUUID) {
        super(playerUUID);
    }

    public @Nullable Team getPlayerTeam(UUID playerUUID) { return null; /* ported stub */ }

    public @Nullable Team getTeam() {
        return getPlayerTeam(playerUUID);
    }

    @UnmodifiableView
    @Override
    public @NotNull Set<UUID> getMembers() { return null; /* ported stub */ }

    @Override
    public boolean isPlayerInTeam(UUID playerUUID) { return false; /* ported stub */ }

    @Override
    public boolean isPlayerFriendly(UUID playerUUID) { return false; /* ported stub */ }

    @Override
    public UUID getUUID() { return null; /* ported stub */ }

    @Override
    public String getName() { return null; /* ported stub */ }

    @Override
    public Component getTypeDisplayName() {
        return displayName;
    }

    @Override
    public void displayInfo(List<Component> compList) {
        super.displayInfo(compList);
        compList.add(Component.translatable("behavior.portable_scanner.team_name", getName()));
        MachineOwner.displayPlayerInfo(compList, playerUUID);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof FTBOwner && super.equals(object);
    }
}
