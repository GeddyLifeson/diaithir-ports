#!/usr/bin/env python3
"""Foods, registry id plumbing (ACProps), armour, main-class payload registration. Run once on a fresh import."""
import re, pathlib, sys
ROOT=pathlib.Path('src/main/java/com/github/alexmodguy/alexscaves')
SRV=ROOT/'server'
AT='net.minecraft.world.item.equipment.ArmorType'
PI='com.github.alexmodguy.alexscaves.server.misc.ACProps.item()'

# ---- main class: payload registration and send helpers from upstream
p=ROOT/'AlexsCaves.java'; s=p.read_text()
orig=open('/home/user/src/ac/src/main/java/com/github/alexmodguy/alexscaves/AlexsCaves.java').read()
a=orig.index('    private void registerPayloads'); b=orig.index('    private void commonSetup')
c=orig.index('    public static <MSG extends CustomPacketPayload> void sendMSGToServer'); d=orig.index('    private void loadComplete')
e=orig.index('    public static <MSG extends CustomPacketPayload> void sendNonLocal'); f=orig.index('    private void registerLayerDefinitions')
imports='''import com.github.alexmodguy.alexscaves.server.message.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
'''
if 'registerPayloads' not in s:
    s=s.replace('import org.slf4j.Logger;',imports+'import org.slf4j.Logger;')
    s=s.replace('        // registries','        modEventBus.addListener(this::registerPayloads);\n        // registries')
    s=s.rstrip()[:-1]+'\n'+orig[a:b]+orig[c:d]+orig[e:f]+'}\n'
    p.write_text(s)

# ---- ACFoods
p=SRV/'item/ACFoods.java'; s=p.read_text()
if 'FoodProperties.Builder()' in s:
    out=['''package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

/** Foods in the 26.1 form: FoodProperties plus a Consumable carrying the status effects. */
public class ACFoods {
    public record Food(FoodProperties food, Consumable consumable, ItemStack remainder) {
        public net.minecraft.world.item.Item.Properties apply(net.minecraft.world.item.Item.Properties properties) {
            properties = properties.food(food, consumable);
            return remainder.isEmpty() ? properties : properties.usingConvertsTo(remainder.getItem());
        }
    }

    private static Food make(int nutrition, float sat, boolean always, MobEffectInstance effect, float prob, boolean bowl) {
        FoodProperties.Builder b = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(sat);
        if (always) b.alwaysEdible();
        Consumable.Builder c = Consumable.builder();
        if (effect != null) c.onConsume(new ApplyStatusEffectsConsumeEffect(effect, prob));
        return new Food(b.build(), c.build(), bowl ? new ItemStack(Items.BOWL) : ItemStack.EMPTY);
    }
''']
    for m in re.finditer(r'public static final FoodProperties (\w+) = \(new FoodProperties\.Builder\(\)\)(.*?)\.build\(\);',s):
        name,body=m.groups()
        nut=int(re.search(r'nutrition\((\d+)\)',body).group(1))
        sat=re.search(r'saturationModifier\(([\d.]+F)\)',body).group(1)
        e=re.search(r'effect\(\(\) -> new MobEffectInstance\(([\w.]+), (\d+)\), ([\d.]+F)\)',body)
        eff='new MobEffectInstance(%s, %s)'%(e.group(1),e.group(2)) if e else 'null'
        out.append('    public static final Food %s = make(%d, %s, %s, %s, %s, %s);'%(name,nut,sat,str('alwaysEdible()' in body).lower(),eff,e.group(3) if e else '1.0F',str('usingConvertsTo(Items.BOWL)' in body).lower()))
    out.append('}')
    p.write_text('\n'.join(out)+'\n')

