package cn.leolezury.eternalstarlight.common.item.combat;

import cn.leolezury.eternalstarlight.common.item.combat.ESItemTiers;

import net.minecraft.world.item.Item;

import cn.leolezury.eternalstarlight.common.entity.projectile.EnergySpark;
import cn.leolezury.eternalstarlight.common.util.SpecialItemCooldown;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnergySwordItem extends Item {
	public EnergySwordItem(ESItemTiers tier, Properties properties) {
		super(tier.sword(properties));
	}

	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		super.postHurtEnemy(stack, target, attacker);
		Level level = attacker.level();
		if (!level.isClientSide() && !SpecialItemCooldown.isOnCooldown(attacker, this)) {
			for (int i = 0; i < attacker.getRandom().nextInt(5, 8); i++) {
				EnergySpark spark = new EnergySpark(level, attacker);
				spark.setPos(target.position().add(0, target.getBbHeight() / 2, 0));
				spark.setTarget(target);
				Vec3 movement = new Vec3(attacker.getRandom().nextFloat() - 0.5, attacker.getRandom().nextFloat() - 0.5, attacker.getRandom().nextFloat() - 0.5);
				spark.shoot(movement.x, movement.y, movement.z, 0.1f, 0.2f);
				level.addFreshEntity(spark);
			}
			SpecialItemCooldown.setCooldown(attacker, this, 75);
		}
	}
}
