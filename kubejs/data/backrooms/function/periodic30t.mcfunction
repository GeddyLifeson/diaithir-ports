execute unless entity @a[predicate=backrooms:loop_needed] run return run schedule function backrooms:periodic30t 30t
execute at @e[type=!minecraft:player,predicate=backrooms:level3_5lightning,tag=!window] run summon minecraft:lightning_bolt
schedule function backrooms:periodic30t 30t
execute as @a at @a if biome ~ ~ ~ backrooms:level139_5 unless score @s neardenizen matches 1 run playsound minecraft:block.wood.step master @p
execute at @a[predicate=!backrooms:islowhealth] if score #server backroomsrandomnumber matches 0..25 if biome ~ ~ ~ backrooms:level1_1sector4 run tp @p ~ ~ ~-2
execute at @a[predicate=!backrooms:islowhealth] if score #server backroomsrandomnumber matches 26..50 if biome ~ ~ ~ backrooms:level1_1sector4 run tp @p ~ ~ ~2
execute at @a[predicate=!backrooms:islowhealth] if score #server backroomsrandomnumber matches 51..75 if biome ~ ~ ~ backrooms:level1_1sector4 run tp @p ~2 ~ ~
execute at @a[predicate=!backrooms:islowhealth] if score #server backroomsrandomnumber matches 76..100 if biome ~ ~ ~ backrooms:level1_1sector4 run tp @p ~-2 ~ ~
execute at @a unless block ~ ~-1 ~ minecraft:pink_concrete if biome ~ ~ ~ backrooms:level283pit run tp @p ~ ~-1 ~
execute at @a if biome ~ ~ ~ #backrooms:gray run title @p title [{font:"backrooms:colorfilter",text:"5",color:gray}]
execute at @a if biome ~ ~ ~ #backrooms:red run title @p title [{font:"backrooms:colorfilter",text:"4",color:red}]
execute at @a if biome ~ ~ ~ #backrooms:blue run title @p title [{font:"backrooms:colorfilter",text:"4",color:blue}]
execute at @a if entity @e[tag=tornado0,distance=..15] run title @p title [{text:"4",font:"backrooms:colorfilter",color:green}]
execute at @a if entity @e[tag=tornado1,distance=..25] run title @p title [{text:"4",font:"backrooms:colorfilter",color:green}]
execute at @a if entity @e[tag=tornado2,distance=..35] run title @p title [{text:"4",font:"backrooms:colorfilter",color:green}]
execute at @a if entity @e[tag=tornado3,distance=..50] run title @p title [{text:"4",font:"backrooms:colorfilter",color:green}]
execute at @a if entity @e[tag=tornado4,distance=..65] run title @p title [{text:"4",font:"backrooms:colorfilter",color:green}]
execute at @a if entity @e[tag=tornado5,distance=..75] run title @p title [{text:"4",font:"backrooms:colorfilter",color:green}]
execute at @a if biome ~ ~ ~ #backrooms:gray run title @p times 0 40t 0
execute at @a if biome ~ ~ ~ #backrooms:blue run title @p times 0 40t 0
execute at @a if entity @e[tag=tornado5,distance=..15] run title @p times 0 40t 0
execute at @a if entity @e[tag=tornado4,distance=..25] run title @p times 0 40t 0
execute at @a if entity @e[tag=tornado3,distance=..35] run title @p times 0 40t 0
execute at @a if entity @e[tag=tornado2,distance=..50] run title @p times 0 40t 0
execute at @a if entity @e[tag=tornado1,distance=..65] run title @p times 0 40t 0
execute at @a if entity @e[tag=tornado0,distance=..75] run title @p times 0 40t 0
execute at @a as @p if entity @e[tag=tornado5,distance=..15] run advancement grant @s only backrooms:pheno/unnumbered/suddengreening
execute at @a as @p if entity @e[tag=tornado4,distance=..25] run advancement grant @s only backrooms:pheno/unnumbered/suddengreening
execute at @a as @p if entity @e[tag=tornado3,distance=..35] run advancement grant @s only backrooms:pheno/unnumbered/suddengreening
execute at @a as @p if entity @e[tag=tornado2,distance=..50] run advancement grant @s only backrooms:pheno/unnumbered/suddengreening
execute at @a as @p if entity @e[tag=tornado1,distance=..65] run advancement grant @s only backrooms:pheno/unnumbered/suddengreening
execute at @a as @p if entity @e[tag=tornado0,distance=..75] run advancement grant @s only backrooms:pheno/unnumbered/suddengreening
execute at @a as @p if entity @e[tag=tornado5,distance=..15] run advancement grant @s only backrooms:pheno suddengreening
execute at @a as @p if entity @e[tag=tornado4,distance=..25] run advancement grant @s only backrooms:pheno suddengreening
execute at @a as @p if entity @e[tag=tornado3,distance=..35] run advancement grant @s only backrooms:pheno suddengreening
execute at @a as @p if entity @e[tag=tornado2,distance=..50] run advancement grant @s only backrooms:pheno suddengreening
execute at @a as @p if entity @e[tag=tornado1,distance=..65] run advancement grant @s only backrooms:pheno suddengreening
execute at @a as @p if entity @e[tag=tornado0,distance=..75] run advancement grant @s only backrooms:pheno suddengreening
execute at @a as @p if entity @e[tag=tornado5,distance=..15] run advancement grant @s only backrooms:true_explorer suddengreening
execute at @a as @p if entity @e[tag=tornado4,distance=..25] run advancement grant @s only backrooms:true_explorer suddengreening
execute at @a as @p if entity @e[tag=tornado3,distance=..35] run advancement grant @s only backrooms:true_explorer suddengreening
execute at @a as @p if entity @e[tag=tornado2,distance=..50] run advancement grant @s only backrooms:true_explorer suddengreening
execute at @a as @p if entity @e[tag=tornado1,distance=..65] run advancement grant @s only backrooms:true_explorer suddengreening
execute at @a as @p if entity @e[tag=tornado0,distance=..75] run advancement grant @s only backrooms:true_explorer suddengreening
execute at @a if biome ~ ~ ~ #backrooms:red unless entity @e[type=minecraft:glow_item_frame,tag=level14enter,distance=..15] run title @p times 0 40t 0
execute at @a if biome ~ ~ ~ #backrooms:red if entity @e[type=minecraft:glow_item_frame,tag=level14enter,distance=..15] run title @p times 0 0 0
execute at @a as @p if biome ~ ~ ~ backrooms:levelrun run playsound minecraft:block.bell.resonate ambient @s ~ ~ ~
execute at @a if dimension backrooms:levelrun if entity @e[tag=level14enter,distance=..15] run kill @e[tag=skinstealer,distance=..100]
execute at @a as @p if biome ~ ~ ~ backrooms:level7 if score #server backroomsrandomnumber matches 0..2 run advancement grant @s only backrooms:pheno/unnumbered/pipcomplex
execute at @a as @p if biome ~ ~ ~ backrooms:level7 if score #server backroomsrandomnumber matches 0..2 run advancement grant @s only backrooms:pheno pipcomplex
execute at @a as @p if biome ~ ~ ~ backrooms:level7 if score #server backroomsrandomnumber matches 0..2 run advancement grant @s only backrooms:true_explorer pipcomplex
execute at @a as @p if biome ~ ~ ~ backrooms:level7 if score #server backroomsrandomnumber matches 0..2 run title @s times 0t 50t 0t
execute at @a as @p if biome ~ ~ ~ backrooms:level7 if score #server backroomsrandomnumber matches 0..2 run title @s title {font:"backrooms:hallucination",text:"1"}
team join backroomsdev Aria976
team join backroomsdev BellFish2
team join backroomsdev Immanavocado
team join backroomsdev ryan101608
team join backroomsdev zumbozoss
team join backroomsdev xanebug
team join backroomsdev birdmhan
execute as @a at @a if predicate backrooms:inshady0 run advancement grant @s only backrooms:enigmatic/shady/shady0
execute as @a at @a if predicate backrooms:inshady1 run advancement grant @s only backrooms:enigmatic/shady/shady1
execute as @a at @a if predicate backrooms:inshady2 run advancement grant @s only backrooms:enigmatic/shady/shady2
execute as @a at @a if predicate backrooms:inshady3 run advancement grant @s only backrooms:enigmatic/shady/shady3
execute as @a at @a if predicate backrooms:inshady4 run advancement grant @s only backrooms:enigmatic/shady/shady4
execute as @a at @a if predicate backrooms:inshady5 run advancement grant @s only backrooms:enigmatic/shady/shady5
execute as @a at @a if predicate backrooms:inshady6 run advancement grant @s only backrooms:enigmatic/shady/shady6
execute as @a at @a if predicate backrooms:inshady7 run advancement grant @s only backrooms:enigmatic/shady/shady7
execute as @a at @a if predicate backrooms:inshady0 run advancement grant @s only backrooms:levels shady0
execute as @a at @a if predicate backrooms:inshady1 run advancement grant @s only backrooms:levels shady1
execute as @a at @a if predicate backrooms:inshady2 run advancement grant @s only backrooms:levels shady2
execute as @a at @a if predicate backrooms:inshady3 run advancement grant @s only backrooms:levels shady3
execute as @a at @a if predicate backrooms:inshady4 run advancement grant @s only backrooms:levels shady4
execute as @a at @a if predicate backrooms:inshady5 run advancement grant @s only backrooms:levels shady5
execute as @a at @a if predicate backrooms:inshady6 run advancement grant @s only backrooms:levels shady6
execute as @a at @a if predicate backrooms:inshady7 run advancement grant @s only backrooms:levels shady7
execute as @a at @a if predicate backrooms:inshady0 run advancement grant @s only backrooms:true_explorer shady0
execute as @a at @a if predicate backrooms:inshady1 run advancement grant @s only backrooms:true_explorer shady1run advancement grant @s only backrooms:true_explorer shady2
execute as @a at @a if predicate backrooms:inshady3 run advancement grant @s only backrooms:true_explorer shady3
execute as @a at @a if predicate backrooms:inshady4 run advancement grant @s only backrooms:true_explorer shady4
execute as @a at @a if predicate backrooms:inshady5 run advancement grant @s only backrooms:true_explorer shady5
execute as @a at @a if predicate backrooms:inshady6 run advancement grant @s only backrooms:true_explorer shady6
execute as @a at @a if predicate backrooms:inshady7 run advancement grant @s only backrooms:true_explorer shady7
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server footsteps matches 1.. run playsound minecraft:block.powder_snow.step
execute if score #server footsteps matches 1.. run scoreboard players add #server footsteps 1
execute if score #server footsteps matches 3.. run scoreboard players set #server footsteps 0
execute at @e[tag=stormykat] run execute at @e[distance=..10,tag=!stormykat,type=!lightning_bolt] run summon minecraft:lightning_bolt