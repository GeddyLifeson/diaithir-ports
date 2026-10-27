execute unless entity @a[predicate=backrooms:in_backrooms] run return run schedule function backrooms:entity/entitycommands4 20t
execute as @e[tag=thenucleus] at @e[tag=thenucleus] unless block ~ ~ ~ minecraft:oak_planks run kill @s
execute as @e[tag=light] at @e[tag=light] unless block ~ ~ ~ minecraft:torch run kill @s
execute as @a if score @s diseasetimer matches 10..24000 run scoreboard players set @s diseasestage 1
execute as @a if score @s diseasetimer matches 24000..72000 run scoreboard players set @s diseasestage 2
execute as @a if score @s diseasetimer matches 72000..120000 run scoreboard players set @s diseasestage 3
execute as @a if score @s diseasetimer matches 120000..168000 run scoreboard players set @s diseasestage 4
execute as @a if score @s diseasetimer matches 168000.. run scoreboard players set @s hasdisease 0
execute as @a if score @s diseasetimer matches 168000.. run scoreboard players set @s diseasetimer 0
execute as @a if score @s diseasetimer matches 168000.. run scoreboard players set @s diseasestage 0
execute as @a if predicate backrooms:dead run scoreboard players set @s diseasetimer 0
execute as @a if predicate backrooms:dead run scoreboard players set @s hasdisease 0
execute as @a if predicate backrooms:dead run scoreboard players set @s diseasestage 0
execute as @a if score @s hasdisease matches 1 run advancement grant @s only backrooms:entities/thedisease
execute as @a if score @s hasdisease matches 1 run advancement grant @s only backrooms:entities thedisease
execute as @a if score @s hasdisease matches 1 run advancement grant @s only backrooms:true_explorer thedisease
execute as @e[type=minecraft:villager,predicate=backrooms:inbackrooms,tag=!human] unless data entity @s CustomName run tag @s add human
scoreboard players set @a neardenizen 0
execute at @e[tag=denizen] as @a[distance=..15] if biome ~ ~ ~ backrooms:level139_5 run scoreboard players set @s neardenizen 1
execute at @e[tag=denizen] run advancement grant @a[distance=..15] only backrooms:entities/denizen
execute at @e[tag=denizen] run advancement grant @a[distance=..15] only backrooms:entities denizen
execute at @e[tag=denizen] run advancement grant @a[distance=..15] only backrooms:true_explorer denizen
execute as @e[tag=landlord] at @e[tag=landlord] unless predicate backrooms:inshady1 run tp @s @n[tag=shady1entityspawn]
execute at @e[tag=biopipe] run advancement grant @a[distance=..2] only backrooms:true_explorer biopipe
execute at @e[tag=tornado3] unless entity @e[type=minecraft:item_display,distance=..6] run summon item_display ~ ~1 ~ {transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[8f,8f,8f]},item:{id:"minecraft:bow",count:1,components:{"minecraft:item_model":"backrooms:tornado"}}}
execute at @e[tag=tornado3] unless entity @e[type=minecraft:item_display,tag=tornado3,distance=..6] run tag @n[type=minecraft:item_display] add tornado3
execute at @e[tag=tornado3,type=!item_display] if entity @e[type=minecraft:item_display,tag=tornado3,distance=..6] run tp @n[type=minecraft:item_display,tag=tornado3] ~ ~4 ~
execute at @e[type=minecraft:item_display,tag=tornado3] run tp @n[type=minecraft:item_display,tag=tornado3] ~ ~ ~ ~70 0
execute at @e[type=minecraft:item_display,tag=tornado3] unless entity @e[tag=tornado3,distance=..6,type=!minecraft:item_display] run kill @n[type=minecraft:item_display,tag=tornado3]
execute at @e[tag=tornado3,type=!minecraft:item_display] run fill ~3 ~8 ~3 ~-3 ~ ~-3 air destroy
execute at @e[tag=tornado3,type=!minecraft:item_display] as @a[distance=..8] run damage @s 3 backrooms:tornado by @s from @n[tag=tornado3]
execute as @e[tag=tornado3,type=!item_display] if score #server backroomsrandomnumber2 matches ..3 run function backrooms:lowertornado/tornado3
schedule function backrooms:entity/entitycommands1 1t