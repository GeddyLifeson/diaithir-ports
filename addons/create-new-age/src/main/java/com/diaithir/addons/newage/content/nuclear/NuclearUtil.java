package com.diaithir.addons.newage.content.nuclear;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.common.Tags;
import com.diaithir.addons.newage.CNAEffects;
import com.diaithir.addons.newage.CNAItems;
import com.diaithir.addons.newage.CNATags;
import com.diaithir.addons.newage.util.RaycastUtil;

import java.util.List;

public class NuclearUtil {
    public static void createRadiation(int length, Level world, BlockPos pos) {
        if (world.isClientSide())
            return;

        List<Entity> entities = world.getEntities(EntityTypeTest.forClass(Entity.class), new AABB(pos).inflate(length),
                livingEntity -> !isResistant(livingEntity));

        for (Entity entity : entities) {
            for (Direction dir : Direction.values()) {
                if (entity instanceof LivingEntity le) {
                    if (world.getBlockState(pos.relative(dir)).is(CNATags.Block.STOPS_RADIATION.blockTag))
                        continue;

                    Vec3 start = pos.getCenter().relative(dir, 0.5f);
                    double distance = le.getEyePosition().distanceTo(start);

                    if (distance > length)
                        continue;

                    Vec3 direction = le.getEyePosition().subtract(start).normalize();
                    HitResult hitResult = RaycastUtil.pickFilteredBlockFromPos(world, start, direction, (float) Math.ceil(distance), bs -> bs.is(CNATags.Block.STOPS_RADIATION.blockTag));

                    if (hitResult instanceof BlockHitResult bhr) {
                        if (world.getBlockState(bhr.getBlockPos()).is(CNATags.Block.STOPS_RADIATION.blockTag))
                            continue;

                        if (bhr.getLocation().distanceTo(start) < distance)
                            continue;
                    }

                    irradiate(le);
                    break;
                } /*else if (entity instanceof ItemEntity ie) {
                    // TODO: Create particles
//                    world.addParticle(ParticleTypes.ANGRY_VILLAGER,
//                            true,
//                            ie.getX() + 0.5,
//                            ie.getY() + 0.5,
//                            ie.getZ() + 0.5,
//                            0.0, 0.5, 0.0);
                    // TODO: Maybe make this a recipe type
                    ie.setItem(CNAItems.NUCLEAR_FUEL.toStack());
                }*/
            }
        }
    }

    private static boolean isResistant(Entity entity) {
        if (entity instanceof Player pl && (pl.isCreative() || pl.isSpectator()))
            return true;

        if (entity instanceof LivingEntity le) {
            for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
                if (slot.getType() != net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR)
                    continue;
                if (!le.getItemBySlot(slot).is(CNATags.Item.HAZMAT_SUIT.tag))
                    return false;
            }
        }/* else if (entity instanceof ItemEntity ie) {
            return !ie.getItem().is(Tags.Items.MUSIC_DISCS);
        }*/

        return true;
    }

    private static void irradiate(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(CNAEffects.RADIATION_POISONING, 400, 1));
    }
}
