import re,glob,os
exec(open('tools/rename11.py').read().split('def main')[0])
SL='net.minecraft.server.level.ServerLevel'
files={os.path.basename(f)[:-5]:f for f in glob.glob(ROOT+'**/*.java',recursive=True)}
def rd(n): return open(files[n]).read()
def wr(n,s): open(files[n],'w').write(s)
# hurtServer stubs
for n in ['ThrownWasteDrumEntity','FloaterEntity','SugarStaffHexEntity','BeholderEyeEntity','MineGuardianAnchorEntity','SpinningPeppermintEntity','DesolateDaggerEntity','MagneticWeaponEntity','WaveEntity','NuclearExplosionEntity','MeltedCaramelEntity','DinosaurSpiritEntity','NuclearBombEntity','AbstractMovingBlockEntity']:
    s=rd(n)
    if 'boolean hurtServer(' in s: print('has hurtServer',n); continue
    i=s.rindex('}')
    s=s[:i]+'\n    @Override\n    public boolean hurtServer('+SL+' serverLevel, net.minecraft.world.damagesource.DamageSource source, float amount) {\n        return false;\n    }\n}\n'
    wr(n,s)
for f in glob.glob(ROOT+'**/*.java',recursive=True):
    s=open(f).read();o=s;n=os.path.basename(f)[:-5]
    s=s.replace('isControlledByLocalInstance()','isLocalInstanceAuthoritative()')
    s=s.replace('this.absMoveTo(','this.snapTo(')
    s=re.sub(r'@Override\s*\n(\s*public Vec3 collide\()',r'\1',s)
    s=re.sub(r'@Override\s*\n(\s*public void lerpTo\()',r'\1',s)
    s=re.sub(r'@Override\s*\n(\s*public AABB getBoundingBoxForCulling)',r'\1',s)
    s=s.replace('super.getBoundingBoxForCulling()','this.getBoundingBox()')
    ext=re.search(r'class %s\b[^{]*?extends (\w+)'%n,s)
    if ext and ext.group(1) in('Entity','PartEntity'):
        s=s.replace('super.isInvulnerableTo(serverLevel, damageSource)','this.isInvulnerableToBase(damageSource)')
        if not re.search(r'boolean isInvulnerableTo\(',s):
            s=re.sub(r'this\.isInvulnerableTo\(serverLevel, (\w+)\)',r'this.isInvulnerableToBase(\1)',s)
        s=re.sub(r'@Override\s*\n(\s*public boolean isInvulnerableTo\()',r'\1',s)
    if s!=o: open(f,'w').write(s)
# gumworm segment override removal
s=rd('GumWormSegmentEntity')
s=re.sub(r'\n    @Override\n    public boolean isLocalInstanceAuthoritative\(\) \{\n        return this.isEffectiveAi\(\);\n    \}\n','\n',s)
s=s.replace('this.level().getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING)','(this.level() instanceof '+SL+' __sl ? __sl.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.MAX_ENTITY_CRAMMING) : 24)')
wr('GumWormSegmentEntity',s)
# submarine bubble
s=rd('SubmarineEntity')
s=s.replace('''    @Override
    public void onAboveBubbleCol(boolean b) {
        if (!isVehicle()) {
            super.onAboveBubbleCol(b);
        }
    }''','''    @Override
    public void onAboveBubbleColumn(boolean b, net.minecraft.core.BlockPos bubblePos) {
        if (!isVehicle()) {
            super.onAboveBubbleColumn(b, bubblePos);
        }
    }''').replace('''        if (!isVehicle()) {
            super.onAboveBubbleCol(b);
        }
        this.resetFallDistance();''','''        if (!isVehicle()) {
            super.onInsideBubbleColumn(b);
        }
        this.resetFallDistance();''')
wr('SubmarineEntity',s)
# throwable item projectiles
for n in['DepthChargeEntity','GuanoEntity','InkBombEntity','CinderBrickEntity','ThrownIceCreamScoopEntity']:
    s=rd(n)
    m=re.search(r'getDefaultItem\(\)\s*\{\s*return ([^;]+);',s)
    item=m.group(1)
    if n=='DepthChargeEntity':
        item='ACItemRegistry.DEPTH_CHARGE.get()' if 'ACItemRegistry' in s else item
    s=re.sub(r'super\((ACEntityRegistry\.\w+\.get\(\)), thrower, level\);',r'super(\1, thrower, level, new net.minecraft.world.item.ItemStack(%s));'%item,s)
    s=re.sub(r'super\((ACEntityRegistry\.\w+\.get\(\)), x, y, z, level\);',r'super(\1, x, y, z, level, new net.minecraft.world.item.ItemStack(%s));'%item,s)
    wr(n,s)
s=rd('DepthChargeEntity'); s=s.replace('this.checkInsideBlocks();','this.applyEffectsFromBlocks();'); wr('DepthChargeEntity',s)
s=rd('NuclearBombEntity'); s=s.replace('this.updateInWaterStateAndDoFluidPushing();','this.updateInWaterStateAndDoFluidPushing();'); wr('NuclearBombEntity',s)
s=rd('MovingMetalBlockEntity'); s=s.replace('import net.minecraft.world.entity.player.Player;','import net.minecraft.world.entity.player.Player;\nimport net.minecraft.world.entity.Entity;') if 'import net.minecraft.world.entity.Entity;' not in s else s
if 'import net.minecraft.world.entity.Entity;' not in s: s=s.replace('\nimport ','\nimport net.minecraft.world.entity.Entity;\nimport ',1)
wr('MovingMetalBlockEntity',s)
# boats
for n,cls in(('AlexsCavesBoatEntity','Boat'),('AlexsCavesChestBoatEntity','ChestBoat')):
    s=rd(n)
    s=re.sub(r'    public %s\(EntityType type, Level level\) \{\n        super\(type, level\);'%n,'''    public %s(EntityType type, Level level) {
        this(type, level, new DropRef(%s));
    }

    private static class DropRef implements java.util.function.Supplier<Item> {
        private final boolean chest;
        private AlexsCavesBoat boat;

        private DropRef(boolean chest) {
            this.chest = chest;
        }

        @Override
        public Item get() {
            AlexsCavesBoat.Type type = boat == null ? AlexsCavesBoat.Type.PEWEN : boat.getACBoatType();
            return chest ? type.getChestDropSupplier().get() : type.getDropSupplier().get();
        }
    }

    private %s(EntityType type, Level level, DropRef ref) {
        super(type, level, ref);
        ref.boat = this;'''%(n,'true' if 'Chest' in n else 'false',n),s)
    s=re.sub(r'    @Override\n    public Item getDropItem\(\) \{.*?\n    \}\n\n?','',s,flags=re.S)
    s=re.sub(r'    @Override\n    public void setVariant\(Boat\.Type vanillaType\) \{\n    \}\n\n?','',s)
    s=re.sub(r'    @Override\n    public Boat\.Type getVariant\(\) \{\n        return Boat\.Type\.OAK;\n    \}\n\n?','',s)
    wr(n,s)
print('ok')
