package net.id.paradise_lost.test.mixin;

import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HangingSignEditScreen.class)
public interface HangingSignEditScreenAccessor {
    @Accessor("texture")
    Identifier testmod$texture();
}
