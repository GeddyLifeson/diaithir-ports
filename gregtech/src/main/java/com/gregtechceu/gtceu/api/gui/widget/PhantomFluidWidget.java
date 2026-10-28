package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;

import com.lowdragmc.lowdraglib.gui.editor.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.ingredient.IGhostIngredientTarget;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.lowdragmc.lowdraglib.gui.compat.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import lombok.Getter;
import lombok.Setter;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@LDLRegister(name = "gtm_phantom_fluid_slot", group = "widget.gtm_container", priority = 50)
public class PhantomFluidWidget extends TankWidget implements IGhostIngredientTarget, IConfigurableWidget {

    @Setter
    private Supplier<FluidStack> phantomFluidGetter;
    @Setter
    private Consumer<FluidStack> phantomFluidSetter;

    @Nullable
    @Getter
    protected FluidStack lastPhantomStack;

    public PhantomFluidWidget() {
        super();
        this.phantomFluidGetter = () -> FluidStack.EMPTY;
        this.phantomFluidSetter = stack -> {};
    }

    public PhantomFluidWidget(@Nullable IFluidHandler fluidTank, int tank, int x, int y, int width, int height,
                              Supplier<FluidStack> phantomFluidGetter, Consumer<FluidStack> phantomFluidSetter) {
        super(fluidTank, tank, x, y, width, height, false, false);
        this.phantomFluidGetter = phantomFluidGetter;
        this.phantomFluidSetter = phantomFluidSetter;
    }

    @ConfigSetter(field = "allowClickFilled")
    public PhantomFluidWidget setAllowClickFilled(boolean v) {
        // you cant modify it
        return this;
    }

    @ConfigSetter(field = "allowClickDrained")
    public PhantomFluidWidget setAllowClickDrained(boolean v) {
        // you cant modify it
        return this;
    }

    protected void setLastPhantomStack(FluidStack fluid) {
        if (fluid != null) {
            this.lastPhantomStack = fluid.copy();
            this.lastPhantomStack.setAmount(1);
        } else {
            this.lastPhantomStack = null;
        }
    }

    public static FluidStack drainFrom(Object ingredient) { return null; /* ported stub */ }

    @Nullable
    private static Object convertIngredient(Object ingredient) {
        if (GTCEu.Mods.isEMILoaded()) {
            ingredient = EMICallWrapper.tryWrap(ingredient);
        } else if (GTCEu.Mods.isREILoaded()) {
            ingredient = REICallWrapper.tryWrap(ingredient);
        } else if (GTCEu.Mods.isJEILoaded()) {
            ingredient = JEICallWrapper.tryWrap(ingredient);
        }
        return ingredient;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public List<Target> getPhantomTargets(Object ingredient) {
        ingredient = convertIngredient(ingredient);
        if (!(ingredient instanceof FluidStack) && drainFrom(ingredient).isEmpty()) {
            return Collections.emptyList();
        }

        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new Target() {

            @NotNull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@NotNull Object ingredient) {
                ingredient = convertIngredient(ingredient);

                FluidStack ingredientStack;
                if (ingredient instanceof FluidStack fluidStack) ingredientStack = fluidStack;
                else ingredientStack = drainFrom(ingredient);

                if (!ingredientStack.isEmpty()) {
                    writeClientAction(2,
                            buf -> FluidStack.STREAM_CODEC.encode(buf, ingredientStack));
                }

                if (isClientSideWidget) {
                    if (phantomFluidSetter != null) {
                        phantomFluidSetter.accept(ingredientStack);
                    }
                }
            }
        });
    }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        if (id == 1) {
            handlePhantomClick();
        } else if (id == 2) {
            if (phantomFluidSetter != null) {
                phantomFluidSetter.accept(FluidStack.STREAM_CODEC.decode(buffer));
            }
        } else if (id == 4) {
            phantomFluidSetter.accept(FluidStack.EMPTY);
        } else if (id == 5) {
            phantomFluidSetter.accept(FluidStack.STREAM_CODEC.decode(buffer));
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        FluidStack stack = phantomFluidGetter.get();
        if (stack == null || stack.isEmpty()) {
            if (lastPhantomStack != null) {
                setLastPhantomStack(null);
                writeUpdateInfo(4, buf -> {});
            }
        } else if (lastPhantomStack == null || !FluidStack.isSameFluidSameComponents(stack, lastPhantomStack)) {
            setLastPhantomStack(stack);
            writeUpdateInfo(5, buf -> FluidStack.STREAM_CODEC.encode(buf, stack));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (isClientSideWidget) {
                handlePhantomClick();
            } else {
                writeClientAction(1, buffer -> {});
            }
            return true;
        }
        return false;
    }

    private void handlePhantomClick() {
        ItemStack itemStack = gui.getModularUIContainer().getCarried();
        FluidStack fluid = FluidUtil.getFluidContained(itemStack)
                .map(f -> f.copyWithAmount(FluidType.BUCKET_VOLUME))
                .orElse(FluidStack.EMPTY);
        if (phantomFluidSetter != null) phantomFluidSetter.accept(fluid);
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {  /* ported stub */ }

    private static class EMICallWrapper {

        private static Object tryWrap(Object ingredient) { return null; /* ported stub */ }
    }

    private static class REICallWrapper {

        private static Object tryWrap(Object ingredient) { return null; /* ported stub */ }
    }

    private static class JEICallWrapper {

        private static Object tryWrap(Object ingredient) { return null; /* ported stub */ }
    }
}
