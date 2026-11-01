#!/usr/bin/env python3
import re, pathlib
I=pathlib.Path('src/main/java/com/github/alexmodguy/alexscaves/server/item')
P='com.github.alexmodguy.alexscaves.server.misc.ACProps.item()'
def edit(rel, pairs, regex=False, base=I):
    p=base/rel; s=p.read_text()
    for a,b in pairs:
        if regex: s=re.sub(a,b,s,flags=re.S)
        else:
            if a not in s: print('MISSING',rel,a[:60])
            s=s.replace(a,b)
    p.write_text(s)
# registry
edit('ACItemRegistry.java',[
 ('import net.neoforged.neoforge.common.DeferredSpawnEggItem;\n',''),
 ('new DeferredSpawnEggItem(type, color1, color2, '+P+')','new net.minecraft.world.item.SpawnEggItem('+P+'.spawnEgg(type.get()))'),
 ('SmithingTemplateItem.createArmorTrimTemplate(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "polarity"))','SmithingTemplateItem.createArmorTrimTemplate('+P+')'),
 ('new SignItem(('+P+').stacksTo(16), ACBlockRegistry.PEWEN_SIGN.get(), ACBlockRegistry.PEWEN_WALL_SIGN.get())','new SignItem(ACBlockRegistry.PEWEN_SIGN.get(), ACBlockRegistry.PEWEN_WALL_SIGN.get(), '+P+'.stacksTo(16).useBlockDescriptionPrefix())'),
 ('(forgeSpawnEggItem.getType(null) == type)','false'),
])
s=(I/'ACItemRegistry.java').read_text()
s=re.sub(r'new SignItem\(\('+re.escape(P)+r'\)\.stacksTo\(16\), (ACBlockRegistry\.THORNWOOD_SIGN\.get\(\)), (ACBlockRegistry\.THORNWOOD_WALL_SIGN\.get\(\))\)', r'new SignItem(\1, \2, '+P+'.stacksTo(16).useBlockDescriptionPrefix())', s)
s=re.sub(r'new StandingAndWallBlockItem\(([^,]+), ([^,]+), '+re.escape(P)+r', (Direction\.\w+)\)', r'new StandingAndWallBlockItem(\1, \2, \3, '+P+'.useBlockDescriptionPrefix())', s)
s=re.sub(r'\n\s*\w+_ARMOR_MATERIAL\.setRepairMaterial\([^;]*;','',s)
s=s.replace('DispenserBlock.registerBehavior(GALENA_GAUNTLET.get(), ArmorItem.DISPENSE_ITEM_BEHAVIOR);','')
s=s.replace('forgeSpawnEggItem.getType(null) == type','false')
s=s.replace('entry.getKey().get() instanceof DeferredSpawnEggItem forgeSpawnEggItem','entry.getKey().get() instanceof net.minecraft.world.item.SpawnEggItem forgeSpawnEggItem')
s=s.replace('new DesolateDaggerItem()','new DesolateDaggerItem()')
(I/'ACItemRegistry.java').write_text(s)
# armour material helper
edit('ACArmorMaterial.java',[('    public ArmorMaterial material() { return material; }','    public ArmorMaterial material() { return material; }\n\n    public int getDefenseForType(ArmorType type) { return material.defense().getOrDefault(type, 0); }')])
edit('DivingArmorItem.java',[('armorMaterial.getDefenseForType(slot)','armorMaterial.getDefenseForType(slot)')])
# gingerbread armour needs its own type field
edit('GingerbreadArmorItem.java',[
 ('        this.acMaterial = armorMaterial;\n','        this.acMaterial = armorMaterial;\n        this.armorType = slot;\n'),
 ('    private final ACArmorMaterial acMaterial;','    private final ACArmorMaterial acMaterial;\n    private final net.minecraft.world.item.equipment.ArmorType armorType;'),
 ('"armor_gingerbread_" + type.getName()','"armor_gingerbread_" + armorType.getName()'),
 ('EquipmentSlotGroup.bySlot(type.getSlot())','EquipmentSlotGroup.bySlot(armorType.getSlot())'),
 ('this.getDefense()','acMaterial.getDefenseForType(armorType)'),
])
# dagger
edit('DesolateDaggerItem.java',[
 ('import net.minecraft.world.item.SwordItem;\n','import net.minecraft.world.item.ToolMaterial;\n'),('import net.minecraft.world.item.Tiers;\n',''),
 ('extends SwordItem','extends Item'),
 ('super(Tiers.DIAMOND, ('+P+').rarity(ACItemRegistry.getRarityDemonic()).attributes(createDaggerAttributes()));','super('+P+'.rarity(ACItemRegistry.getRarityDemonic()).sword(ToolMaterial.DIAMOND, 4F, -2F).durability(360));'),
 ('        if (super.hurtEnemy(stack, hurt, player)) {','        super.hurtEnemy(stack, hurt, player);\n        if (true) {'),
])
edit('DesolateDaggerItem.java',[(r'\n    private static ItemAttributeModifiers createDaggerAttributes\(\).*?\n    }\n','\n'),(r'\n    public int getMaxDamage\(ItemStack stack\) \{.*?\n    }\n','\n')],regex=True)
for f in ['ExtinctionSpearItem.java','FrostmintSpearItem.java']:
    edit(f,[('return super.hurtEnemy(stack, hurtEntity, player);','super.hurtEnemy(stack, hurtEntity, player);\n        return true;')])
