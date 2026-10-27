execute unless entity @a[predicate=backrooms:in_backrooms] run return 0
execute at @a as @p if biome ~ ~ ~ #backrooms:p15 run effect give @p minecraft:invisibility 2 0 true
execute as @a if score @s rothstage matches 1.. run advancement grant @s only backrooms:pheno/rothsyndrome
execute as @a if score @s rothstage matches 1.. run advancement grant @s only backrooms:pheno rothsyndrome
execute as @a if score @s rothstage matches 1.. run advancement grant @s only backrooms:true_explorer rothsyndrome
execute at @a[predicate=!backrooms:lookingatrainman] as @n[tag=rainman] run attribute @s minecraft:movement_speed base set 0
execute at @a[predicate=backrooms:lookingatrainman] as @n[tag=rainman] run attribute @s minecraft:movement_speed base set 0.5
execute at @e[tag=car] unless entity @e[type=minecraft:item_display,distance=..8] run summon item_display ~ ~1 ~ {transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],translation:[0f,0f,0f],scale:[6f,6f,6f]},item:{id:"minecraft:bow",count:1,components:{"minecraft:item_model":"backrooms:car"}}}
execute at @e[tag=car] unless entity @e[type=minecraft:item_display,tag=car,distance=..8] run tag @n[type=minecraft:item_display] add car
execute at @e[tag=car,type=!item_display] if entity @e[type=minecraft:item_display,tag=car,distance=..8] run tp @n[type=minecraft:item_display,tag=car] ~ ~3 ~ ~ 0
execute at @e[type=minecraft:item_display,tag=car] unless entity @e[tag=car,distance=..3,type=!minecraft:item_display] run kill @n[type=minecraft:item_display,tag=car]
execute at @a as @p if entity @e[type=minecraft:item_display,distance=..20,tag=car] run advancement grant @s only backrooms:pheno/unnumbered/cars
execute at @a as @p if entity @e[type=minecraft:item_display,distance=..20,tag=car] run advancement grant @s only backrooms:pheno cars
execute at @a as @p if entity @e[type=minecraft:item_display,distance=..20,tag=car] run advancement grant @s only backrooms:true_explorer cars
execute if score #server backroomsrandomnumber matches 1 store result score #server p14 run random value 1..4
execute at @a as @p if score #server backroomsrandomnumber matches 1 if biome ~ ~ ~ #backrooms:p14 if score #server p14 matches 1 rotated ~ 0 run tp @s ^ ^ ^25
execute at @a as @p if score #server backroomsrandomnumber matches 1 if biome ~ ~ ~ #backrooms:p14 if score #server p14 matches 2 rotated ~ 0 run tp @s ^ ^ ^25
execute at @a as @p if biome ~ ~ ~ backrooms:level37_1 if block ~ ~ ~ minecraft:water run effect give @s minecraft:regeneration 10 1 true
execute at @a as @p if biome ~ ~ ~ backrooms:level37_1 if block ~ ~ ~ minecraft:water run scoreboard players add @s backroomssanity 10
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server blackoutstate matches 2 run effect give @s minecraft:darkness 10 0 true
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server blackoutstate matches 2 run advancement grant @s only backrooms:pheno/unnumbered/thegreatblackout
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server blackoutstate matches 2 run advancement grant @s only backrooms:pheno thegreatblackout
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server blackoutstate matches 2 run advancement grant @s only backrooms:true_explorer thegreatblackout
execute as @e[tag=mistman] at @s run tp @s ~ ~ ~ facing entity @p
execute at @e[tag=mistman] run damage @p[distance=..20] 5 minecraft:player_attack by @p from @n
execute at @e[tag=walldoor] run advancement grant @a[distance=..2] only backrooms:pheno/unnumbered/walldoor
execute at @e[tag=walldoor] run advancement grant @a[distance=..2] only backrooms:pheno walldoor
execute at @e[tag=walldoor] run advancement grant @a[distance=..2] only backrooms:true_explorer walldoor