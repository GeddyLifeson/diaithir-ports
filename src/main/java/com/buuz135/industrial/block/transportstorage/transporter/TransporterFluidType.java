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
package com.buuz135.industrial.block.transportstorage.transporter;

import com.buuz135.industrial.utils.Caps;

import net.minecraft.nbt.NbtOps;

import com.buuz135.industrial.api.IBlockContainer;
import com.buuz135.industrial.api.transporter.FilteredTransporterType;
import com.buuz135.industrial.api.transporter.TransporterType;
import com.buuz135.industrial.api.transporter.TransporterTypeFactory;
import com.buuz135.industrial.block.transportstorage.tile.TransporterTile;
import com.buuz135.industrial.block.transportstorage.transporter.filter.FluidFilter;
import com.buuz135.industrial.proxy.block.filter.RegulatorFilter;
import com.buuz135.industrial.proxy.client.render.TransporterTESR;
import com.buuz135.industrial.utils.FluidUtils;
import com.buuz135.industrial.utils.IndustrialTags;
import com.buuz135.industrial.utils.Reference;
import com.google.common.collect.Sets;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TransporterFluidType extends FilteredTransporterType<FluidStack, IFluidHandler> {

    public static final int QUEUE_SIZE = 6;

    private final HashMap<Direction, List<FluidStack>> queue;

    public TransporterFluidType(IBlockContainer<?> container, TransporterTypeFactory factory, Direction side, TransporterTypeFactory.TransporterAction action) {
        super(container, factory, side, action);
        this.queue = new HashMap<>();
        for (Direction value : Direction.values()) {
            while (this.queue.computeIfAbsent(value, direction -> new ArrayList<>()).size() < QUEUE_SIZE)
                this.queue.get(value).addFirst(FluidStack.EMPTY);

            if (this.queue.size() > QUEUE_SIZE)
                this.queue.get(value).removeLast();
        }
    }

    @Override
    public RegulatorFilter<FluidStack, IFluidHandler> createFilter() {
        // Max 8192 Buckets - 8 192 000 mb.
        return new FluidFilter(20, 20, 5, 3, 50, 200, 8192000, "mb");
    }

    @Override
    public void update() {
        super.update();
        float speed = getSpeed();

        if (getLevel().isClientSide() || getLevel().getGameTime() % (Math.max(1, 4 - speed)) != 0) return;

        IBlockContainer<?> container = getContainer();
        if (getAction() != TransporterTypeFactory.TransporterAction.EXTRACT || !(container instanceof TransporterTile transporterTile)) return;

        for (Direction direction : transporterTile.getTransporterTypeMap().keySet()) {
            TransporterType transporterType = transporterTile.getTransporterTypeMap().get(direction);
            if (transporterType instanceof TransporterFluidType fluidTransporter && transporterType.getAction() == TransporterTypeFactory.TransporterAction.INSERT) {
                var origin = Caps.fluidBlock(getLevel(), getPos().relative(this.getSide()), getSide().getOpposite());
                if (origin == null) continue;

                var destination = Caps.fluidBlock(getLevel(), getPos().relative(direction), direction.getOpposite());
                if (destination == null) continue;

                int possibleAmount = (int) Math.ceil(1000 * getEfficiency());
                FluidStack simulatedStack = origin.drain(possibleAmount, IFluidHandler.FluidAction.SIMULATE);
                int filteredAmount = Math.min(
                    this.getFilter().getExtractAmount(simulatedStack, origin),
                    fluidTransporter.getFilter().getInsertAmount(simulatedStack, destination)
                );

                int amount = Math.min(filteredAmount, possibleAmount);
                if (amount <= 0) continue;
                simulatedStack.setAmount(amount);

                int simulatedInserted = destination.fill(simulatedStack, IFluidHandler.FluidAction.SIMULATE);
                if (simulatedInserted > 0) {
                    destination.fill(origin.drain(simulatedInserted, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                    fluidTransporter.addTransferredStack(getSide(), simulatedStack);
                }
            }
        }
    }

    @Override
    public void updateClient() {
        super.updateClient();
        for (Direction value : Direction.values()) {
            while (this.queue.computeIfAbsent(value, direction -> new ArrayList<>()).size() < QUEUE_SIZE)
                this.queue.get(value).addFirst(FluidStack.EMPTY);

            this.queue.get(value).addFirst(FluidStack.EMPTY);

            while (this.queue.get(value).size() > QUEUE_SIZE)
                this.queue.get(value).removeLast();
        }
    }

    public void addTransferredStack(Direction direction, @NotNull FluidStack stack) {
        syncRender(direction, (CompoundTag) FluidStack.OPTIONAL_CODEC.encodeStart(getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow());
    }

    @Override
    public void handleRenderSync(Direction origin, CompoundTag compoundNBT) {
        this.queue.computeIfAbsent(origin, direction -> new ArrayList<>()).addFirst(FluidStack.OPTIONAL_CODEC.parse(getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE), compoundNBT).result().orElse(FluidStack.EMPTY));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void renderTransfer(Vector3f pos, Direction direction, int step, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera, float frame, Level level) {
        super.renderTransfer(pos, direction, step, stack, collector, camera, frame, level);
        if (step < queue.computeIfAbsent(direction, v -> new ArrayList<>()).size()) {
            float scale = 0.10f;
            stack.scale(scale, scale, scale);
            FluidStack fluidStack = queue.get(direction).get(step);
            float red;
            float green;
            float blue;
            float particleScale;
            if (fluidStack.isEmpty()) {
                Color CLOSE = Color.CYAN;
                Color FAR = new Color(0x6800FF);
                double ratio = (step + 2.5) / (double) QUEUE_SIZE;
                red = (int) Math.abs((ratio * FAR.getRed()) + ((1 - ratio) * CLOSE.getRed())) / 256F;
                green = (int) Math.abs((ratio * FAR.getGreen()) + ((1 - ratio) * CLOSE.getGreen())) / 256F;
                blue = (int) Math.abs((ratio * FAR.getBlue()) + ((1 - ratio) * CLOSE.getBlue())) / 256F;
                particleScale = 0.25f;
            } else {
                Color color = new Color(FluidUtils.getFluidColor(fluidStack));
                red = color.getRed() / 256F;
                green = color.getGreen() / 256F;
                blue = color.getBlue() / 256F;
                particleScale = 0.75f;
            }
            TransporterTESR.submitParticle(stack, collector, camera, particleScale, red, green, blue);

        }
    }

    public static class Factory extends TransporterTypeFactory {

        public Factory() {
            super("fluid");
        }

        @Override
        public TransporterType create(IBlockContainer container, Direction face, TransporterAction action) {
            return new TransporterFluidType(container, this, face, action);
        }

        @Override
        @Nonnull
        public Identifier getModel(Direction upgradeSide, TransporterAction action) {
            return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "block/transporters/fluid_transporter_" + action.name().toLowerCase(Locale.ROOT) + "_" + upgradeSide.getSerializedName().toLowerCase(Locale.ROOT));
        }

        @Override
        public Set<Identifier> getTextures() {
            return Sets.newHashSet(Identifier.parse("industrialforegoing:block/transporters/fluid"), Identifier.parse("industrialforegoing:block/base/bottom"));
        }

        @Override
        public boolean canBeAttachedAgainst(Level world, BlockPos pos, Direction face) {
            return Caps.fluidBlock(world, pos, face) != null;
        }

        @Nonnull
        @Override
        public Identifier getItemModel() {
            return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "block/transporters/fluid_transporter_" + TransporterAction.EXTRACT.name().toLowerCase(Locale.ROOT) + "_" + Direction.NORTH.getSerializedName().toLowerCase(Locale.ROOT));
        }

        @Override
        public void registerRecipe(RecipeOutput consumer) {
            TitaniumShapedRecipeBuilder.shapedRecipe(getUpgradeItem(), 2)
                    .pattern("IPI").pattern("GMG").pattern("ICI")
                    .define('I', Tags.Items.DUSTS_REDSTONE)
                    .define('P', Items.ENDER_PEARL)
                    .define('G', Tags.Items.GEMS_LAPIS)
                    .define('M', IndustrialTags.Items.MACHINE_FRAME_PITY)
                    .define('C', Items.PISTON)
                    .save(consumer);
        }
    }
}