# nbt block pos
for p in I.rglob('*.java'):
    s=p.read_text(); o=s
    s=re.sub(r'NbtUtils\.readBlockPos\((\w+), ("[^"]*")\)',r'\1.read(\2, BlockPos.CODEC)',s)
    s=re.sub(r'(\w+)\.put\(("[^"]*"), NbtUtils\.writeBlockPos\((\w+)\)\)',r'\1.store(\2, BlockPos.CODEC, \3)',s)
    s=s.replace('foodProperties.effects()','com.github.alexmodguy.alexscaves.server.item.ACFoods.effectsOf(itemStack)') if p.name=='PrehistoricMixtureItem.java' else s
    s=re.sub(r'(\w+)\.getFoodProperties\((?:\w+, )?\w+\)',r'\1.get(net.minecraft.core.component.DataComponents.FOOD)',s)
    s=s.replace('this.getFoodProperties(stack, livingEntity)','stack.get(net.minecraft.core.component.DataComponents.FOOD)')
    if s!=o: p.write_text(s)
edit('ACFoods.java',[('    private static Food make(','''    public static java.util.List<MobEffectInstance> effectsOf(ItemStack stack) {
        Consumable c = stack.get(net.minecraft.core.component.DataComponents.CONSUMABLE);
        java.util.List<MobEffectInstance> out = new java.util.ArrayList<>();
        if (c != null) for (var e : c.onConsumeEffects()) if (e instanceof ApplyStatusEffectsConsumeEffect a) out.addAll(a.effects());
        return out;
    }

    private static Food make(''')])
edit('PrehistoricMixtureItem.java',[
 ('if (!com.github.alexmodguy.alexscaves.server.item.ACFoods.effectsOf(itemStack).isEmpty()) {','if (!ACFoods.effectsOf(itemStack).isEmpty()) {'),
 ('for (FoodProperties.PossibleEffect possibleEffect : com.github.alexmodguy.alexscaves.server.item.ACFoods.effectsOf(itemStack)) {','for (net.minecraft.world.effect.MobEffectInstance possibleEffect : ACFoods.effectsOf(itemStack)) {'),
 ('livingEntity.addEffect(possibleEffect.effect());','livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(possibleEffect));')])
edit('CaveInfoItem.java',[('level.registryAccess().registry(Registries.BIOME).orElse(null)','level.registryAccess().lookupOrThrow(Registries.BIOME)')])
edit('BiomeTreatItem.java',[('map.set(DataComponents.FOOD, ACFoods.BIOME_TREAT_DONE);','map.set(DataComponents.FOOD, ACFoods.BIOME_TREAT_DONE.food());\n            map.set(DataComponents.CONSUMABLE, ACFoods.BIOME_TREAT_DONE.consumable());'),
  ('player.canEat(itemstack.get(net.minecraft.core.component.DataComponents.FOOD).canAlwaysEat())','player.canEat(itemstack.get(net.minecraft.core.component.DataComponents.FOOD).canAlwaysEat())')])
