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

package com.buuz135.industrial.module;

import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.transfer.access.ItemAccess;

import com.buuz135.industrial.item.infinity.InfinityItemEnergy;

import com.buuz135.industrial.utils.Caps;

import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import com.buuz135.industrial.block.tool.InfinityBackpackBlock;
import com.buuz135.industrial.block.tool.tile.InfinityBackpackTile;
import com.buuz135.industrial.entity.InfinityLauncherProjectileEntity;
import com.buuz135.industrial.entity.InfinityNukeEntity;
import com.buuz135.industrial.entity.InfinityTridentEntity;
import com.buuz135.industrial.item.MeatFeederItem;
import com.buuz135.industrial.item.MobImprisonmentToolItem;
import com.buuz135.industrial.item.infinity.InfinityStackHolder;
import com.buuz135.industrial.item.infinity.InfinityTankStorage;
import com.buuz135.industrial.item.infinity.ItemInfinity;
import com.buuz135.industrial.item.infinity.item.*;
import com.buuz135.industrial.utils.BlockUtils;
import com.buuz135.industrial.utils.IFAttachments;
import com.buuz135.industrial.utils.Reference;
import com.buuz135.industrial.worlddata.BackpackDataManager;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.capability.CapabilityItemStackHolder;
import com.hrznstudio.titanium.component.fluid.FluidTankComponent;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.itemstack.ItemStackHarness;
import com.hrznstudio.titanium.itemstack.ItemStackHarnessRegistry;
import com.hrznstudio.titanium.module.BlockWithTile;
import com.hrznstudio.titanium.module.DeferredRegistryHelper;
import com.hrznstudio.titanium.network.IButtonHandler;
import com.hrznstudio.titanium.tab.TitaniumTab;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.hrznstudio.titanium.api.IFactory;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;


public class ModuleTool implements IModule {

