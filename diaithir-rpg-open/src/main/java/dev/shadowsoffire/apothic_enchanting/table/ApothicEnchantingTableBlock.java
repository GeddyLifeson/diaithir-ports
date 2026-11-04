package dev.shadowsoffire.apothic_enchanting.table;

import dev.shadowsoffire.apothic_enchanting.ApothicEnchanting;
import net.minecraft.resources.Identifier;

public class ApothicEnchantingTableBlock extends ApothEnchantingTableBlock implements BookTexturedTable {

    public static final Identifier BOOK_TEXTURE_ID = ApothicEnchanting.loc("apothic_book");

    public ApothicEnchantingTableBlock(Properties props) {
        super(props);
    }

    @Override
    public Identifier getBookTextureId() {
        return BOOK_TEXTURE_ID;
    }

}
