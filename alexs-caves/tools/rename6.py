#!/usr/bin/env python3
import re, pathlib, sys
R=[
 # appendHoverText
 (r'appendHoverText\(ItemStack (\w+), (?:Item\.)?TooltipContext (\w+), List<Component> (\w+), TooltipFlag (\w+)\)',
  r'appendHoverText(ItemStack \1, Item.TooltipContext \2, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> \3, TooltipFlag \4)'),
 (r'super\.appendHoverText\((\w+), (\w+), (\w+), (\w+)\)', r'super.appendHoverText(\1, \2, display, \3, \4)'),
 # inventoryTick
 (r'void inventoryTick\(ItemStack (\w+), Level (\w+), Entity (\w+), int (\w+), boolean (\w+)\)',
  r'void inventoryTick(ItemStack \1, net.minecraft.server.level.ServerLevel \2, Entity \3, @javax.annotation.Nullable net.minecraft.world.entity.EquipmentSlot slotArg)'),
 (r'super\.inventoryTick\((\w+), (\w+), (\w+), \w+, \w+\)', r'super.inventoryTick(\1, \2, \3, slotArg)'),
 # mob effects
 (r'void applyEffectTick\(LivingEntity (\w+), int (\w+)\)', r'boolean applyEffectTick(net.minecraft.server.level.ServerLevel serverLevel, LivingEntity \1, int \2)'),
 (r'\bgetCraftingRemainingItem\(\)', 'getCraftingRemainder()'),
 (r'RenderShape\.ENTITYBLOCK_ANIMATED', 'RenderShape.MODEL'),
 (r'\bMobEffects\.DIG_SPEED\b','MobEffects.HASTE'),(r'\bMobEffects\.DIG_SLOWDOWN\b','MobEffects.MINING_FATIGUE'),
 (r'\bMobEffects\.DAMAGE_BOOST\b','MobEffects.STRENGTH'),(r'\bMobEffects\.DAMAGE_RESISTANCE\b','MobEffects.RESISTANCE'),
 (r'\bMobEffects\.MOVEMENT_SPEED\b','MobEffects.SPEED'),(r'\bMobEffects\.MOVEMENT_SLOWDOWN\b','MobEffects.SLOWNESS'),
 (r'\bMobEffects\.CONFUSION\b','MobEffects.NAUSEA'),(r'\bMobEffects\.HARM\b','MobEffects.INSTANT_DAMAGE'),
 (r'\bMobEffects\.HEAL\b','MobEffects.INSTANT_HEALTH'),(r'\bMobEffects\.JUMP\b','MobEffects.JUMP_BOOST'),
 (r'\bgetProfiler\(\)', 'getProfiler()'),
 (r'public void lerpMotion\(double (\w+), double (\w+), double (\w+)\)', r'public void lerpMotion(net.minecraft.world.phys.Vec3 lerpVec)'),
 (r'boolean canBeCollidedWith\(\)', 'boolean canBeCollidedWith(@javax.annotation.Nullable Entity collidingWith)'),
 (r'\.canBeCollidedWith\(\)', '.canBeCollidedWith(null)'),
 (r'boolean isInvulnerableTo\(DamageSource (\w+)\)', r'boolean isInvulnerableTo(net.minecraft.server.level.ServerLevel serverLevel, DamageSource \1)'),
 (r'super\.isInvulnerableTo\((\w+)\)', r'super.isInvulnerableTo(serverLevel, \1)'),
 (r'void dropFromLootTable\(DamageSource (\w+), boolean (\w+)\)', r'void dropFromLootTable(net.minecraft.server.level.ServerLevel serverLevel, DamageSource \1, boolean \2)'),
 (r'super\.dropFromLootTable\((\w+), (\w+)\)', r'super.dropFromLootTable(serverLevel, \1, \2)'),
 (r'boolean shouldDropLoot\(\)', 'boolean shouldDropLoot(net.minecraft.server.level.ServerLevel serverLevel)'),
 (r'Optional<Reference<EntityType<\?>>>', 'Optional<Holder.Reference<EntityType<?>>>'),
]
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    for a,b in R: s=re.sub(a,b,s)
    if s!=o: p.write_text(s); n+=1
print(n,'files')