# ---- id plumbing
for q in ROOT.rglob('*.java'):
    s=q.read_text(); o=s
    s=s.replace('new Item.Properties()',PI)
    s=re.sub(r'\bnew Properties\(\)',PI,s)
    s=s.replace('BlockBehaviour.Properties.ofFullCopy(','com.github.alexmodguy.alexscaves.server.misc.ACProps.blockCopy(')
    s=s.replace('BlockBehaviour.Properties.ofLegacyCopy(','com.github.alexmodguy.alexscaves.server.misc.ACProps.blockCopy(')
    s=s.replace('BlockBehaviour.Properties.of()','com.github.alexmodguy.alexscaves.server.misc.ACProps.block()')
    s=re.sub(r'\.food\(ACFoods\.(\w+)\)',r'.food(ACFoods.\1.food(), ACFoods.\1.consumable())',s)
    s=s.replace('ArmorItem.Type.',AT+'.')
    if s!=o: q.write_text(s)
p=SRV/'block/ACBlockRegistry.java'; s=p.read_text()
if 'ACProps.setBlock' not in s:
    s=re.sub(r'public static final BlockBehaviour\.Properties (\w+_PROPERTIES) = (.*);$', r'public static final java.util.function.Supplier<BlockBehaviour.Properties> \1 = () -> \2;', s, flags=re.M)
    s=re.sub(r'\b(\w+_PROPERTIES)\b(?!\s*=\s*\()(?!\.get)', r'\1.get()', s)
    s=s.replace('ACItemRegistry.DEF_REG.register(','ACItemRegistry.reg(')
    s=re.sub(r'(?<![\w.])DEF_REG\.register\(','reg(',s)
    helper='''
    private static <B extends Block> DeferredHolder<Block, B> reg(String name, java.util.function.Supplier<B> supplier) {
        return DEF_REG.register(name, () -> {
            com.github.alexmodguy.alexscaves.server.misc.ACProps.setBlock(name);
            try { return supplier.get(); } finally { com.github.alexmodguy.alexscaves.server.misc.ACProps.setBlock(null); }
        });
    }
'''
    i=s.index('    private static DeferredHolder<Block, Block> registerBlockAndItemEdible')
    s=s[:i]+helper+'\n'+s[i:]
    s=s.replace('.food(foodProperties)','.food(foodProperties.food(), foodProperties.consumable())').replace('FoodProperties foodProperties','ACFoods.Food foodProperties')
    p.write_text(s)
p=SRV/'item/ACItemRegistry.java'; s=p.read_text()
if 'ACProps.setItem' not in s:
    s=re.sub(r'(?<![\w.])DEF_REG\.register\(','reg(',s)
    helper='''
    public static <I extends Item> DeferredHolder<Item, I> reg(String name, java.util.function.Supplier<I> supplier) {
        return DEF_REG.register(name, () -> {
            com.github.alexmodguy.alexscaves.server.misc.ACProps.setItem(name);
            try { return supplier.get(); } finally { com.github.alexmodguy.alexscaves.server.misc.ACProps.setItem(null); }
        });
    }
'''
    i=s.index('    public static final DeferredRegister<Item> DEF_REG'); j=s.index('\n',i)+1
    s=s[:j]+helper+s[j:]; p.write_text(s)

# ---- armour classes
for q in SRV.rglob('*.java'):
    s=q.read_text(); o=s
    if 'extends ArmorItem' in s:
        s=s.replace('extends ArmorItem','extends Item')
        s=re.sub(r'(?<![\w.])Type\.(BOOTS|HELMET|CHESTPLATE|LEGGINGS)',AT+r'.\1',s)
        s=re.sub(r'(?<![\w.])Type (\w+)',AT+r' \1',s)
        s=re.sub(r'super\((\w+)\.getHolder\(\),\s*([\w.]+),\s*'+re.escape(PI)+r'\.durability\(\w+\.getDurabilityForType\([\w.]+\)\)',
                 lambda m:'super(%s.humanoidArmor(%s.material(), %s)'%(PI,m.group(1),m.group(2)),s)
        if 'import net.minecraft.world.item.Item;' not in s:
            s=re.sub(r'(package [\w.]+;\n)',r'\1\nimport net.minecraft.world.item.Item;',s,count=1)
    s=s.replace('import net.minecraft.world.item.ArmorItem;\n','')
    if s!=o: q.write_text(s)
