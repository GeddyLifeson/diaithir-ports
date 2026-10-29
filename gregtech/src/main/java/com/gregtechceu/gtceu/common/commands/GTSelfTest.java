package com.gregtechceu.gtceu.common.commands;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

/** Developer self test for the basic chain: generator, cable, macerator. Run with /gtceu selftest. */
public final class GTSelfTest {

    private static ServerLevel level;
    private static BlockPos base;
    private static int ticks = -1;
    private static final List<String> LOG = new ArrayList<>();

    private GTSelfTest() {}

    private static ItemStack pickInput(ServerLevel serverLevel) {
        var holders = serverLevel.getServer().getRecipeManager().recipes
                .byType(com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES);
        LOG.add("macerator recipes loaded: " + holders.size());
        for (var h : holders) {
            var contents = h.value().getInputContents(
                    com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP);
            if (contents.size() != 1) continue;
            try {
                var sized = (net.neoforged.neoforge.common.crafting.SizedIngredient) contents.get(0).content;
                var items = com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions.getItems(sized);
                if (items.length > 0 && !items[0].isEmpty() && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(items[0].getItem()).getNamespace().equals("minecraft")) {
                    LOG.add("using recipe " + h.id().identifier() + " input " + items[0]);
                    return items[0].copy();
                }
            } catch (Throwable t) {
                LOG.add("pick failed " + t);
            }
        }
        return new ItemStack(Items.COBBLESTONE, 4);
    }

    public static synchronized List<String> start(ServerLevel serverLevel, BlockPos origin) {
        LOG.clear();
        ItemStack input = pickInput(serverLevel);
        level = serverLevel;
        base = origin;
        ticks = 0;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
            serverLevel.setChunkForced((origin.getX() >> 4) + dx, (origin.getZ() >> 4) + dz, true);
        // Part A: powered macerator, items inserted directly
        place(base, GTMachines.MACERATOR[GTValues.LV].getBlock().defaultBlockState());
        var a = machine(base);
        if (a instanceof WorkableTieredMachine w) {
            w.energyContainer.setEnergyStored(1000);
            ItemStack left = w.importItems.storage.insertItem(0, input.copy(), false);
            LOG.add("A: macerator placed, inserted cobblestone, leftover=" + left.getCount() + " energy="
                    + w.energyContainer.getEnergyStored());
        } else {
            LOG.add("A: FAIL macerator machine is " + a);
        }
        // Part B: combustion generator -> cable -> macerator
        BlockPos gen = base.offset(6, 0, 0);
        BlockPos cable = base.offset(5, 0, 0);
        BlockPos mac2 = base.offset(4, 0, 0);
        place(mac2, GTMachines.MACERATOR[GTValues.LV].getBlock().defaultBlockState());
        var cableBlock = GTMaterialBlocks.CABLE_BLOCKS.row(com.gregtechceu.gtceu.api.data.tag.TagPrefix.cableGtSingle)
                .get(GTMaterials.Tin);
        if (cableBlock == null) {
            LOG.add("B: FAIL no tin cable block");
        } else {
            place(cable, cableBlock.get().defaultBlockState());
        }
        var genBlock = GTMachines.COMBUSTION[GTValues.LV].getBlock();
        var genState = genBlock.defaultBlockState();
        for (var p : genState.getProperties()) {
            if (p.getName().equals("facing") && p.getValueClass() == Direction.class) {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                var st = genState.setValue((net.minecraft.world.level.block.state.properties.Property) p, Direction.WEST);
                genState = st;
            }
        }
        place(gen, genState);
        var g = machine(gen);
        if (g instanceof WorkableTieredMachine w) {
            int filled = w.importFluids.fill(new FluidStack(GTMaterials.Diesel.getFluid(), 4000),
                    IFluidHandler.FluidAction.EXECUTE);
            w.importFluids.getStorages();
            LOG.add("B: generator placed, diesel filled=" + filled);
        } else {
            LOG.add("B: FAIL generator machine is " + g);
        }
        if (level.getBlockEntity(cable) instanceof com.gregtechceu.gtceu.common.blockentity.CableBlockEntity cbe) {
            // gt6 style pipes: connections are made by hand (or by the item when placed against a block)
            cbe.setConnection(Direction.EAST, true, false);
            cbe.setConnection(Direction.WEST, true, false);
            LOG.add("B: cable connections=" + cbe.getConnections());
        }
        var m2 = machine(mac2);
        if (m2 instanceof WorkableTieredMachine w) {
            w.importItems.storage.insertItem(0, input.copy(), false);
        }
        // Part C: ore -> crushed ore -> impure dust, two powered macerators
        String[][] chain = { { "gtceu:copper_ore", "C1" }, { "gtceu:crushed_copper_ore", "C2" } };
        for (int i = 0; i < chain.length; i++) {
            BlockPos p = base.offset(0, 0, 3 + 2 * i);
            place(p, GTMachines.MACERATOR[GTValues.LV].getBlock().defaultBlockState());
            var m = machine(p);
            var item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getOptional(net.minecraft.resources.Identifier.parse(chain[i][0])).orElse(null);
            if (m instanceof WorkableTieredMachine w && item != null) {
                w.energyContainer.setEnergyStored(1000);
                w.importItems.storage.insertItem(0, new ItemStack(item, 2), false);
                LOG.add(chain[i][1] + ": inserted 2x " + chain[i][0]);
            } else {
                LOG.add(chain[i][1] + ": FAIL machine=" + m + " item=" + chain[i][0] + " -> " + item);
            }
        }
        startToolTests();
        return LOG;
    }

