execute unless entity @a[predicate=backrooms:in_backrooms] run return run schedule function backrooms:entity/entitycommands2 20t
execute at @e[tag=fallenangel] run advancement grant @a[distance=..10] only backrooms:entities fallenangel
execute at @e[tag=fallenangel] run advancement grant @a[distance=..10] only backrooms:true_explorer fallenangel
execute as @e[tag=radiodemon] at @e[tag=radiodemon] unless block ~ ~1 ~ minecraft:black_concrete run kill @s
execute at @e[tag=bomberplane] run advancement grant @a[distance=..45] only backrooms:entities/unnumbered/bomberplane
execute at @e[tag=bomberplane] run advancement grant @a[distance=..45] only backrooms:unnumberedentities bomberplane
execute at @e[tag=bomberplane] run advancement grant @a[distance=..45] only backrooms:entities bomberplane
execute at @e[tag=bomberplane] run advancement grant @a[distance=..45] only backrooms:true_explorer bomberplane
execute as @a store result score @s luckycranes run clear @s minecraft:music_disc_11[item_model="backrooms:lucky_crane"] 0
execute as @a if score @s luckycranes matches 0 run attribute @s minecraft:luck base set 0
execute as @a if score @s luckycranes matches 1 run attribute @s minecraft:luck base set -2
execute as @a if score @s luckycranes matches 2 run attribute @s minecraft:luck base set -4
execute as @a if score @s luckycranes matches 3 run attribute @s minecraft:luck base set -6
execute as @a if score @s luckycranes matches 4 run attribute @s minecraft:luck base set -8
execute as @a if score @s luckycranes matches 5 run attribute @s minecraft:luck base set -10
execute as @a if score @s luckycranes matches 6 run attribute @s minecraft:luck base set -12
execute as @a if score @s luckycranes matches 7 run attribute @s minecraft:luck base set -14
execute as @a if score @s luckycranes matches 8 run attribute @s minecraft:luck base set -16
execute as @a if score @s luckycranes matches 9 run attribute @s minecraft:luck base set -18
execute as @a if score @s luckycranes matches 10 run attribute @s minecraft:luck base set -20
execute as @a if score @s luckycranes matches 11 run attribute @s minecraft:luck base set -22
execute as @a if score @s luckycranes matches 12 run attribute @s minecraft:luck base set -24
execute as @a if score @s luckycranes matches 13 run attribute @s minecraft:luck base set -26
execute as @a if score @s luckycranes matches 14 run attribute @s minecraft:luck base set -28
execute as @a if score @s luckycranes matches 15 run attribute @s minecraft:luck base set -30
execute as @a if score @s luckycranes matches 16 run attribute @s minecraft:luck base set -32
execute as @a if score @s luckycranes matches 17 run attribute @s minecraft:luck base set -34
execute as @a if score @s luckycranes matches 18 run attribute @s minecraft:luck base set -36
execute as @a if score @s pinklarvae matches 1.. run advancement grant @s only backrooms:entities pinklarvae
execute as @a if score @s clockroaches matches 1.. run advancement grant @s only backrooms:entities clockroach
execute at @e[tag=biopipe] run advancement grant @a[distance=..2] only backrooms:entities/unnumbered/thebiopipe
execute at @e[tag=stormykat] as @e[distance=..10] run effect give @n[tag=stormykat] minecraft:instant_health 1 2 true
execute at @e[tag=tornado1] unless entity @e[type=minecraft:item_display,distance=..4] run summon item_display ~ ~1 ~ {transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[4f,4f,4f]},item:{id:"minecraft:bow",count:1,components:{"minecraft:item_model":"backrooms:tornado"}}}
execute at @e[tag=tornado1] unless entity @e[type=minecraft:item_display,tag=tornado1,distance=..4] run tag @n[type=minecraft:item_display] add tornado1
execute at @e[tag=tornado1,type=!item_display] if entity @e[type=minecraft:item_display,tag=tornado1,distance=..4] run tp @n[type=minecraft:item_display,tag=tornado1] ~ ~2 ~
execute at @e[type=minecraft:item_display,tag=tornado1] run tp @n[type=minecraft:item_display,tag=tornado1] ~ ~ ~ ~30 0
execute at @e[type=minecraft:item_display,tag=tornado1] unless entity @e[tag=tornado1,distance=..4,type=!minecraft:item_display] run kill @n[type=minecraft:item_display,tag=tornado1]
execute at @e[tag=tornado1,type=!minecraft:item_display] run fill ~1 ~4 ~1 ~-1 ~ ~-1 air destroy
execute at @e[tag=tornado1,type=!minecraft:item_display] as @a[distance=..4] run damage @s 1 backrooms:tornado by @s from @n[tag=tornado1]
execute as @e[tag=tornado1,type=!minecraft:item_display] if score #server backroomsrandomnumber2 matches ..3 run function backrooms:lowertornado/tornado1
execute at @e[tag=tornado5] unless entity @e[type=minecraft:item_display,distance=..10] run summon item_display ~ ~1 ~ {transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[16f,16f,16f]},item:{id:"minecraft:bow",count:1,components:{"minecraft:item_model":"backrooms:tornado"}}}
execute at @e[tag=tornado5] unless entity @e[type=minecraft:item_display,tag=tornado5,distance=..10] run tag @n[type=minecraft:item_display] add tornado5
execute at @e[tag=tornado5,type=!item_display] if entity @e[type=minecraft:item_display,tag=tornado5,distance=..10] run tp @n[type=minecraft:item_display,tag=tornado5] ~ ~8 ~
execute at @e[type=minecraft:item_display,tag=tornado5] run tp @n[type=minecraft:item_display,tag=tornado5] ~ ~ ~ ~100 0
execute at @e[type=minecraft:item_display,tag=tornado5] unless entity @e[tag=tornado5,distance=..10,type=!minecraft:item_display] run kill @n[type=minecraft:item_display,tag=tornado5]
execute as @e[tag=tornado5,type=!item_display] if score #server backroomsrandomnumber2 matches ..5 run function backrooms:lowertornado/tornado5
execute at @e[tag=tornado5,type=!minecraft:item_display] run fill ~7 ~16 ~7 ~-7 ~ ~-7 air destroy
execute at @e[tag=tornado5,type=!minecraft:item_display] as @a[distance=..16] run damage @s 5 backrooms:tornado by @s from @n[tag=tornado5]
schedule function backrooms:entity/entitycommands3 1t