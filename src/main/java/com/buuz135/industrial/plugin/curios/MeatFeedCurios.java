/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.plugin.curios;

import com.buuz135.industrial.item.MeatFeederItem;
import com.buuz135.industrial.module.ModuleTool;
import com.buuz135.industrial.plugin.CuriosPlugin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class MeatFeedCurios implements ICurio, ICurioRenderer {

    @Override
    public boolean canEquip(SlotContext identifier) {
        return identifier.identifier().equals("head");
    }

    @Override
    public ItemStack getStack() {
        return new ItemStack(ModuleTool.MEAT_FEEDER.get());
    }

    @Override
    public void curioTick(SlotContext slotContext) {
        ItemStack stack = CuriosPlugin.getStack(slotContext.entity(), "head", 0);
        if (stack.getItem() instanceof MeatFeederItem) {
            if (slotContext.entity().level() instanceof ServerLevel serverLevel)
                ModuleTool.MEAT_FEEDER.get().inventoryTick(stack, serverLevel, slotContext.entity(), EquipmentSlot.HEAD);
        }
    }
}
