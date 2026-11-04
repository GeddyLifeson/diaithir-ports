# Generates blockstates/models/items/lang/loot for Nature's Spirit ids from templates (own JSON, vanilla parents). Existing files are kept.
import os,glob,json,re,sys
SRCG='/tmp/ns/src/src/main/generated/assets/natures_spirit'
R=sys.argv[1]  # module src/main/resources
A=R+'/assets/natures_spirit'; D=R+'/data/natures_spirit'
tex=set(os.path.basename(p)[:-4] for p in glob.glob(A+'/textures/block/*.png'))
ids=sorted(os.path.basename(f)[:-5] for f in glob.glob(SRCG+'/blockstates/*.json'))
def W(path,obj):
    if os.path.exists(path): return False
    os.makedirs(os.path.dirname(path),exist_ok=True); open(path,'w').write(json.dumps(obj,separators=(', ',': ')) if not isinstance(obj,str) else obj); return True
def T(*c):
    for x in c:
        if x in tex: return x
    return c[0]
def base(n,suf):
    b=n[:-len(suf)]
    return T(b+'_planks',b,b+'s',b+'_block',b+'_bricks',b.rstrip('s')+'s',b+'_mosaic',n)
def bm(name,parent,textures): return ('block/'+name,{"parent":parent,"textures":{k:'natures_spirit:block/'+v for k,v in textures.items()}})
made=0; lang={}
def add(path,obj):
    global made; made+=W(path,obj)
