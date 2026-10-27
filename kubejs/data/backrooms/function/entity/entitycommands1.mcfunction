execute unless entity @a[predicate=backrooms:loop_needed] run return run schedule function backrooms:entity/entitycommands1 20t
execute at @e[tag=thedisease] run effect give @a[distance=..50] minecraft:nausea 10 0 true
execute at @e[tag=thedisease] as @a[distance=..50] if score @s health matches ..10 run scoreboard players set @s hasdisease 1
execute as @a if score @s diseasestage matches 1 run effect give @s minecraft:slowness 2 0 true
execute as @a if score @s diseasestage matches 2.. run effect give @s minecraft:slowness 2 1 true
execute as @a if score @s diseasestage matches 3 unless score @s health matches ..10 run effect give @s minecraft:wither 2 0 true
execute as @a if score @s diseasestage matches 4 unless predicate backrooms:diseasedamagestop run effect give @s minecraft:wither 2 1 true
execute as @e[tag=carballoon] at @e[tag=carballoon] unless block ~ ~1 ~ minecraft:red_concrete run kill @s
execute as @e[tag=jumboslice] at @e[tag=jumboslice] unless block ~ ~1 ~ minecraft:lava run kill @s
execute as @e[tag=lighter] at @e[tag=lighter] unless block ~ ~1 ~ minecraft:sea_lantern run kill @s
execute as @e[tag=warningkite] at @e[tag=warningkite] unless block ~ ~ ~ minecraft:red_wool run kill @s
execute as @e[tag=mother] at @e[tag=mother] if block ~ ~-1 ~ minecraft:air run kill @s
execute as @e[tag=denizen] at @e[tag=denizen] if block ~ ~-1 ~ minecraft:air run kill @s
execute as @e[tag=heart] at @e[tag=heart] unless block ~ ~1 ~ minecraft:nether_wart_block run kill @s
execute at @e[tag=foglighter] run advancement grant @a[distance=..20] only backrooms:entities/foglighter
execute as @e[tag=foglighter] at @e[tag=foglighter] unless block ~ ~1 ~ minecraft:glowstone run kill @s
execute at @e[tag=foglighter] run advancement grant @a[distance=..20] only backrooms:entities foglighter
execute at @e[tag=foglighter] run advancement grant @a[distance=..20] only backrooms:true_explorer foglighter
execute at @e[tag=thearmless] run advancement grant @a[distance=..75] only backrooms:entities/thearmless
execute at @e[tag=thearmless] run advancement grant @a[distance=..75] only backrooms:entities thearmless
execute at @e[tag=thearmless] run advancement grant @a[distance=..75] only backrooms:true_explorer thearmless
execute as @e[tag=phobia] at @e[tag=phobia] unless block ~ ~1 ~ minecraft:black_concrete run kill @s
execute at @e[tag=phobia] run effect give @a[distance=..45] minecraft:slowness 2 0 true
execute at @e[tag=phobia] run advancement grant @a[distance=..45] only backrooms:entities/phobia
execute at @e[tag=phobia] run advancement grant @a[distance=..45] only backrooms:entities phobia
execute at @e[tag=phobia] run advancement grant @a[distance=..45] only backrooms:true_explorer phobia
execute at @e[tag=fallenangel] run advancement grant @a[distance=..10] only backrooms:entities/unnumbered/fallenangel
execute as @a if score @s pinklarvae matches 1.. run advancement grant @s only backrooms:entities/pinklarvae
execute as @a if score @s clockroaches matches 1.. run advancement grant @s only backrooms:entities/clockroach
execute at @e[tag=vendingmachineentity] run advancement grant @a[distance=..15] only backrooms:entities/vendingmachineentity
execute at @e[tag=vendingmachineentity] run advancement grant @a[distance=..15] only backrooms:entities vendingmachineentity
execute at @e[tag=vendingmachineentity] run advancement grant @a[distance=..15] only backrooms:true_explorer vendingmachineentity
execute at @e[tag=falsepuddle] if entity @e[type=item,nbt={Item:{id:"minecraft:music_disc_11",count:1,components:{"minecraft:item_model":"backrooms:liquid_pain"}}},distance=..2] run tag @n[type=item] add backroomsdie
execute at @e[tag=falsepuddle] if entity @e[type=item,nbt={Item:{id:"minecraft:music_disc_11",count:1,components:{"minecraft:item_model":"backrooms:liquid_pain"}}},distance=..2] run kill @n[tag=falsepuddle]
execute at @e[tag=naturekat] run place feature minecraft:single_piece_of_grass
execute at @e[tag=naturekat] run particle minecraft:happy_villager ~ ~ ~ 0.2 0.2 0.2 10 1
execute at @e[tag=tornado0] unless entity @e[type=minecraft:item_display,distance=..3] run summon item_display ~ ~1 ~ {transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[2f,2f,2f]},item:{id:"minecraft:bow",count:1,components:{"minecraft:item_model":"backrooms:tornado"}}}
execute at @e[tag=tornado0] unless entity @e[type=minecraft:item_display,tag=tornado0,distance=..3] run tag @n[type=minecraft:item_display] add tornado0
execute at @e[tag=tornado0,type=!item_display] if entity @e[type=minecraft:item_display,tag=tornado0,distance=..3] run tp @n[type=minecraft:item_display,tag=tornado0] ~ ~1 ~
execute at @e[type=minecraft:item_display,tag=tornado0] run tp @n[type=minecraft:item_display,tag=tornado0] ~ ~ ~ ~20 0
execute at @e[type=minecraft:item_display,tag=tornado0] unless entity @e[tag=tornado0,distance=..3,type=!minecraft:item_display] run kill @n[type=minecraft:item_display,tag=tornado0]
execute as @e[tag=tornado0] if score #server backroomsrandomnumber2 matches ..3 run function backrooms:lowertornado/tornado0
execute at @e[tag=tornado4] unless entity @e[type=minecraft:item_display,distance=..8] run summon item_display ~ ~1 ~ {transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[12f,12f,12f]},item:{id:"minecraft:bow",count:1,components:{"minecraft:item_model":"backrooms:tornado"}}}
execute at @e[tag=tornado4] unless entity @e[type=minecraft:item_display,tag=tornado4,distance=..8] run tag @n[type=minecraft:item_display] add tornado4
execute at @e[tag=tornado4,type=!item_display] if entity @e[type=minecraft:item_display,tag=tornado4,distance=..8] run tp @n[type=minecraft:item_display,tag=tornado4] ~ ~6 ~
execute at @e[type=minecraft:item_display,tag=tornado4] run tp @n[type=minecraft:item_display,tag=tornado4] ~ ~ ~ ~70 0
execute at @e[type=minecraft:item_display,tag=tornado4] unless entity @e[tag=tornado4,distance=..8,type=!minecraft:item_display] run kill @n[type=minecraft:item_display,tag=tornado4]
execute at @e[tag=tornado4,type=!minecraft:item_display] run fill ~5 ~12 ~5 ~-5 ~ ~-5 air destroy
execute at @e[tag=tornado4,type=!minecraft:item_display] as @a[distance=..12] run damage @s 4 backrooms:tornado by @s from @n[tag=tornado4]
execute as @e[tag=tornado4,type=!item_display] if score #server backroomsrandomnumber2 matches ..4 run function backrooms:lowertornado/tornado4
schedule function backrooms:entity/entitycommands2 1t