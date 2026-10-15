package sonar.fluxnetworks.common.data;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import sonar.fluxnetworks.FluxConfig;
import sonar.fluxnetworks.api.FluxConstants;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static sonar.fluxnetworks.api.FluxConstants.INVALID_NETWORK_ID;

@ParametersAreNonnullByDefault
public class FluxPlayerData implements ValueIOSerializable {

    private boolean superAdmin = false;
    private int wirelessMode = 0;
    private int wirelessNetwork = INVALID_NETWORK_ID;

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public boolean setSuperAdmin(boolean superAdmin) {
        if (this.superAdmin != superAdmin) {
            this.superAdmin = superAdmin;
            return true;
        }
        return false;
    }

    public int getWirelessMode() {
        return this.wirelessMode;
    }

    public void setWirelessMode(int wirelessMode) {
        this.wirelessMode = wirelessMode;
    }

    public int getWirelessNetwork() {
        return this.wirelessNetwork;
    }

    public void setWirelessNetwork(int wirelessNetwork) {
        this.wirelessNetwork = wirelessNetwork;
    }

    public void copy(FluxPlayerData other) {
        superAdmin = other.superAdmin;
        wirelessMode = other.wirelessMode;
        wirelessNetwork = other.wirelessNetwork;
    }

    // Utils (Server-side only)
    public static boolean canActivateSuperAdmin(ServerPlayer player) {
        return FluxConfig.enableSuperAdmin && player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(FluxConfig.superAdminRequiredPermission)));
    }

    public static boolean isPlayerSuperAdmin(@Nonnull ServerPlayer player) {
        if (FluxConfig.enableSuperAdmin) {
            return FluxUtils.getPlayerData(player).isSuperAdmin();
        }
        return false;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean(FluxConstants.SUPER_ADMIN, superAdmin);
        output.putInt(FluxConstants.WIRELESS_MODE, wirelessMode);
        output.putInt(FluxConstants.WIRELESS_NETWORK, wirelessNetwork);
    }

    @Override
    public void deserialize(ValueInput input) {
        superAdmin = input.getBooleanOr(FluxConstants.SUPER_ADMIN, false);
        wirelessMode = input.getIntOr(FluxConstants.WIRELESS_MODE, 0);
        wirelessNetwork = input.getIntOr(FluxConstants.WIRELESS_NETWORK, 0);
    }
}
