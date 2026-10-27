package net.id.paradise_lost.screen.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

import static net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;

public class PreviewSlot extends Slot {
    private final Identifier image;

    public PreviewSlot(Identifier image, Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.image = image;
    }

    @Override
    public Pair<Identifier, Identifier> getNoItemIcon() {
        return Pair.of(BLOCK_ATLAS, image);
    }

}
