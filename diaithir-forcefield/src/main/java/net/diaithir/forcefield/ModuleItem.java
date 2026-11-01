package net.diaithir.forcefield;

import net.minecraft.world.item.Item;

/** An upgrade module. Use it on a projector to fit it. */
public final class ModuleItem extends Item {
    public final Module module;

    public ModuleItem(Module module, Properties properties) {
        super(properties);
        this.module = module;
    }
}
