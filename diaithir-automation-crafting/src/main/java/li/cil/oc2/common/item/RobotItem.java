/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.item;

import li.cil.oc2.common.capabilities.Capabilities;
import li.cil.oc2.common.components.RestrictedContainer;
import li.cil.oc2.common.config.Config;
import li.cil.oc2.common.energy.EnergyStorageItemStack;
import li.cil.oc2.common.entity.Entities;
import li.cil.oc2.common.entity.Robot;
import li.cil.oc2.common.entity.robot.RobotActions;
import li.cil.oc2.common.tags.ItemTags;
import li.cil.oc2.common.util.LevelUtils;
import li.cil.oc2.common.util.TooltipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static li.cil.oc2.common.Constants.*;
import static li.cil.oc2.common.util.NBTUtils.makeInventoryTag;
import static li.cil.oc2.common.util.RegistryUtils.key;

public final class RobotItem extends ModItem {
    @Override
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display, final Consumer<Component> builder, final TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        final List<Component> components = new ArrayList<>();
        TooltipUtils.addEnergyConsumption(Config.robotEnergyPerTick, components);
        TooltipUtils.addEntityEnergyInformation(stack, components);
        TooltipUtils.addInventoryInformation(stack, components);
        components.forEach(builder);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (Config.robotsUseEnergy()) {
            event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, ctx) -> {
                    return new EnergyStorageItemStack(stack, Config.robotEnergyStorage, MOD_TAG_NAME, ENERGY_TAG_NAME);
                },
                Items.ROBOT.get()
            );
        }
    }

    @Override
    public InteractionResult useOn(final UseOnContext context) {
        final Level level = context.getLevel();
        final BlockPos pos = context.getClickedPos();

        final Vec3 position;
        if (level.getBlockState(pos).canBeReplaced(new BlockPlaceContext(context))) {
            position = Vec3.atCenterOf(pos);
        } else {
            position = Vec3.atCenterOf(pos.relative(context.getClickedFace()));
        }

        final Robot robot = Entities.ROBOT.get().create(context.getLevel(), EntitySpawnReason.SPAWN_ITEM_USE);
        if (robot == null) {
            return InteractionResult.FAIL;
        }

        robot.snapTo(position.x, position.y - robot.getBbHeight() * 0.5f, position.z,
            Direction.fromYRot(context.getRotation()).getOpposite().toYRot(), 0);
        if (!level.noCollision(robot)) {
            return super.useOn(context);
        }

        if (!level.isClientSide()) {
            RobotActions.initializeData(robot);
            robot.importFromItemStack(context.getItemInHand());

            level.addFreshEntity(robot);
            Vec3i posi = new Vec3i((int) position.x, (int) position.y, (int) position.z);
            LevelUtils.playSound(level, new BlockPos(posi), SoundType.METAL, SoundType::getPlaceSound);

            if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }

        if (context.getPlayer() != null) {
            context.getPlayer().awardStat(Stats.ITEM_USED.get(this));
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////

    // TODO(oc2-restore): initializeClient with RobotWithoutLevelRenderer (client package excluded, BlockEntityWithoutLevelRenderer removed in 26.1)

    public static ItemStack getRobotWithFlash() {
        final ItemStack robot = new ItemStack(Items.ROBOT.get());
        var container = new RestrictedContainer();

        container.items().put(ItemTags.DEVICES_FLASH_MEMORY, NonNullList.withSize(1, new ItemStack(Items.FLASH_MEMORY_CUSTOM.get())));
        container.items().put(ItemTags.DEVICES_CPU, NonNullList.withSize(1, ItemStack.EMPTY));
        container.items().put(ItemTags.DEVICES_MEMORY, NonNullList.withSize(4, ItemStack.EMPTY));
        container.items().put(ItemTags.DEVICES_ROBOT_MODULE, NonNullList.withSize(4, ItemStack.EMPTY));
        container.items().put(ItemTags.DEVICES_HARD_DRIVE, NonNullList.withSize(2, ItemStack.EMPTY));

        robot.set(li.cil.oc2.common.components.DataComponents.RESTRICTED_CONTAINER, container);

        return robot;
    }
}
