#!/usr/bin/env python3
"""Item API conversions: tooltip consumer, releaseUsing/hurtEnemy wrappers, cooldowns."""
import re, pathlib, sys
n=0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s=p.read_text(); o=s
    m=re.search(r'java\.util\.function\.Consumer<Component> (\w+), TooltipFlag',s)
    if m:
        name=m.group(1)
        # only inside appendHoverText: crude approach, replace in whole file for that param name
        s=re.sub(r'\b%s\.add\('%name, name+'.accept(', s)
    s=re.sub(r'((?:@Override\s*)?)public void releaseUsing\(ItemStack (\w+), Level (\w+), LivingEntity (\w+), int (\w+)\) \{',
        lambda m:'@Override\n    public boolean releaseUsing(ItemStack %s, Level %s, LivingEntity %s, int %s) {\n        releaseUsingImpl(%s, %s, %s, %s);\n        return true;\n    }\n\n    private void releaseUsingImpl(ItemStack %s, Level %s, LivingEntity %s, int %s) {'%((m.group(2),m.group(3),m.group(4),m.group(5))*3), s)
    s=re.sub(r'((?:@Override\s*)?)public boolean hurtEnemy\(ItemStack (\w+), LivingEntity (\w+), LivingEntity (\w+)\) \{',
        lambda m:'@Override\n    public void hurtEnemy(ItemStack %s, LivingEntity %s, LivingEntity %s) {\n        hurtEnemyImpl(%s, %s, %s);\n    }\n\n    private boolean hurtEnemyImpl(ItemStack %s, LivingEntity %s, LivingEntity %s) {'%((m.group(2),m.group(3),m.group(4))*3), s)
    s=s.replace('addCooldown(this,','addCooldown(new ItemStack(this),')
    if s!=o: p.write_text(s); n+=1
print(n,'files')