    private static net.minecraft.server.level.ServerPlayer fake() {
        return net.neoforged.neoforge.common.util.FakePlayerFactory.get(level,
                new com.mojang.authlib.GameProfile(java.util.UUID.fromString("00000000-0000-0000-0000-0000000000a1"), "[gt selftest]"));
    }

    private static net.minecraft.world.InteractionResult use(net.minecraft.server.level.ServerPlayer p, ItemStack stack,
                                                            BlockPos pos, Direction face) {
        p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stack);
        var center = net.minecraft.world.phys.Vec3.atCenterOf(pos);
        var eye = center.add(face.getStepX() * 2.5, face.getStepY() * 2.5, face.getStepZ() * 2.5);
        p.setPos(eye.x, eye.y - p.getEyeHeight(), eye.z);
        p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
                center.add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5));
        var hit = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(pos).add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5),
                face, pos, false);
        return p.gameMode.useItemOn(p, level, stack, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
    }

    private static ItemStack item(String id) {
        var it = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getOptional(net.minecraft.resources.Identifier.parse(id)).orElse(null);
        return it == null ? ItemStack.EMPTY : new ItemStack(it);
    }

    /** Part D: tools and covers through the real right-click path (ServerPlayerGameMode.useItemOn). */
    private static void startToolTests() {
        try {
            var p = fake();
            BlockPos mac = base.offset(0, 0, 12);
            BlockPos cab = base.offset(1, 0, 12);
            place(mac, GTMachines.MACERATOR[GTValues.LV].getBlock().defaultBlockState());
            var cableBlock = GTMaterialBlocks.CABLE_BLOCKS.row(com.gregtechceu.gtceu.api.data.tag.TagPrefix.cableGtSingle)
                    .get(GTMaterials.Tin);
            place(cab, cableBlock.get().defaultBlockState());
            var m = machine(mac);
            LOG.add("D: macerator facing before=" + (m == null ? null : m.getFrontFacing()));
            var r1 = use(p, item("gtceu:iron_wrench"), mac, Direction.UP);
            LOG.add("D wrench on macerator top: result=" + r1 + " facing after=" + machine(mac).getFrontFacing());
            p.setShiftKeyDown(true);
            var r1b = use(p, item("gtceu:iron_wrench"), mac, Direction.WEST);
            p.setShiftKeyDown(false);
            LOG.add("D sneak-wrench on macerator west: result=" + r1b + " facing after=" + machine(mac).getFrontFacing());
            boolean en = machine(mac) instanceof WorkableTieredMachine w0 && w0.isWorkingEnabled();
            var r2 = use(p, item("gtceu:wood_mallet"), mac, Direction.UP);
            boolean en2 = machine(mac) instanceof WorkableTieredMachine w1 && w1.isWorkingEnabled();
            LOG.add("D mallet: result=" + r2 + " workingEnabled " + en + " -> " + en2);
            var r3 = use(p, item("gtceu:iron_screwdriver"), mac, Direction.UP);
            LOG.add("D screwdriver: result=" + r3);
            int c0 = level.getBlockEntity(cab) instanceof com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity<?, ?> pe ? pe.getConnections() : -1;
            var r4 = use(p, item("gtceu:iron_wire_cutter"), cab, Direction.WEST);
            int c1 = level.getBlockEntity(cab) instanceof com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity<?, ?> pe ? pe.getConnections() : -1;
            LOG.add("D wire cutter on cable west (toward macerator): result=" + r4 + " connections " + c0 + " -> " + c1);
            var cover = item("gtceu:lv_conveyor_module");
            var r5 = use(p, cover, mac, Direction.SOUTH);
            var cov = machine(mac).getCoverContainer().getCoverAtSide(Direction.SOUTH);
            LOG.add("D conveyor cover on macerator south: result=" + r5 + " cover=" + (cov == null ? null : cov.getClass().getSimpleName()));
            var r6 = use(p, item("gtceu:iron_screwdriver"), mac, Direction.SOUTH);
            LOG.add("D screwdriver on cover side: result=" + r6);
        } catch (Throwable t) {
            LOG.add("D FAIL " + t);
            GTCEu.LOGGER.error("selftest D", t);
        }
    }

    public static synchronized void tick(ServerLevel l) {
        if (ticks < 0 || l != level) return;
        ticks++;
        if (ticks == 600) {
            report();
            ticks = -1;
        }
    }

    private static void report() {
        List<String> out = new ArrayList<>(LOG);
        var a = machine(base);
        if (a instanceof WorkableTieredMachine w) {
            out.add("A result: status=" + w.recipeLogic.getStatus() + " export=" + describe(w) + " energy="
                    + w.energyContainer.getEnergyStored());
        }
        var capM = level.getCapability(com.gregtechceu.gtceu.api.capability.GTCapability.CAPABILITY_ENERGY_CONTAINER,
                base.offset(4, 0, 0), Direction.EAST);
        var capG = level.getCapability(com.gregtechceu.gtceu.api.capability.GTCapability.CAPABILITY_ENERGY_CONTAINER,
                base.offset(6, 0, 0), Direction.WEST);
        out.add("B caps: macerator=" + capM + " generator=" + capG);
        var cableBe = level.getBlockEntity(base.offset(5, 0, 0));
        out.add("B cable be=" + (cableBe == null ? "null" : cableBe.saveWithoutMetadata(level.registryAccess())
                .toString().replaceAll("cover:\\{[^}]*\\}[^}]*\\}", "")));
        var g = machine(base.offset(6, 0, 0));
        if (g instanceof WorkableTieredMachine w) {
            out.add("B generator: status=" + w.recipeLogic.getStatus() + " energy=" + w.energyContainer.getEnergyStored());
        }
        var m2 = machine(base.offset(4, 0, 0));
        if (m2 instanceof WorkableTieredMachine w) {
            out.add("B macerator2: status=" + w.recipeLogic.getStatus() + " energy=" + w.energyContainer.getEnergyStored()
                    + " export=" + describe(w));
        }
        for (int i = 0; i < 2; i++) {
            var c = machine(base.offset(0, 0, 3 + 2 * i));
            if (c instanceof WorkableTieredMachine w) {
                out.add("C" + (i + 1) + " result: status=" + w.recipeLogic.getStatus() + " export=" + describe(w));
            }
        }
        out.forEach(s -> GTCEu.LOGGER.info("SELFTEST {}", s));
    }

    private static String describe(WorkableTieredMachine w) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < w.exportItems.storage.getSlots(); i++) {
            ItemStack s = w.exportItems.storage.getStackInSlot(i);
            if (!s.isEmpty()) sb.append(s.getCount()).append("x ").append(s.getItem()).append(' ');
        }
        return sb.append(']').toString();
    }

    private static MetaMachine machine(BlockPos pos) {
        return MetaMachine.getMachine(level, pos);
    }

    private static void place(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        level.setBlock(pos, state, 3);
    }
}