edit('JellyBeanItem.java',[('mobeffect.applyInstantenousEffect(player, player, living, mobeffectinstance.getAmplifier(), 1.0D);','mobeffect.applyInstantenousEffect((net.minecraft.server.level.ServerLevel) level, player, player, living, mobeffectinstance.getAmplifier(), 1.0D);'),
  ('return living.eat(level, stack);','return super.finishUsingItem(stack, level, living);')])
edit('CaveMapItem.java',[('tag.getByteArray("MapBiomes")','tag.getByteArray("MapBiomes").orElse(new byte[0])')])
edit('SackOfSatingItem.java',[('foodStack.getItem().getCraftingRemainder() != null ? new ItemStack(foodStack.getItem().getCraftingRemainder(), foodStack.getCount()) : ItemStack.EMPTY','foodStack.getCraftingRemainder().isEmpty() ? ItemStack.EMPTY : foodStack.getCraftingRemainder().copyWithCount(foodStack.getCount())'),
  (' || foodStack.getItem() instanceof HoneyBottleItem',' || foodStack.is(Items.HONEY_BOTTLE)')])
edit('SeaStaffItem.java',[('entity.isInWaterRainOrBubble()','(entity.isInWater() || entity.isInRain())')])
edit('PrimitiveClubItem.java',[('    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, @javax.annotation.Nullable net.minecraft.world.entity.EquipmentSlot slotArg) {','    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, @javax.annotation.Nullable net.minecraft.world.entity.EquipmentSlot slotArg) {\n        boolean held = slotArg == net.minecraft.world.entity.EquipmentSlot.MAINHAND;')])

# ---- round 2
edit('DreadbowItem.java',[('abstractArrow.setBaseDamage(abstractArrow.getBaseDamage() * 2.0F);','abstractArrow.setBaseDamage(4.0D);')])
edit('SeaStaffItem.java',[('(entity.isInWater() || entity.isInRain())','(entity.isInWater() || entity.level().isRainingAt(entity.blockPosition()))')])
edit('RaygunItem.java',[('level.getTimeOfDay(1.0F)','com.github.alexmodguy.alexscaves.server.misc.ACMath.celestialAngle(level)'),('level.isDay()','level.isBrightOutside()'),('maxAABB, Entity::canBeHitByProjectile);','maxAABB, Entity::canBeHitByProjectile, 0.0F);')])
edit('CaveBoatItem.java',[('Boat boat = this.hasChest ? new AlexsCavesChestBoatEntity(level, hitresult.getLocation(), type) : new AlexsCavesBoatEntity(level, hitresult.getLocation(), type);','Boat boat = this.hasChest ? (Boat) new AlexsCavesChestBoatEntity(level, hitresult.getLocation(), type) : (Boat) new AlexsCavesBoatEntity(level, hitresult.getLocation(), type);')])
for f in ['RadiationRemovingFoodItem.java','DarkenedAppleItem.java']:
    edit(f,[('this.get(net.minecraft.core.component.DataComponents.FOOD)','stack.get(net.minecraft.core.component.DataComponents.FOOD)')])
edit('ModFishBucketItem.java',[('super(fishTypeIn.get(),','super((EntityType) fishTypeIn.get(),'),('checkExtraContent(@Nullable Player player,','checkExtraContent(@Nullable net.minecraft.world.entity.LivingEntity player,')])
edit('SackOfSatingItem.java',[('foodStack.getCraftingRemainder().isEmpty() ? ItemStack.EMPTY : foodStack.getCraftingRemainder().copyWithCount(foodStack.getCount())','foodStack.getItem().getCraftingRemainder() == null ? ItemStack.EMPTY : foodStack.getItem().getCraftingRemainder().create().copyWithCount(foodStack.getCount())')])
edit('RadioactiveOnDestroyedBlockItem.java',[('cloud.setParticle(ACParticleRegistry.GAMMAROACH.get());','cloud.setCustomParticle(ACParticleRegistry.GAMMAROACH.get());')])
