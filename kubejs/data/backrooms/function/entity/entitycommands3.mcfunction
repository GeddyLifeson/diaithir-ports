execute unless entity @a[predicate=backrooms:loop_needed] run return run schedule function backrooms:entity/entitycommands3 20t
execute as @a if score @s luckycranes matches 18 run attribute @s minecraft:luck base set -36
execute as @a if score @s luckycranes matches 19 run attribute @s minecraft:luck base set -38
execute as @a if score @s luckycranes matches 20 run attribute @s minecraft:luck base set -40
execute as @a if score @s luckycranes matches 21 run attribute @s minecraft:luck base set -42
execute as @a if score @s luckycranes matches 22 run attribute @s minecraft:luck base set -44
execute as @a if score @s luckycranes matches 23 run attribute @s minecraft:luck base set -46
execute as @a if score @s luckycranes matches 24 run attribute @s minecraft:luck base set -48
execute as @a if score @s luckycranes matches 25 run attribute @s minecraft:luck base set -50
execute as @a if score @s luckycranes matches 26 run attribute @s minecraft:luck base set -52
execute as @a if score @s luckycranes matches 27 run attribute @s minecraft:luck base set -54
execute as @a if score @s luckycranes matches 28 run attribute @s minecraft:luck base set -56
execute as @a if score @s luckycranes matches 29 run attribute @s minecraft:luck base set -58
execute as @a if score @s luckycranes matches 30 run attribute @s minecraft:luck base set -60
execute as @a if score @s luckycranes matches 31 run attribute @s minecraft:luck base set -62
execute as @a if score @s luckycranes matches 32 run attribute @s minecraft:luck base set -64
execute as @a if score @s luckycranes matches 33 run attribute @s minecraft:luck base set -66
execute as @a if score @s luckycranes matches 34 run attribute @s minecraft:luck base set -68
execute as @a if score @s luckycranes matches 35 run attribute @s minecraft:luck base set -70
execute as @a if score @s luckycranes matches 36 run attribute @s minecraft:luck base set -72
execute as @a if score @s luckycranes matches 37 run attribute @s minecraft:luck base set -74
execute at @e[tag=warningkite] run advancement grant @a[distance=..45] only backrooms:entities/warningkite
execute at @e[tag=warningkite] run advancement grant @a[distance=..45] only backrooms:entities warningkite
execute at @e[tag=warningkite] run advancement grant @a[distance=..45] only backrooms:true_explorer warningkite
execute at @e[tag=elevator] run advancement grant @a[distance=..5] only backrooms:entities/elevator
execute at @e[tag=elevator] run advancement grant @a[distance=..5] only backrooms:entities elevator
execute at @e[tag=elevator] run advancement grant @a[distance=..5] only backrooms:true_explorer elevator
execute as @a if score @s pinklarvae matches 1.. run advancement grant @s only backrooms:true_explorer pinklarvae
execute as @a if score @s clockroaches matches 1.. run advancement grant @s only backrooms:true_explorer clockroach
execute at @e[tag=biopipe] run advancement grant @a[distance=..2] only backrooms:entities biopipe
execute at @e[tag=tornado2] unless entity @e[type=minecraft:item_display,distance=..5] run summon item_display ~ ~1 ~ {transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[6f,6f,6f]},item:{id:"minecraft:bow",count:1,components:{"minecraft:item_model":"backrooms:tornado"}}}
execute at @e[tag=tornado2] unless entity @e[type=minecraft:item_display,tag=tornado2,distance=..5] run tag @n[type=minecraft:item_display] add tornado2
execute at @e[tag=tornado2,type=!item_display] if entity @e[type=minecraft:item_display,tag=tornado2,distance=..5] run tp @n[type=minecraft:item_display,tag=tornado2] ~ ~3 ~
execute at @e[type=minecraft:item_display,tag=tornado2] run tp @n[type=minecraft:item_display,tag=tornado2] ~ ~ ~ ~50 0
execute at @e[type=minecraft:item_display,tag=tornado2] unless entity @e[tag=tornado2,distance=..5,type=!minecraft:item_display] run kill @n[type=minecraft:item_display,tag=tornado2]
execute at @e[tag=tornado2,type=!minecraft:item_display] run fill ~2 ~6 ~2 ~-2 ~ ~-2 air destroy
execute at @e[tag=tornado2,type=!minecraft:item_display] as @a[distance=..6] run damage @s 2 backrooms:tornado by @s from @n[tag=tornado2]
execute as @e[tag=tornado2,type=!item_display] if score #server backroomsrandomnumber2 matches ..3 run function backrooms:lowertornado/tornado2
schedule function backrooms:entity/entitycommands4 1t