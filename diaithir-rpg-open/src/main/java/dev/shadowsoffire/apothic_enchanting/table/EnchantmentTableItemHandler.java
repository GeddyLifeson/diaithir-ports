package dev.shadowsoffire.apothic_enchanting.table;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class EnchantmentTableItemHandler extends ItemStacksResourceHandler {

    public static final AttachmentType<EnchantmentTableItemHandler> TYPE = AttachmentType.serializable(EnchantmentTableItemHandler::new).build();

    public EnchantmentTableItemHandler() {
        super(1);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return index == 0 && resource.is(Tags.Items.ENCHANTING_FUELS);
    }
}
