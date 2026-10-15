package mcjty.rftoolsutility.modules.screen.blocks;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsbase.tools.ManualHelper;


import static mcjty.lib.builder.TooltipBuilder.*;

public class ScreenControllerBlock extends BaseBlock {

    public ScreenControllerBlock() {
        super(new BlockBuilder()
                .manualEntry(ManualHelper.create("rftoolsbase:machines/screen_controller"))
                .infusable()
                .info(key("message.rftoolsutility.shiftmessage"))
                .infoShift(header(), gold())
                .tileEntitySupplier(ScreenControllerTileEntity::new));
    }

}
