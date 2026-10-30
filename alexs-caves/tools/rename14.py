import re,glob,os
exec(open('tools/rename11.py').read().split('def main')[0])
SL='net.minecraft.server.level.ServerLevel'
P='com.github.alexmodguy.alexscaves.server.misc.ACProps'
def rw(f,pairs,base=ROOT+'entity/'):
    import glob as _g
    p=base+f if os.path.exists(base+f) else _g.glob(ROOT+'**/'+os.path.basename(f),recursive=True)[0]; s=open(p).read()
    for a,b in pairs:
        if a not in s: print('MISS',f,a[:70])
        s=s.replace(a,b)
    open(p,'w').write(s)
BUCKET_OUT=('''CompoundTag platTag = new CompoundTag();
        this.addAdditionalSaveData(platTag);''','''net.minecraft.world.level.storage.TagValueOutput platOut = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, this.registryAccess());
        this.addAdditionalSaveData(platOut);
        CompoundTag platTag = platOut.buildResult();''')
def tvi(expr): return 'net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, this.registryAccess(), %s)'%expr
for f in('SweetishFishEntity','GossamerWormEntity','LanternfishEntity','TripodfishEntity','RadgillEntity'):
    p=ROOT+'entity/living/'+f+'.java'; s=open(p).read()
    s=s.replace(*BUCKET_OUT)
    s=s.replace('this.readAdditionalSaveData(compound.getCompoundOrEmpty("FishBucketTag"));','this.readAdditionalSaveData('+tvi('compound.getCompoundOrEmpty("FishBucketTag")')+');')
    s=s.replace('this.readAdditionalSaveData(compound);','this.readAdditionalSaveData('+tvi('compound')+');')
    open(p,'w').write(s)
# game rules
for f in glob.glob(ROOT+'**/*.java',recursive=True):
    s=open(f).read();o=s
    s=s.replace('import net.minecraft.world.level.GameRules;','import net.minecraft.world.level.gamerules.GameRules;')
    s=re.sub(r'(\b[\w.()]+?)\.getGameRules\(\)\.get\((net\.minecraft\.world\.level\.gamerules\.GameRules\.\w+|GameRules\.\w+)\)',lambda m:P+'.rule('+m.group(1)+', '+m.group(2)+')',s)
    if 'GameRules.' in s and 'import net.minecraft.world.level.gamerules.GameRules;' not in s and 'gamerules.GameRules.' not in s.replace('import','') :
        s=re.sub(r'(?m)^(package [^\n]+;\n)',r'\1\nimport net.minecraft.world.level.gamerules.GameRules;',s,count=1)
    s=re.sub(r'(?<![\w.])(this|\w+)\.kill\(\)',lambda m:m.group(0) if not re.search(r'Entity|Boat|entity',f) else m.group(1)+'.kill(('+SL+') '+m.group(1)+'.level())' ,s) if re.search(r'(Boat|Gingerbread)',f) else s
    s=re.sub(r'(\w+)\.this\.doHurtTarget\(\(net\.minecraft\.server\.level\.ServerLevel\) this\.level\(\)',r'\1.this.doHurtTarget((net.minecraft.server.level.ServerLevel) \1.this.level()',s)
    s=re.sub(r'new ChunkPos\((\w+\.blockPosition\(\))\)',r'ChunkPos.containing(\1)',s)
    s=s.replace('getNavigation().snapTo(target,','getNavigation().moveTo(target,')
    s=s.replace('ChunkPos.asLong(','ChunkPos.pack(')
    s=s.replace('setCustomParticle(','setCustomParticle(').replace('areaeffectcloud.setParticle(','areaeffectcloud.setCustomParticle(')
    s=s.replace('new PotionContents(java.util.Optional.empty(), java.util.Optional.of(0X77D60E), java.util.List.of())','new PotionContents(java.util.Optional.empty(), java.util.Optional.of(0X77D60E), java.util.List.of(), java.util.Optional.empty())')
    s=re.sub(r'new PotionContents\(java\.util\.Optional\.empty\(\), java\.util\.Optional\.of\(([^()]*)\), java\.util\.List\.of\(\)\)',r'new PotionContents(java.util.Optional.empty(), java.util.Optional.of(\1), java.util.List.of(), java.util.Optional.empty())',s)
    s=s.replace('(inGround','(isInGround()').replace('this.inGround = false','this.setInGround(false)').replace('!inGround','!isInGround()').replace('this.inGround','this.isInGround()').replace('&& inGround','&& isInGround()')
    s=s.replace('level().isNight()','level().isDarkOutside()')
    s=s.replace('import net.minecraft.world.entity.projectile.ThrownPotion;','import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;')
    s=s.replace('ThrownPotion thrownpotion = new ThrownPotion(licowitch.level(), licowitch);\n        thrownpotion.setItem(LicowitchEntity.hungerPotion);','ThrownSplashPotion thrownpotion = new ThrownSplashPotion(licowitch.level(), licowitch, LicowitchEntity.hungerPotion);')
    s=s.replace('@EventBusSubscriber(modid = AlexsCaves.MODID, bus = EventBusSubscriber.Bus.MOD)','@EventBusSubscriber(modid = AlexsCaves.MODID)')
    s=s.replace('this.noCulling = true;','')
    if s!=o: open(f,'w').write(s)
