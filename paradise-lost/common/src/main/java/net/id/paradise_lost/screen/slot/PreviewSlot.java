package net.id.paradise_lost.screen.slot;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;


public class PreviewSlot extends Slot {
    private final Identifier image;

    public PreviewSlot(Identifier image, Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
        this.image = image;
    }

    @Override
    public Identifier getNoItemIcon() {
        return image;
    }

}