for n in ids:
    ln=n.replace('_',' ').title().replace("'S","'s"); lang['block.natures_spirit.'+n]=ln; lang['item.natures_spirit.'+n]=ln
    mods=[]; st=None
    def cube(name,t): mods.append(bm(name,'minecraft:block/cube_all',{'all':t}))
    sfx=next((s for s in ('_hanging_sign','_wall_hanging_sign','_wall_sign','_sign','_stairs','_slab','_wall','_fence_gate','_fence','_trapdoor','_door','_button','_pressure_plate','_log','_wood','_leaves','_sapling','_vines_plant','_vines','_lantern') if n.endswith(s)),None)
    if sfx in ('_sign','_hanging_sign','_wall_sign','_wall_hanging_sign'):
        w=n[:-len(sfx)]; t=T(w+'_planks'); mods.append(bm(n,'minecraft:block/particle_only' if False else 'minecraft:block/cube_all',{'all':t}) if False else bm(n,'minecraft:block/block',{'particle':'natures_spirit:block/'+t}) if False else ('block/'+n,{"textures":{"particle":'natures_spirit:block/'+t}}))
        st={"variants":{"":{"model":'natures_spirit:block/'+n}}}
        if 'standing' not in n and sfx in('_sign','_hanging_sign') : st={"variants":{"":{"model":'natures_spirit:block/'+n}}}
    elif sfx=='_stairs':
        t=base(n,sfx)
        for k,p in (('','stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')): mods.append(bm(n+k,'minecraft:block/'+p,{'bottom':t,'top':t,'side':t}))
        st={"variants":{f"facing={f},half={h},shape={s}":dict(model='natures_spirit:block/'+n+{'straight':'','inner_left':'_inner','inner_right':'_inner','outer_left':'_outer','outer_right':'_outer'}[s],**({} if True else {})) for f in('east','south','west','north') for h in('bottom','top') for s in('straight','inner_left','inner_right','outer_left','outer_right')}}
        ang={'east':0,'south':90,'west':180,'north':270}
        for k,v in st['variants'].items():
            f=re.search('facing=(\w+)',k).group(1);h=re.search('half=(\w+)',k).group(1);s=re.search('shape=(\w+)',k).group(1)
            y=ang[f]; 
            if s in('inner_left','outer_left'): y-=90
            if h=='top' and s in ('straight','inner_right','outer_right'): y+=0
            if h=='top': v['x']=180; y=(y+90) if s in('inner_left','outer_left') else y
            y%=360
            if y: v['y']=y
            if h=='top': v['uvlock']=True
    elif sfx=='_slab':
        t=base(n,sfx); b=T(n[:-5]+'_planks',n[:-5],n[:-5]+'s',n[:-5]+'_bricks',n[:-5]+'_block')
        mods+= [bm(n,'minecraft:block/slab',{'bottom':t,'top':t,'side':t}),bm(n+'_top','minecraft:block/slab_top',{'bottom':t,'top':t,'side':t})]
        st={"variants":{"type=bottom":{"model":'natures_spirit:block/'+n},"type=top":{"model":'natures_spirit:block/'+n+'_top'},"type=double":{"model":'natures_spirit:block/'+t}}}
        if t not in tex: pass
    elif sfx=='_wall':
        t=base(n,sfx)
        mods+=[bm(n+'_post','minecraft:block/template_wall_post',{'wall':t}),bm(n+'_side','minecraft:block/template_wall_side',{'wall':t}),bm(n+'_side_tall','minecraft:block/template_wall_side_tall',{'wall':t}),bm(n+'_inventory','minecraft:block/wall_inventory',{'wall':t})]
        mp='natures_spirit:block/'+n
        st={"multipart":[{"when":{"up":"true"},"apply":{"model":mp+'_post'}}]+[{"when":{d:v},"apply":{"model":mp+('_side' if v=='low' else '_side_tall'),**({"y":y} if y else {}),"uvlock":True}} for d,y in(('north',0),('east',90),('south',180),('west',270)) for v in('low','tall')]}
    elif sfx in('_fence','_fence_gate'):
        t=base(n,sfx)
        if sfx=='_fence':
            mods+=[bm(n+'_post','minecraft:block/fence_post',{'texture':t}),bm(n+'_side','minecraft:block/fence_side',{'texture':t}),bm(n+'_inventory','minecraft:block/fence_inventory',{'texture':t})]
            mp='natures_spirit:block/'+n
            st={"multipart":[{"apply":{"model":mp+'_post'}}]+[{"when":{d:"true"},"apply":{"model":mp+'_side',**({"y":y} if y else {}),"uvlock":True}} for d,y in(('north',0),('east',90),('south',180),('west',270))]}
        else:
            for k,p in (('','template_fence_gate'),('_open','template_fence_gate_open'),('_wall','template_fence_gate_wall'),('_wall_open','template_fence_gate_wall_open')): mods.append(bm(n+k,'minecraft:block/'+p,{'texture':t}))
            mp='natures_spirit:block/'+n
            st={"variants":{f"facing={f},in_wall={w},open={o}":{"model":mp+('_wall' if w=='true' else '')+('_open' if o=='true' else ''),"uvlock":True,**({"y":y} if y else {})} for f,y in(('south',0),('west',90),('north',180),('east',270)) for w in('false','true') for o in('false','true')}}
    elif sfx=='_trapdoor':
        t=T(n,n[:-9]+'_trapdoor')
        for k,p in (('_bottom','template_orientable_trapdoor_bottom'),('_top','template_orientable_trapdoor_top'),('_open','template_orientable_trapdoor_open')): mods.append(bm(n+k,'minecraft:block/'+p,{'texture':t}))
        mp='natures_spirit:block/'+n; v={}
        for f,y in(('north',0),('south',180),('east',90),('west',270)):
            for h in('bottom','top'):
                v[f"facing={f},half={h},open=false"]={"model":mp+'_'+h,**({"y":y} if y else {})}
                o={"model":mp+'_open',**({"y":(y+180)%360} if (y+180)%360 else {})} if True else None
                v[f"facing={f},half={h},open=true"]={"model":mp+'_open',**({"y":(y+(180 if h=='top' else 0))%360} if (y+(180 if h=='top' else 0))%360 else {}),**({"x":0})}
        st={"variants":v}
    elif sfx=='_door':
        tb=T(n+'_bottom'); tt=T(n+'_top')
        for h in('bottom','top'):
            for hinge in('left','right'):
                for o in('','_open'): mods.append(bm(f'{n}_{h}_{hinge}{o}',f'minecraft:block/door_{h}_{hinge}{o}',{'bottom':tb,'top':tt}))
        mp='natures_spirit:block/'+n; v={}
        for f,y in(('east',0),('south',90),('west',180),('north',270)):
            for h in('lower','upper'):
                for hinge in('left','right'):
                    for o in('false','true'):
                        yy=y+(0 if o=='false' else (90 if hinge=='left' else 270)); yy%=360
                        v[f"facing={f},half={h},hinge={hinge},open={o}"]={"model":f"{mp}_{'bottom' if h=='lower' else 'top'}_{hinge}{'_open' if o=='true' else ''}",**({"y":yy} if yy else {})}
        st={"variants":v}
    elif sfx=='_button':
        t=base(n,sfx)
        mods+=[bm(n,'minecraft:block/button',{'texture':t}),bm(n+'_pressed','minecraft:block/button_pressed',{'texture':t}),bm(n+'_inventory','minecraft:block/button_inventory',{'texture':t})]
        mp='natures_spirit:block/'+n; v={}
        for fc,x in(('floor',0),('wall',90),('ceiling',180)):
            for f,y in(('north',0),('east',90),('south',180),('west',270)):
                for p in('false','true'):
                    yy=y if fc!='ceiling' else (y+180)%360
                    d={"model":mp+('_pressed' if p=='true' else '')}
                    if x: d["x"]=x
                    if yy: d["y"]=yy
                    if fc=='wall': d["uvlock"]=True
                    v[f"face={fc},facing={f},powered={p}"]=d
        st={"variants":v}
    elif n.endswith('_pressure_plate'):
        t=base(n,'_pressure_plate'); mods+=[bm(n,'minecraft:block/pressure_plate_up',{'texture':t}),bm(n+'_down','minecraft:block/pressure_plate_down',{'texture':t})]
        st={"variants":{"powered=false":{"model":'natures_spirit:block/'+n},"powered=true":{"model":'natures_spirit:block/'+n+'_down'}}}
    elif sfx in('_log','_wood') or n.endswith('_bundle'):
        t=T(n if sfx=='_log' else n[:-5]+'_log',n); top=T(t+'_top',t)
        mods.append(bm(n,'minecraft:block/cube_column',{'end':top,'side':t}))
        st={"variants":{"axis=y":{"model":'natures_spirit:block/'+n},"axis=z":{"model":'natures_spirit:block/'+n,"x":90},"axis=x":{"model":'natures_spirit:block/'+n,"x":90,"y":90}}}
    elif sfx=='_leaves': cube(n,T(n))
    elif sfx in('_sapling',) or n in ('azolla',) or n.startswith(('tall_','large_')) or sfx is None and (n in tex or n+'_top' in tex) and not re.search(r'(chalk|kaolin|chert|bricks|paper|block|ore|calcite|cheese|travertine|tile)',n):
        t=T(n,n+'_top')
        if n.startswith('potted_'):
            mods.append(bm(n,'minecraft:block/flower_pot_cross',{'plant':T(n[7:]),'dirt':'minecraft:block/dirt'} if False else {'plant':T(n[7:])}))
            mods[-1][1]['textures']['dirt']='minecraft:block/dirt'
        elif n.startswith('tall_') or n.startswith('large_') or n.endswith('_bush'):
            mods+=[bm(n+'_bottom','minecraft:block/cross',{'cross':T(n+'_bottom',n)}),bm(n+'_top','minecraft:block/cross',{'cross':T(n+'_top',n)})]
            st={"variants":{"half=lower":{"model":'natures_spirit:block/'+n+'_bottom'},"half=upper":{"model":'natures_spirit:block/'+n+'_top'}}}
        else: mods.append(bm(n,'minecraft:block/cross',{'cross':t}))
    elif n.startswith('potted_'):
        mods.append(bm(n,'minecraft:block/flower_pot_cross',{'plant':T(n[7:])})); mods[-1][1]['textures']['dirt']='minecraft:block/dirt'
    else:
        cube(n,T(n,n+'_top'))
    for name,obj in mods: add(A+'/models/'+name+'.json',obj)
    add(A+'/blockstates/'+n+'.json',st or {"variants":{"":{"model":'natures_spirit:block/'+n}}})
    # item model + 26.1 item definition
    nm='item/'+n; 
    if sfx in('_wall','_fence','_button'): add(A+'/models/'+nm+'.json',{"parent":'natures_spirit:block/'+n+'_inventory'})
    elif sfx in('_sapling','_door','_sign','_hanging_sign') or n in tex and False: add(A+'/models/'+nm+'.json',{"parent":"minecraft:item/generated","textures":{"layer0":'natures_spirit:item/'+n if os.path.exists(A+'/textures/item/'+n+'.png') else 'natures_spirit:block/'+T(n)}})
    elif n.startswith(('potted_',)) : continue
    else: add(A+'/models/'+nm+'.json',{"parent":'natures_spirit:block/'+n+('_bottom' if sfx is None and (n.startswith(('tall_','large_'))) else '')})
    add(A+'/items/'+n+'.json',{"model":{"type":"minecraft:model","model":'natures_spirit:item/'+n}})
    # loot: drop self (slabs double, doors lower half)
    if not n.startswith('potted_') or True:
        pool={"rolls":1,"entries":[{"type":"minecraft:item","name":'natures_spirit:'+n}],"conditions":[{"condition":"minecraft:survives_explosion"}]}
        add(D+'/loot_table/blocks/'+n+'.json',{"type":"minecraft:block","pools":[pool],"random_sequence":'natures_spirit:blocks/'+n})
add(A+'/lang/_en_us_new.json',json.dumps(lang,indent=1))
print('files written',made,'ids',len(ids))