    public static TitaniumTab TAB_TOOL = new TitaniumTab(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "tool"));

    public static DeferredHolder<Item, Item> MEAT_FEEDER;
    public static DeferredHolder<Item, Item> MOB_IMPRISONMENT_TOOL;
    public static DeferredHolder<Item, Item> INFINITY_DRILL;
    public static DeferredHolder<Item, Item> MOB_ESSENCE_TOOL;
    public static DeferredHolder<Item, Item> INFINITY_SAW;
    public static DeferredHolder<Item, Item> INFINITY_HAMMER;
    public static DeferredHolder<Item, Item> INFINITY_TRIDENT;
    public static DeferredHolder<Item, Item> INFINITY_BACKPACK;
    public static BlockWithTile INFINITY_BACKPACK_BLOCK;
    public static DeferredHolder<Item, Item> INFINITY_LAUNCHER;
    public static DeferredHolder<SoundEvent, SoundEvent> NUKE_CHARGING;
    public static DeferredHolder<SoundEvent, SoundEvent> NUKE_ARMING;
    public static DeferredHolder<SoundEvent, SoundEvent> NUKE_EXPLOSION;

    public static Holder<EntityType<?>> TRIDENT_ENTITY_TYPE;
    public static Holder<EntityType<?>> INFINITY_LAUNCHER_PROJECTILE_ENTITY_TYPE;

    public static DeferredHolder<Item, Item> INFINITY_NUKE;
    public static Holder<EntityType<?>> INFINITY_NUKE_ENTITY_TYPE;

    private static ItemStackHarness infinityHarness(ItemStack stack) {
        ItemInfinity item = (ItemInfinity) stack.getItem();
        // Titanium harnesses only read Void-context capabilities; the energy and tank addons now come from the item
        return new ItemStackHarness(stack, () -> {
            List<IFactory<?>> addons = new ArrayList<>(item.getEnergyConstructor(stack).create().getScreenAddons());
            addons.addAll(item.getTankConstructor(stack).create().getScreenAddons());
            return addons;
        }, (IButtonHandler) item, CapabilityItemStackHolder.ITEMSTACK_HOLDER_CAPABILITY);
    }

    @Override
    public void generateFeatures(DeferredRegistryHelper registryHelper) {
        MEAT_FEEDER = registryHelper.registerGeneric(Registries.ITEM, "meat_feeder", () -> new MeatFeederItem(TAB_TOOL));
        MOB_IMPRISONMENT_TOOL = registryHelper.registerGeneric(Registries.ITEM, "mob_imprisonment_tool", () -> new MobImprisonmentToolItem(TAB_TOOL));
        INFINITY_DRILL = registryHelper.registerGeneric(Registries.ITEM, "infinity_drill", () -> new ItemInfinityDrill(TAB_TOOL));
        //features.add(Feature.builder("mob_essence_tool").content(Registries.ITEM, MOB_ESSENCE_TOOL = new MobEssenceToolItem(TAB_TOOL)));
        INFINITY_SAW = registryHelper.registerGeneric(Registries.ITEM, "infinity_saw", () -> new ItemInfinitySaw(TAB_TOOL));
        INFINITY_HAMMER = registryHelper.registerGeneric(Registries.ITEM, "infinity_hammer", () -> new ItemInfinityHammer(TAB_TOOL));
        INFINITY_TRIDENT = registryHelper.registerGeneric(Registries.ITEM, "infinity_trident", () -> new ItemInfinityTrident(TAB_TOOL));
        TRIDENT_ENTITY_TYPE = registryHelper.registerEntityType("trident_entity", () -> EntityType.Builder.<InfinityTridentEntity>of(InfinityTridentEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
                .setShouldReceiveVelocityUpdates(true)
                /*.setCustomClientFactory((spawnEntity, world) -> new InfinityTridentEntity((EntityType<? extends InfinityTridentEntity>) TRIDENT_ENTITY_TYPE.get(), world))*/.clientTrackingRange(5).updateInterval(1).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "trident_entity"))));
        INFINITY_BACKPACK = registryHelper.registerGeneric(Registries.ITEM, "infinity_backpack", () -> new ItemInfinityBackpack());
        INFINITY_BACKPACK_BLOCK = new BlockWithTile(
                registryHelper.registerGeneric(Registries.BLOCK, "infinity_backpack_block", InfinityBackpackBlock::new),
                registryHelper.registerBlockEntityType("infinity_backpack", () -> new BlockEntityType<>(((BasicTileBlock<?>) INFINITY_BACKPACK_BLOCK.getBlock()).getTileEntityFactory(), INFINITY_BACKPACK_BLOCK.getBlock()))
        );
        INFINITY_LAUNCHER = registryHelper.registerGeneric(Registries.ITEM, "infinity_launcher", () -> new ItemInfinityLauncher(TAB_TOOL));
        INFINITY_LAUNCHER_PROJECTILE_ENTITY_TYPE = registryHelper.registerEntityType("launcher_projectile_entity", () -> EntityType.Builder.<InfinityLauncherProjectileEntity>of(InfinityLauncherProjectileEntity::new, MobCategory.MISC).sized(0.5F, 0.5F)
                .setShouldReceiveVelocityUpdates(true)
                /*.setCustomClientFactory((spawnEntity, world) -> new InfinityLauncherProjectileEntity((EntityType<? extends InfinityLauncherProjectileEntity>) INFINITY_LAUNCHER_PROJECTILE_ENTITY_TYPE.get(), world))*/.clientTrackingRange(4).updateInterval(20).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "launcher_projectile_entity"))));
        INFINITY_NUKE = registryHelper.registerGeneric(Registries.ITEM, "infinity_nuke", () -> new ItemInfinityNuke(TAB_TOOL));
        INFINITY_NUKE_ENTITY_TYPE = registryHelper.registerEntityType("infinity_nuke", () -> EntityType.Builder.<InfinityNukeEntity>of(InfinityNukeEntity::new, MobCategory.MISC).sized(0.5F, 1.5F)
                .setShouldReceiveVelocityUpdates(true)
                /*.setCustomClientFactory((spawnEntity, world) -> new InfinityNukeEntity((EntityType<? extends InfinityNukeEntity>) INFINITY_NUKE_ENTITY_TYPE.get(), world))*/.fireImmune().clientTrackingRange(8).updateInterval(20).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "infinity_nuke"))));
        NUKE_CHARGING = registryHelper.registerGeneric(Registries.SOUND_EVENT, "nuke_charging", () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "nuke_charging"), 128));
        NUKE_ARMING = registryHelper.registerGeneric(Registries.SOUND_EVENT, "nuke_arming", () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "nuke_arming"), 16));
        NUKE_EXPLOSION = registryHelper.registerGeneric(Registries.SOUND_EVENT, "nuke_explosion", () -> SoundEvent.createFixedRangeEvent(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "nuke_explosion"), 128));

        ItemStackHarnessRegistry.register(INFINITY_SAW, ModuleTool::infinityHarness);
        ItemStackHarnessRegistry.register(INFINITY_DRILL, ModuleTool::infinityHarness);
        ItemStackHarnessRegistry.register(INFINITY_HAMMER, ModuleTool::infinityHarness);
        ItemStackHarnessRegistry.register(INFINITY_TRIDENT, ModuleTool::infinityHarness);
        ItemStackHarnessRegistry.register(INFINITY_BACKPACK, ModuleTool::infinityHarness);
        ItemStackHarnessRegistry.register(INFINITY_LAUNCHER, ModuleTool::infinityHarness);
        ItemStackHarnessRegistry.register(INFINITY_NUKE, ModuleTool::infinityHarness);
        EventManager.forge(BreakBlockEvent.class).filter(breakEvent -> breakEvent.getPlayer().getMainHandItem().getItem() == INFINITY_SAW.get() && BlockUtils.isLog((Level) breakEvent.getLevel(), breakEvent.getPos())).process(breakEvent -> {
            breakEvent.setCanceled(true);
            breakEvent.getPlayer().getMainHandItem().mineBlock((Level) breakEvent.getLevel(), breakEvent.getState(), breakEvent.getPos(), breakEvent.getPlayer());
        }).subscribe();
        EventManager.mod(RegisterCapabilitiesEvent.class).process(event -> {
            event.registerItem(Capabilities.Energy.ITEM, (stack, access) -> new InfinityItemEnergy(access), INFINITY_SAW.get(), INFINITY_DRILL.get(), INFINITY_HAMMER.get(), INFINITY_TRIDENT.get(), INFINITY_BACKPACK.get(), INFINITY_LAUNCHER.get(), INFINITY_NUKE.get());
            event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> {
                return stack.getItem() instanceof ItemInfinity itemInfinity ? itemInfinity.getTankConstructor(stack).create().on(access) : null;
            }, INFINITY_SAW.get(), INFINITY_DRILL.get(), INFINITY_HAMMER.get(), INFINITY_TRIDENT.get(), INFINITY_BACKPACK.get(), INFINITY_LAUNCHER.get(), INFINITY_NUKE.get());
            event.registerItem(Capabilities.Fluid.ITEM, (stack, access) -> new InfinityTankStorage(access, new InfinityTankStorage.TankDefinition("meat", 512_000, 0, 0, fluidStack -> fluidStack.is(ModuleCore.MEAT.getSourceFluid()), true, true, FluidTankComponent.Type.SMALL, new FluidStack(ModuleCore.MEAT.getSourceFluid().get(), 1000))), MEAT_FEEDER.get());
            event.registerItem(CapabilityItemStackHolder.ITEMSTACK_HOLDER_CAPABILITY, (o, unused) -> new InfinityStackHolder(), INFINITY_SAW.get(), INFINITY_DRILL.get(), INFINITY_HAMMER.get(), INFINITY_TRIDENT.get(), INFINITY_BACKPACK.get(), INFINITY_LAUNCHER.get(), INFINITY_NUKE.get());
            event.registerItem(Capabilities.Item.ITEM, (o, unused) -> {
                if (o.getItem() instanceof ItemInfinityBackpack itemInfinity && o.has(IFAttachments.INFINITY_BACKPACK_ID)) {
                    String id = o.get(IFAttachments.INFINITY_BACKPACK_ID);
                    if (BackpackDataManager.CLIENT_SIDE_BACKPACKS.containsKey(id)) {
                        return BackpackDataManager.CLIENT_SIDE_BACKPACKS.get(id);
                    } else if (ServerLifecycleHooks.getCurrentServer() != null) {
                        BackpackDataManager manager = BackpackDataManager.getData(ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD));
                        if (manager != null) {
                            return manager.getBackpack(id);
                        }
                    }
                }
                return null;
            }, INFINITY_BACKPACK.get());
            event.registerBlockEntity(Capabilities.Item.BLOCK, INFINITY_BACKPACK_BLOCK.type().get(), (object, context) -> {
                if (object instanceof InfinityBackpackTile tile) {
                    String id = tile.getBackpack().get(IFAttachments.INFINITY_BACKPACK_ID);
                    BackpackDataManager manager = BackpackDataManager.getData(tile.getLevel());
                    if (manager != null) {
                        return manager.getBackpack(id);
                    }
                }
                return null;
            });
            event.registerBlockEntity(Capabilities.Fluid.BLOCK, INFINITY_BACKPACK_BLOCK.type().get(), (object, context) -> {
                if (object instanceof InfinityBackpackTile tile) {
                    return ItemAccess.forStack(tile.getBackpack()).getCapability(Capabilities.Fluid.ITEM);
                } else {
                    return null;
                }
            });
            event.registerBlockEntity(Capabilities.Energy.BLOCK, INFINITY_BACKPACK_BLOCK.type().get(), (object, context) -> {
                if (object instanceof InfinityBackpackTile tile) {
                    return ItemAccess.forStack(tile.getBackpack()).getCapability(Capabilities.Energy.ITEM);
                } else {
                    return null;
                }
            });
        }).subscribe();
    }
}