rw('ai/GingerbreadManOpenDoorGoal.java',[('groundpathnavigation.canOpenDoors()','true')])
rw('ai/VesperAttackGoal.java',[('holder.getUseItem().canPerformAction(ItemAbilities.SHIELD_BLOCK)','holder.getUseItem().has(net.minecraft.core.component.DataComponents.BLOCKS_ATTACKS)')])
rw('living/SauropodBaseEntity.java',[('living.isDamageSourceBlocked(damageSource)','living.isBlocking()'),('player.disableShield();','player.getCooldowns().addCooldown(player.getUseItem(), 100); player.stopUsingItem();')])
rw('util/ACBossEvent.java',[('super(component, BossEvent','super(java.util.UUID.randomUUID(), component, BossEvent')])
rw('living/CandicornEntity.java',[('PlayerRideableJumping,','PlayerRideableJumping,'),('    @Override\n    public boolean isSaddleable()','    public boolean isSaddleable()'),('    @Override\n    public void equipSaddle(','    public void equipSaddle('),('SoundEvents.PIG_SADDLE, soundSource','SoundEvents.PIG_SADDLE.value(), soundSource')])
s=open(ROOT+'entity/living/CandicornEntity.java').read(); s=re.sub(r',\s*Saddleable\b','',s); s=re.sub(r'\bSaddleable,\s*','',s); open(ROOT+'entity/living/CandicornEntity.java','w').write(s)
for f in('GumbeeperEntity','NucleeperEntity'):
    p=ROOT+'entity/living/'+f+'.java'; s=open(p).read()
    s=re.sub(r'implements PowerableMob, ','implements ',s); s=re.sub(r', PowerableMob\b','',s); s=re.sub(r'implements PowerableMob\b','implements ',s)
    s=re.sub(r'@Override\s*\n(\s*public boolean isPowered)',r'\1',s)
    open(p,'w').write(s)
rw('living/QuarrySmasherEntity.java',[('this.isInvulnerableTo(damageSource)','this.isInvulnerableTo(serverLevel, damageSource)')])
rw('living/SubmarineEntity.java',[('this.isInvulnerableTo(damageSource)','this.isInvulnerableTo(serverLevel, damageSource)'),('super.isInvulnerableTo(serverLevel, damageSource) ||','super.isInvulnerableTo(serverLevel, damageSource) ||')])
rw('living/BoundroidWinchEntity.java',[('import net.minecraft.server.level.ServerLevel;','import net.minecraft.server.level.ServerLevel;\nimport net.minecraft.world.entity.LivingEntity;')])
rw('living/TeletorEntity.java',[('this.getEquipmentDropChance(EquipmentSlot.MAINHAND)','0.085F')])
rw('item/InkBombEntity.java',[])
print('done')
