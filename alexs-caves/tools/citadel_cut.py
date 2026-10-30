#!/usr/bin/env python3
"""Cut the Citadel pieces that need mixins/client (tick rate, advanced pathing, capes, lectern) out of the Caves code."""
import re, pathlib
S=pathlib.Path('src/main/java/com/github/alexmodguy/alexscaves/server')
C=pathlib.Path('src/main/java/com/github/alexthe666/citadel')
def sub(p,a,b,count=0,flags=0):
    s=p.read_text(); n=re.sub(a,b,s,count=count,flags=flags); p.write_text(n)
# entity data: persistent data of the entity
(C/'server/entity/CitadelEntityData.java').write_text('''package com.github.alexthe666.citadel.server.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/** Citadel keeps a synced tag on living entities through a mixin; here it lives in NeoForge persistent data. */
public class CitadelEntityData {
    private static final String KEY = "CitadelTag";

    public static CompoundTag getOrCreateCitadelTag(LivingEntity entity) {
        CompoundTag tag = getCitadelTag(entity);
        return tag == null ? new CompoundTag() : tag;
    }

    public static CompoundTag getCitadelTag(LivingEntity entity) {
        return entity.getPersistentData().getCompoundOrEmpty(KEY);
    }

    public static void setCitadelTag(LivingEntity entity, CompoundTag tag) {
        entity.getPersistentData().put(KEY, tag);
    }
}
''')
# proxy
sub(S/'CommonProxy.java',r'import com\.github\.alexthe666\.citadel\.server\.tick\.ServerTickRateTracker;\n','')
sub(S/'CommonProxy.java',r'return ServerTickRateTracker\.getForServer\(level\.getServer\(\)\)\.getServerTickLengthMs\(\) != 50;','return false;')
# biomes
sub(S/'level/biome/ACBiomeRegistry.java',r'import com\.github\.alexthe666\.citadel\.server\.world\.ExpandedBiomes;\n','')
sub(S/'level/biome/ACBiomeRegistry.java',r'\s*ExpandedBiomes\.addExpandedBiome\([^;]*\);','')
# sugar rush: no global tick-rate changes, slow motion is dropped
(S/'potion/SugarRushEffect.java').write_text('''package com.github.alexmodguy.alexscaves.server.potion;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class SugarRushEffect extends MobEffect {

    protected SugarRushEffect() {
        super(MobEffectCategory.BENEFICIAL, 0XFFA4EB);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.parse("alexscaves:sugar_rush_speed"), 0.7F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.parse("alexscaves:sugar_rush_attack"), 0.5F, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration > 0;
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity entity, int amplifier) {
        return true;
    }

    /** The original slowed the server tick rate for everyone else through Citadel; the port gives the drinker speed instead. */
    public static void enterSlowMotion(Player entity, Level level, int duration, float speed) {
    }

    public static void leaveSlowMotion(Player entity, Level level) {
    }
}
''')
sub(S/'event/CommonEvents.java',r'import com\.github\.alexthe666\.citadel\.server\.tick\.ServerTickRateTracker;\n','')
sub(S/'event/CommonEvents.java',r'ServerTickRateTracker tracker = ServerTickRateTracker\.getForServer\(event\.getServer\(\)\);\s*tracker\.tickRateModifierList\.clear\(\);','')
# recipe cave map
sub(S/'recipe/RecipeCaveMap.java',r'import com\.github\.alexthe666\.citadel\.recipe\.SpecialRecipeInGuideBook;\n','')
sub(S/'recipe/RecipeCaveMap.java',r' implements SpecialRecipeInGuideBook','')
# capes
(S/'misc/ACPlayerCapes.java').unlink()
for p in S.rglob('*.java'):
    s=p.read_text()
    if 'ACPlayerCapes.setup();' in s:
        p.write_text(re.sub(r'\s*ACPlayerCapes\.setup\(\);','',s))
# lectern book
sub(S/'item/ACItemRegistry.java',r'import com\.github\.alexthe666\.citadel\.server\.block\.LecternBooks;\n','')
sub(S/'item/ACItemRegistry.java',r'\s*LecternBooks\.BOOKS\.put\([^;]*;','')
# cave book progress: no packet
p=S/'misc/CaveBookProgress.java'
s=p.read_text()
s=s.replace('import com.github.alexthe666.citadel.server.message.PropertiesMessage;\n','')
s=re.sub(r'PacketDistributor\.sendToPlayersTrackingEntityAndSelf\(player, new PropertiesMessage\([^;]*;','',s)
s=re.sub(r'PacketDistributor\.sendToServer\(new PropertiesMessage\([^;]*;','',s)
p.write_text(s)
# packet tag
p=S/'message/UpdateCaveBiomeMapTagMessage.java'
s=p.read_text()
s=s.replace('import com.github.alexthe666.citadel.server.message.PacketBufferUtils;\n','')
s=s.replace('PacketBufferUtils.readTag(buf)','(net.minecraft.nbt.CompoundTag) buf.readNbt()').replace('PacketBufferUtils.writeTag(buf, message.tag)','buf.writeNbt(message.tag)')
p.write_text(s)
# luxtructosaurus reach check with vanilla navigation
p=S/'entity/ai/LuxtructosaurusMeleeGoal.java'
s=p.read_text()
s=s.replace('import com.github.alexthe666.citadel.server.entity.pathfinding.raycoms.AdvancedPathNavigate;\n','').replace('import com.github.alexthe666.citadel.server.entity.pathfinding.raycoms.PathResult;\n','')
s=s.replace('PathResult path = ((AdvancedPathNavigate) this.luxtructosaurus.getNavigation()).moveToLivingEntity(target, 1.0D);\n            if (path == null || path.getPath() == null) {','net.minecraft.world.level.pathfinder.Path path = this.luxtructosaurus.getNavigation().createPath(target, 0);\n            if (path == null) {')
s=s.replace('path.getPath().getEndNode()','path.getEndNode()')
p.write_text(s)
