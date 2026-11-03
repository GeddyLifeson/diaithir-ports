package li.cil.oc2.common.integration.projectred;

// TODO(oc2-restore): ProjectRed (mrtjp.projectred.api) is not available in 26.1.2, so the bundled cable
// integration is reduced to a no-op. The original implemented IBundledTileInteraction, registered itself with
// ProjectRedAPI.transmissionAPI and BundledRedstone.getInstance(), and exposed bundled signals from
// RedstoneInterfaceBlockEntity.
public final class BundledCableHandler {
    public static void initialize() {
        // TODO(oc2-restore): register the handler with ProjectRed once the API is available again.
    }

    private BundledCableHandler() {
    }
}
