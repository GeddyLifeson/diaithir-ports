package mcjty.rftoolspower.modules.powercell.client;

import mcjty.rftoolspower.RFToolsPower;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ClientSetup {

    public static List<Identifier> onTextureStitch() {
        return List.of(
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellboth_t1"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellhoriz_t1"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/celllower_t1"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellmiddle_t1"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellupper_t1"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellboth_t2"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/celllower_t2"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellmiddle_t2"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellupper_t2"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellboth_t3"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/celllower_t3"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellmiddle_t3"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/cellupper_t3"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/inputmask"),
            Identifier.fromNamespaceAndPath(RFToolsPower.MODID, "block/powercell/outputmask")
        );
    }

}
