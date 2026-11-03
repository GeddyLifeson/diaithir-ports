/* SPDX-License-Identifier: MIT */

package li.cil.oc2.data;

import li.cil.oc2.api.API;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

// TODO(oc2-restore): The NeoForge BlockStateProvider / ModelFile / ItemModelBuilder / ExistingFileHelper
//  model generator API (net.neoforged.neoforge.client.model.generators) no longer exists in 26.1.2.
//  The block state and item model generation (horizontal blocks, bus cable multipart, item display
//  transforms) needs to be re-implemented on top of the new vanilla model generators. Until then this
//  provider is a no-op and the corresponding JSON files must be supplied as static resources.
@SuppressWarnings({"UnusedReturnValue", "SameParameterValue", "unused"})
public final class ModBlockStateProvider implements DataProvider {
    private static final Identifier CABLE_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/cable_base");
    private static final Identifier CABLE_LINK_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/cable_link");
    private static final Identifier CABLE_PLUG_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/cable_plug");
    private static final Identifier CABLE_STRAIGHT_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/cable_straight");
    private static final Identifier CHARGER_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/charger");
    private static final Identifier COMPUTER_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/computer");
    private static final Identifier MONITOR_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/monitor");
    private static final Identifier DISK_DRIVE_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/disk_drive");
    private static final Identifier KEYBOARD_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/keyboard");
    private static final Identifier NETWORK_CONNECTOR_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/network_connector");
    private static final Identifier NETWORK_HUB_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/network_hub");
    private static final Identifier PROJECTOR_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/projector");
    private static final Identifier REDSTONE_INTERFACE_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/redstone_interface");
    //private static final Identifier PCI_CARD_CAGE_MODEL = Identifier.fromNamespaceAndPath(API.MOD_ID, "block/pci_card_cage");

    private final PackOutput output;

    public ModBlockStateProvider(final PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput cache) {
        // TODO(oc2-restore): generate block states and item models.
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Block States: " + API.MOD_ID;
    }
}
