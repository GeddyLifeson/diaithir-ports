execute unless entity @a[predicate=backrooms:loop_needed] run return run schedule function backrooms:enter/enter0_100 20t
execute as @e[predicate=backrooms:invoid,type=!zombie] unless dimension backrooms:level4 run effect give @s slow_falling 5 0 true
execute as @a[predicate=backrooms:invoid] unless dimension backrooms:level4 unless score #server backroomsrandomnumber matches 0..20 run schedule function backrooms:locatesublevels/mainspawns/level0 10t
execute as @e[predicate=backrooms:invoid,type=!zombie] unless dimension backrooms:level4 unless score #server backroomsrandomnumber matches 0..20 run execute in backrooms:level0 run tp 0 16 0
execute at @a as @p if predicate backrooms:level7_43to0 run schedule function backrooms:locatesublevels/mainspawns/level0 10t
execute at @a as @p if predicate backrooms:level7_43to0 run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level0enter] as @a[tag=!level0enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level0 10t
execute at @e[tag=level0enter] as @e[type=#backrooms:canexit,tag=!level0enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level001enter] as @a[tag=!level001enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel01/level001 10t
execute at @e[tag=level001enter] as @e[type=#backrooms:canexit,tag=!level001enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level011enter] as @a[tag=!level011enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel11/level011 10t
execute at @e[tag=level011enter] as @e[type=#backrooms:canexit,tag=!level011enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level006enter] as @a[tag=!level006enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel06/level006 10t
execute at @e[tag=level006enter] as @e[type=#backrooms:canexit,tag=!level006enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level02enter] as @a[tag=!level02enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel2/level02 10t
execute at @e[tag=level02enter] as @e[type=#backrooms:canexit,tag=!level02enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level022enter] as @a[tag=!level022enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel22/level022 10t
execute at @e[tag=level022enter] as @e[type=#backrooms:canexit,tag=!level022enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level023enter] as @a[tag=!level023enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel23/level023 10t
execute at @e[tag=level023enter] as @e[type=#backrooms:canexit,tag=!level023enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level05enter] as @a[tag=!level05enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel5/level05 10t
execute at @e[tag=level05enter] as @e[type=#backrooms:canexit,tag=!level05enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level067enter] as @a[tag=!level067enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel67/level067 10t
execute at @e[tag=level067enter] as @e[type=#backrooms:canexit,tag=!level067enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level07enter] as @a[tag=!level07enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel7/level07 10t
execute at @e[tag=level07enter] as @e[type=#backrooms:canexit,tag=!level07enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level08enter] as @a[tag=!level08enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel8/level08 10t
execute at @e[tag=level08enter] as @e[type=#backrooms:canexit,tag=!level08enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level098enter] as @a[tag=!level098enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel98/level098 10t
execute at @e[tag=level098enter] as @e[type=#backrooms:canexit,tag=!level098enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=level099enter] as @a[tag=!level099enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel99/level099 10t
execute at @e[tag=level099enter] as @e[type=#backrooms:canexit,tag=!level099enter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute at @e[tag=dullnessenter] as @a[tag=!dullnessenter,distance=..2] run schedule function backrooms:locatesublevels/sublevel1/dullness 10t
execute at @e[tag=dullnessenter] as @e[type=#backrooms:canexit,tag=!dullnessenter,distance=..2] run execute in backrooms:level0 run tp 0 16 0
execute as @a[predicate=backrooms:invoid] unless dimension backrooms:level4 if score #server backroomsrandomnumber matches 0..10 run schedule function backrooms:locatesublevels/mainspawns/level1 10t
execute as @e[predicate=backrooms:invoid,type=!zombie] unless dimension backrooms:level4 if score #server backroomsrandomnumber matches 0..10 run execute in backrooms:level1 run tp 0 16 0
execute at @e[tag=level1enter] as @a[tag=!level1enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level1 10t
execute at @e[tag=level1enter] as @e[type=#backrooms:canexit,tag=!level1enter,distance=..2] run execute in backrooms:level1 run tp 0 16 0
execute as @a[predicate=backrooms:islowhealth] if biome ~ ~ ~ #backrooms:level993 if predicate backrooms:haswither run schedule function backrooms:locatesublevels/mainspawns/level1 10t
execute as @a[predicate=backrooms:islowhealth] if biome ~ ~ ~ #backrooms:level993 if predicate backrooms:haswither run execute in backrooms:level1 run tp 0 16 0
execute at @e[tag=level1_01enter] as @a[tag=!level1_01enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel01/level1_01 10t
execute at @e[tag=level1_01enter] as @e[type=#backrooms:canexit,tag=!level1_01enter,distance=..2] run execute in backrooms:level1 run tp 0 16 0
execute at @a if biome ~ ~ ~ #backrooms:level1_1 if score @p backroomsy matches 20.. run tp @p ~ 16 ~
execute at @e[tag=level1_11enter] as @a[tag=!level1_11enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel11/level1_11 10t
execute at @e[tag=level1_11enter] as @e[type=#backrooms:canexit,tag=!level1_11enter,distance=..2] run execute in backrooms:level1 run tp 0 16 0
execute at @a if biome ~ ~ ~ backrooms:level1_11 if score @p backroomsy matches 20.. run tp @p ~ 16 ~
execute at @e[tag=level1_3enter] as @a[tag=!level1_3enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel3/level1_3 10t
execute at @e[tag=level1_3enter] as @e[type=#backrooms:canexit,tag=!level1_3enter,distance=..2] run execute in backrooms:level1 run tp 0 16 0
execute at @e[tag=level1_31enter] as @a[tag=!level1_31enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel31/level1_31 10t
execute at @e[tag=level1_31enter] as @e[type=#backrooms:canexit,tag=!level1_31enter,distance=..2] run execute in backrooms:level1 run tp 0 16 0
execute at @a if biome ~ ~ ~ backrooms:level1_31 if score @p backroomsy matches 20.. run tp @p ~ 16 ~
execute at @e[tag=level1_5enter] as @a[tag=!level1_5enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel5/level1_5 10t
execute at @e[tag=level1_5enter] as @e[type=#backrooms:canexit,tag=!level1_5enter,distance=..2] run execute in backrooms:level1 run tp 0 16 0
execute at @a if biome ~ ~ ~ backrooms:level1_5 if score @p backroomsy matches 20.. run tp @p ~ 16 ~
execute at @e[tag=level1_51enter] as @a[tag=!level1_51enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel51/level1_51 10t
execute at @e[tag=level1_51enter] as @e[type=#backrooms:canexit,tag=!level1_51enter,distance=..2] run execute in backrooms:level1 run tp 0 16 0
execute at @a if biome ~ ~ ~ #backrooms:level1_51 if score @p backroomsy matches 20.. run tp @p ~ 16 ~
execute at @e[tag=levelphienter] as @a[tag=!levelphienter,distance=..2] run schedule function backrooms:locatesublevels/sublevel6/levelphi 10t
execute at @e[tag=levelphienter] as @e[type=#backrooms:canexit,tag=!levelphienter,distance=..2] run execute in backrooms:level1 run tp 0 16 0
execute as @a[predicate=backrooms:invoid] unless dimension backrooms:level4 if score #server backroomsrandomnumber matches 11..20 run schedule function backrooms:locatesublevels/mainspawns/level2 10t
execute as @e[predicate=backrooms:invoid,type=!zombie] unless dimension backrooms:level4 if score #server backroomsrandomnumber matches 11..20 run execute in backrooms:level2 run tp 0 16 0
execute at @e[tag=level2enter] as @a[tag=!level2enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level2 10t
execute at @e[tag=level2enter] as @e[type=#backrooms:canexit,tag=!level2enter,distance=..2] run execute in backrooms:level2 run tp 0 16 0
execute at @e[tag=level2_1enter] as @a[tag=!level2_1enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel1/level2_1 10t
execute at @e[tag=level2_1enter] as @e[type=#backrooms:canexit,tag=!level2_1enter,distance=..2] run execute in backrooms:level2 run tp 0 16 0
execute at @e[tag=level2_2enter] as @a[tag=!level2_2enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel2/level2_2 10t
execute at @e[tag=level2_2enter] as @e[type=#backrooms:canexit,tag=!level2_2enter,distance=..2] run execute in backrooms:level2 run tp 0 16 0
execute at @e[tag=officespaceenter] as @a[tag=!officespaceenter,distance=..2] run schedule function backrooms:locatesublevels/sublevel1/officespace 10t
execute at @e[tag=officespaceenter] as @e[type=#backrooms:canexit,tag=!officespaceenter,distance=..2] run execute in backrooms:level2 run tp 0 16 0
execute at @e[tag=level3enter] as @a[tag=!level3enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level3 10t
execute at @e[tag=level3enter] as @e[type=#backrooms:canexit,tag=!level3enter,distance=..2] run execute in backrooms:level3 run tp 0 16 0
execute at @e[tag=level3_1enter] as @a[tag=!level3_1enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel1/level3_1 10t
execute at @e[tag=level3_1enter] as @e[type=#backrooms:canexit,tag=!level3_1enter,distance=..2] run execute in backrooms:level3 run tp 0 16 0
execute at @e[tag=levelpienter] as @a[tag=!levelpienter,distance=..2] run schedule function backrooms:locatesublevels/sublevel14/levelpi 10t
execute at @e[tag=levelpienter] as @e[type=#backrooms:canexit,tag=!levelpienter,distance=..2] run execute in backrooms:level3 run tp 0 16 0
execute at @e[tag=level3_5enter] as @a[tag=!level3_5enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel5/level3_5 10t
execute at @e[tag=level3_5enter] as @e[type=#backrooms:canexit,tag=!level3_5enter,distance=..2] run execute in backrooms:level3 run tp 0 16 0
execute at @e[tag=level3_53enter] as @a[tag=!level3_53enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel53/level3_53 10t
execute at @e[tag=level3_53enter] as @e[type=#backrooms:canexit,tag=!level3_53enter,distance=..2] run execute in backrooms:level3 run tp 0 16 0
execute at @e[tag=level3_6enter] as @a[tag=!level3_6enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel6/level3_6 10t
execute at @e[tag=level3_6enter] as @e[type=#backrooms:canexit,tag=!level3_6enter,distance=..2] run execute in backrooms:level3 run tp 0 16 0
execute at @e[tag=level4enter] as @a[tag=!level4enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level4 10t
execute at @e[tag=level4enter] as @e[type=#backrooms:canexit,tag=!level4enter,distance=..2] run execute in backrooms:level4 run tp 0 16 0
execute at @a positioned ~ -130 ~ if dimension backrooms:level4 as @a[distance=..60] run schedule function backrooms:locatesublevels/mainspawns/level4 10t
execute at @e positioned ~ -130 ~ if dimension backrooms:level4 as @e[distance=..60] run execute in backrooms:level4 run tp 0 16 0
execute as @a[predicate=backrooms:invoid] if dimension backrooms:level4 if biome ~ ~ ~ backrooms:level4_3 if score #server backroomsrandomnumber matches 50..75 run schedule function backrooms:locatesublevels/mainspawns/level5 10t
execute as @e[predicate=backrooms:invoid,type=!zombie] if dimension backrooms:level4 if biome ~ ~ ~ backrooms:level4_3 if score #server backroomsrandomnumber matches 50..75 run execute in backrooms:level5 run tp 0 16 0
execute at @e[tag=level4_11enter] as @a[tag=!level4_11enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel11/level4_11 10t
execute at @e[tag=level4_11enter] as @e[type=#backrooms:canexit,tag=!level4_11enter,distance=..2] run execute in backrooms:level4 run tp 0 16 0
execute at @e[tag=level4_3enter] as @a[tag=!level4_3enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel3/level4_3 10t
execute at @e[tag=level4_3enter] as @e[type=#backrooms:canexit,tag=!level4_3enter,distance=..2] run execute in backrooms:level4 run tp 0 16 0
execute at @e[tag=level5enter] as @a[tag=!level5enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level5 10t
execute at @e[tag=level5enter] as @e[type=#backrooms:canexit,tag=!level5enter,distance=..2] run execute in backrooms:level5 run tp 0 16 0
execute at @e[tag=level5_11enter] as @a[tag=!level5_11enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel11/level5_11 10t
execute at @e[tag=level5_11enter] as @e[type=#backrooms:canexit,tag=!level5_11enter,distance=..2] run execute in backrooms:level5 run tp 0 16 0
execute at @e[tag=level5_21enter] as @a[tag=!level5_21enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel21/level5_21 10t
execute at @e[tag=level5_21enter] as @e[type=#backrooms:canexit,tag=!level5_21enter,distance=..2] run execute in backrooms:level5 run tp 0 16 0
execute at @e[tag=level5_31enter] as @a[tag=!level5_31enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel31/level5_31 10t
execute at @e[tag=level5_31enter] as @e[type=#backrooms:canexit,tag=!level5_31enter,distance=..2] run execute in backrooms:level5 run tp 0 16 0
execute as @a[predicate=backrooms:invoid] if dimension backrooms:level4 if biome ~ ~ ~ backrooms:level4_3 if score #server backroomsrandomnumber matches 76.. run schedule function backrooms:locatesublevels/sublevel31/level5_31 10t
execute as @e[predicate=backrooms:invoid,type=!zombie] if dimension backrooms:level4 if biome ~ ~ ~ backrooms:level4_3 if score #server backroomsrandomnumber matches 76.. run execute in backrooms:level5 run tp 0 16 0
execute at @e[tag=level5_55enter] as @e[type=#backrooms:canexit,tag=!level5_55enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel55/level5_55 10t
execute at @e[tag=level5_55enter] as @e[type=#backrooms:canexit,tag=!level5_55enter,distance=..2] run execute in backrooms:level5 run tp 0 16 0
execute at @e[tag=level6enter] as @a[tag=!level6enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level6 10t
execute at @e[tag=level6enter] as @e[type=#backrooms:canexit,tag=!level6enter,distance=..2] run execute in backrooms:level6 run tp 0 16 0
execute at @e[tag=level6_1enter] as @a[tag=!level6_1enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel1/level6_1 10t
execute at @e[tag=level6_1enter] as @e[type=#backrooms:canexit,tag=!level6_1enter,distance=..2] run execute in backrooms:level6 run tp 0 16 0
execute at @a if biome ~ ~ ~ backrooms:level6_1 if score @p backroomsy matches 20.. run tp @p ~ 16 ~
execute at @e[tag=level6_11enter] as @a[tag=!level6_11enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel11/level6_11 10t
execute at @e[tag=level6_11enter] as @e[type=#backrooms:canexit,tag=!level6_11enter,distance=..2] run execute in backrooms:level6 run tp 0 2 0
execute at @e[tag=level6_21enter] as @a[tag=!level6_21enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel21/level6_21 10t
execute at @e[tag=level6_21enter] as @e[type=#backrooms:canexit,tag=!level6_21enter,distance=..2] run execute in backrooms:level6 run tp 0 16 0
execute at @e[tag=leveltauenter] as @a[tag=!leveltauenter,distance=..2] run schedule function backrooms:locatesublevels/sublevel28/leveltau 10t
execute at @e[tag=leveltauenter] as @e[type=#backrooms:canexit,tag=!leveltauenter,distance=..2] run execute in backrooms:level6 run tp 0 16 0
execute at @e[tag=level6_5enter] as @a[tag=!level6_5enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel5/level6_5 10t
execute at @e[tag=level6_5enter] as @e[type=#backrooms:canexit,tag=!level6_5enter,distance=..2] run execute in backrooms:level6 run tp 0 16 0
execute at @e[tag=level6_66enter] as @a[tag=!level6_66enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel66/level6_66 10t
execute at @e[tag=level6_66enter] as @e[type=#backrooms:canexit,tag=!level6_66enter,distance=..2] run execute in backrooms:level6 run tp 0 16 0
execute at @e[tag=level7enter] as @a[tag=!level7enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level7 10t
execute at @e[tag=level7enter] as @e[type=#backrooms:canexit,tag=!level7enter,distance=..2] run execute in backrooms:level7 run tp 0 450 0
execute at @a as @p if predicate backrooms:level7_99to7 run schedule function backrooms:locatesublevels/mainspawns/level7 10t
execute at @a as @p if predicate backrooms:level7_99to7 run execute in backrooms:level7 run tp 0 450 0
execute at @e[tag=level7_1enter] as @a[tag=!level7_1enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel1/level7_1 10t
execute at @e[tag=level7_1enter] as @e[type=#backrooms:canexit,tag=!level7_1enter,distance=..2] run execute in backrooms:level7 run tp 0 450 0
execute at @e[tag=level7_22enter] as @a[tag=!level7_22enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel22/level7_22 10t
execute at @e[tag=level7_22enter] as @e[type=#backrooms:canexit,tag=!level7_22enter,distance=..2] run execute in backrooms:level7 run tp 0 450 0
execute at @e[tag=level7_43enter] as @a[tag=!level7_43enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel43/level7_43 10t
execute at @e[tag=level7_43enter] as @e[type=#backrooms:canexit,tag=!level7_43enter,distance=..2] run execute in backrooms:level7 run tp 0 450 0
execute at @a if biome ~ ~ ~ backrooms:level7_43 if score @p backroomsy matches 350.. run tp @p ~ 340 ~
execute at @e[tag=level7_5enter] as @a[tag=!level7_5enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel5/level7_5 10t
execute at @e[tag=level7_5enter] as @e[type=#backrooms:canexit,tag=!level7_5enter,distance=..2] run execute in backrooms:level7 run tp 0 450 0
execute at @e[tag=level7_7enter] as @a[tag=!level7_7enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel7/level7_7 10t
execute at @e[tag=level7_7enter] as @e[type=#backrooms:canexit,tag=!level7_7enter,distance=..2] run execute in backrooms:level7 run tp 0 450 0
execute at @e[tag=level7_88enter] as @a[tag=!level7_88enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel88/level7_88 10t
execute at @e[tag=level7_88enter] as @e[type=#backrooms:canexit,tag=!level7_88enter,distance=..2] run execute in backrooms:level7 run tp 0 450 0
execute at @e[tag=level8enter] as @a[tag=!level8enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level8 10t
execute at @e[tag=level8enter] as @e[type=#backrooms:canexit,tag=!level8enter,distance=..2] run execute in backrooms:level8 run tp 0 200 0
execute at @a if biome ~ ~ ~ #backrooms:level8_1 if score @p backroomsy matches ..190 run effect give @p minecraft:resistance 5 255
execute at @a if biome ~ ~ ~ #backrooms:level8_1 if score @p backroomsy matches 210.. run effect give @p minecraft:resistance 5 255
execute at @a if biome ~ ~ ~ #backrooms:level8_1 if score @p backroomsy matches ..190 run tp @p ~ 200 ~
execute at @a if biome ~ ~ ~ #backrooms:level8_1 if score @p backroomsy matches 210.. run tp @p ~ 200 ~
execute at @e[tag=level8_11enter] as @a[tag=!level8_11enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel11/level8_11 10t
execute at @e[tag=level8_11enter] as @e[type=#backrooms:canexit,tag=!level8_11enter,distance=..2] run execute in backrooms:level8 run tp 0 16 0
execute at @e[tag=level8_2enter] as @a[tag=!level8_2enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel2/level8_2 10t
execute at @e[tag=level8_2enter] as @e[type=#backrooms:canexit,tag=!level8_2enter,distance=..2] run execute in backrooms:level8 run tp 0 16 0
execute at @e[tag=level8_4enter] as @a[tag=!level8_4enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel4/level8_4 10t
execute at @e[tag=level8_4enter] as @e[type=#backrooms:canexit,tag=!level8_4enter,distance=..2] run execute in backrooms:level8 run tp 0 16 0
execute at @e[tag=level9enter] as @a[tag=!level9enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level9 10t
execute at @e[tag=level9enter] as @e[type=#backrooms:canexit,tag=!level9enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_01enter] as @a[tag=!level9_01enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel01/level9_01 10t
execute at @e[tag=level9_01enter] as @e[type=#backrooms:canexit,tag=!level9_01enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_11enter] as @a[tag=!level9_11enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel11/level9_11 10t
execute at @e[tag=level9_11enter] as @e[type=#backrooms:canexit,tag=!level9_11enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_2enter] as @a[tag=!level9_2enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel2/level9_2 10t
execute at @e[tag=level9_2enter] as @e[type=#backrooms:canexit,tag=!level9_2enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_21enter] as @a[tag=!level9_21enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel21/level9_21 10t
execute at @e[tag=level9_21enter] as @e[type=#backrooms:canexit,tag=!level9_21enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_31enter] as @a[tag=!level9_31enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel31/level9_31 10t
execute at @e[tag=level9_31enter] as @e[type=#backrooms:canexit,tag=!level9_31enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute as @a[predicate=backrooms:invoid] if dimension backrooms:level4 if biome ~ ~ ~ backrooms:level4_3 if score #server backroomsrandomnumber matches 1..2 run schedule function backrooms:locatesublevels/sublevel31/level9_31 10t
execute as @e[predicate=backrooms:invoid,type=!zombie] if dimension backrooms:level4 if biome ~ ~ ~ backrooms:level4_3 if score #server backroomsrandomnumber matches 1..2 run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_5enter] as @a[tag=!level9_5enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel5/level9_5 10t
execute at @e[tag=level9_5enter] as @e[type=#backrooms:canexit,tag=!level9_5enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_55enter] as @a[tag=!level9_55enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel55/level9_55 10t
execute at @e[tag=level9_55enter] as @e[type=#backrooms:canexit,tag=!level9_55enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_6enter] as @a[tag=!level9_6enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel6/level9_6 10t
execute at @e[tag=level9_6enter] as @e[type=#backrooms:canexit,tag=!level9_6enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_7enter] as @a[tag=!level9_7enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel7/level9_7 10t
execute at @e[tag=level9_7enter] as @e[type=#backrooms:canexit,tag=!level9_7enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level9_99enter] as @a[tag=!level9_99enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel99/level9_99 10t
execute at @e[tag=level9_99enter] as @e[type=#backrooms:canexit,tag=!level9_99enter,distance=..2] run execute in backrooms:level9 run tp 0 16 0
execute at @e[tag=level10enter] as @a[tag=!level10enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level10 10t
execute at @e[tag=level10enter] as @e[type=#backrooms:canexit,tag=!level10enter,distance=..2] run execute in backrooms:level10 run tp 0 319 0
execute at @a as @p if predicate backrooms:level7_99to10 run schedule function backrooms:locatesublevels/mainspawns/level10 10t
execute at @a as @p if predicate backrooms:level7_99to10 run execute in backrooms:level10 run tp 0 319 0
execute at @e[tag=level10_11enter] as @a[tag=!level10_11enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel11/level10_11 10t
execute at @e[tag=level10_11enter] as @e[type=#backrooms:canexit,tag=!level10_11enter,distance=..2] run execute in backrooms:level10 run tp 0 319 0
execute at @e[tag=level10_2enter] as @a[tag=!level10_2enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel2/level10_2 10t
execute at @e[tag=level10_2enter] as @e[type=#backrooms:canexit,tag=!level10_2enter,distance=..2] run execute in backrooms:level10 run tp 0 319 0
execute at @e[tag=level10_21enter] as @a[tag=!level10_21enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel21/level10_21 10t
execute at @e[tag=level10_21enter] as @e[type=#backrooms:canexit,tag=!level10_21enter,distance=..2] run execute in backrooms:level10 run tp 0 319 0
execute at @e[tag=level10_3enter] as @a[tag=!level10_3enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel3/level10_3 10t
execute at @e[tag=level10_3enter] as @e[type=#backrooms:canexit,tag=!level10_3enter,distance=..2] run execute in backrooms:level10 run tp 0 16 0
execute at @e[tag=level10_4enter] as @a[tag=!level10_4enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel4/level10_4 10t
execute at @e[tag=level10_4enter] as @e[type=#backrooms:canexit,tag=!level10_4enter,distance=..2] run execute in backrooms:level10 run tp 0 16 0
execute at @e[tag=level10_6enter] as @a[tag=!level10_6enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel6/level10_6 10t
execute at @e[tag=level10_6enter] as @e[type=#backrooms:canexit,tag=!level10_6enter,distance=..2] run execute in backrooms:level10 run tp 0 16 0
execute at @e[tag=level10_99enter] as @a[tag=!level10_99enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel99/level10_99 10t
execute at @e[tag=level10_99enter] as @e[type=#backrooms:canexit,tag=!level10_99enter,distance=..2] run execute in backrooms:level10 run tp 0 16 0
execute at @e[tag=level11enter] as @a[tag=!level11enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level11 10t
execute at @e[tag=level11enter] as @e[type=#backrooms:canexit,tag=!level11enter,distance=..2] run execute in backrooms:level11 run tp 0 16 0
execute at @e[tag=level11_11enter] as @a[tag=!level11_11enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel11/level11_11 10t
execute at @e[tag=level11_11enter] as @e[type=#backrooms:canexit,tag=!level11_11enter,distance=..2] run execute in backrooms:level11 run tp 0 16 0
execute at @e[tag=level11_2enter] as @a[tag=!level11_2enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel2/level11_2 10t
execute at @e[tag=level11_2enter] as @e[type=#backrooms:canexit,tag=!level11_2enter,distance=..2] run execute in backrooms:level11 run tp 0 16 0
execute at @e[tag=level11_3enter] as @a[tag=!level11_3enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel3/level11_3 10t
execute at @e[tag=level11_3enter] as @e[type=#backrooms:canexit,tag=!level11_3enter,distance=..2] run execute in backrooms:level11 run tp 0 16 0
execute at @e[tag=level12enter] as @a[tag=!level12enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level12 10t
execute at @e[tag=level12enter] as @e[type=#backrooms:canexit,tag=!level12enter,distance=..2] run execute in backrooms:level12 run tp 0 16 0
execute at @e[tag=level13enter] as @a[tag=!level13enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level13 10t
execute at @e[tag=level13enter] as @e[type=#backrooms:canexit,tag=!level13enter,distance=..2] run execute in backrooms:level13 run tp 0 16 0
execute at @a if biome ~ ~ ~ backrooms:level13_1 if score @p backroomsy matches 30.. run tp @p ~ 16 ~
execute at @e[tag=level13_1enter] as @a[tag=!level13_1enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel1/level13_1 10t
execute at @e[tag=level13_1enter] as @e[type=#backrooms:canexit,tag=!level13_1enter,distance=..2] run execute in backrooms:level13 run tp 0 16 0
execute at @e[tag=level14enter] as @e[type=#backrooms:canexit,tag=!level14enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level14enter] as @e[type=#backrooms:canexit,tag=!level14enter,distance=..2] run execute in backrooms:level14 run tp ~ 319 ~
execute at @e[tag=level15enter] as @e[type=#backrooms:canexit,tag=!level15enter,distance=..2] run execute in backrooms:level15 run tp ~ 16 ~
execute at @e[tag=level16enter] as @e[type=#backrooms:canexit,tag=!level16enter,distance=..2] run execute in backrooms:level15 run tp ~ 16 ~
execute at @e[tag=level17enter] as @e[type=#backrooms:canexit,tag=!level17enter,distance=..2] run execute in backrooms:level15 run tp ~ 16 ~
execute at @e[tag=level18enter] as @e[type=#backrooms:canexit,tag=!level18enter,distance=..2] run execute in backrooms:level15 run tp ~ 16 ~
execute at @e[tag=level19enter] as @e[type=#backrooms:canexit,tag=!level19enter,distance=..2] run execute in backrooms:level19 run tp ~ 16 ~
execute at @e[tag=level20enter] as @e[type=#backrooms:canexit,tag=!level20enter,distance=..2] run execute in backrooms:level19 run tp ~ 16 ~
execute at @e[tag=level21enter] as @e[type=#backrooms:canexit,tag=!level21enter,distance=..2] run execute in backrooms:level19 run tp ~ 16 ~
execute at @e[tag=level22enter] as @e[type=#backrooms:canexit,tag=!level22enter,distance=..2] run execute in backrooms:level22 run tp ~ 16 ~
execute at @e[tag=level23enter] as @e[type=#backrooms:canexit,tag=!level23enter,distance=..2] run execute in backrooms:level22 run tp ~ 16 ~
execute at @e[tag=level24enter] as @e[type=#backrooms:canexit,tag=!level24enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level24enter] as @e[type=#backrooms:canexit,tag=!level24enter,distance=..2] run execute in backrooms:level24 run tp ~ 319 ~
execute at @e[tag=level25enter] as @e[type=#backrooms:canexit,tag=!level25enter,distance=..2] run execute in backrooms:level25 run tp ~ 16 ~
execute at @e[tag=level26enter] as @e[type=#backrooms:canexit,tag=!level26enter,distance=..2] run execute in backrooms:level26 run tp ~ 16 ~
execute at @e[tag=level27enter] as @e[type=#backrooms:canexit,tag=!level27enter,distance=..2] run execute in backrooms:level26 run tp ~ 16 ~
execute at @e[tag=level28enter] as @e[type=#backrooms:canexit,tag=!level28enter,distance=..2] run execute in backrooms:level26 run tp ~ 16 ~
execute at @e[tag=level29enter] as @e[type=#backrooms:canexit,tag=!level29enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level29enter] as @e[type=#backrooms:canexit,tag=!level29enter,distance=..2] run execute in backrooms:level29 run tp ~ 319 ~
execute at @e[tag=level30enter] as @e[type=#backrooms:canexit,tag=!level30enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level30enter] as @e[type=#backrooms:canexit,tag=!level30enter,distance=..2] run execute in backrooms:level29 run tp ~ 319 ~
execute at @e[tag=level31enter] as @e[type=#backrooms:canexit,tag=!level31enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level31enter] as @e[type=#backrooms:canexit,tag=!level31enter,distance=..2] run execute in backrooms:level29 run tp ~ 319 ~
execute at @e[tag=level32enter] as @e[type=#backrooms:canexit,tag=!level32enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level32enter] as @e[type=#backrooms:canexit,tag=!level32enter,distance=..2] run execute in backrooms:level32 run tp ~ 319 ~
execute at @e[tag=level33enter] as @e[type=#backrooms:canexit,tag=!level33enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level33enter] as @e[type=#backrooms:canexit,tag=!level33enter,distance=..2] run execute in backrooms:level32 run tp ~ 319 ~
execute at @e[tag=level34enter] as @e[type=#backrooms:canexit,tag=!level34enter,distance=..2] run execute in backrooms:level34 run tp ~ 16 ~
execute at @e[tag=level35enter] as @e[type=#backrooms:canexit,tag=!level35enter,distance=..2] run execute in backrooms:level34 run tp ~ 16 ~
execute at @e[tag=level36enter] as @e[type=#backrooms:canexit,tag=!level36enter,distance=..2] run execute in backrooms:level34 run tp ~ 16 ~
execute at @e[tag=level37enter] as @a[tag=!level37enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level37 10t
execute at @e[tag=level37enter] as @e[type=#backrooms:canexit,tag=!level37enter,distance=..2] run execute in backrooms:level37 run tp 0 58 0
execute at @e[tag=level37_1enter] as @a[tag=!level37_1enter,distance=..2] run schedule function backrooms:locatesublevels/sublevel1/level37_1 10t
execute at @e[tag=level37_1enter] as @e[type=#backrooms:canexit,tag=!level37_1enter,distance=..2] run execute in backrooms:level37 run tp 0 58 0
execute at @e[tag=level38enter] as @e[type=#backrooms:canexit,tag=!level38enter,distance=..2] run execute in backrooms:level37 run tp ~ 58 ~
execute at @e[tag=level39enter] as @e[type=#backrooms:canexit,tag=!level39enter,distance=..2] run execute in backrooms:level37 run tp ~ 58 ~
execute at @e[tag=level40enter] as @e[type=#backrooms:canexit,tag=!level40enter,distance=..2] run execute in backrooms:level37 run tp ~ 58 ~
execute at @e[tag=level41enter] as @e[type=#backrooms:canexit,tag=!level41enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level41enter] as @e[type=#backrooms:canexit,tag=!level41enter,distance=..2] run execute in backrooms:level41 run tp ~ 450 ~
execute at @e[tag=level42enter] as @e[type=#backrooms:canexit,tag=!level42enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level42enter] as @e[type=#backrooms:canexit,tag=!level42enter,distance=..2] run execute in backrooms:level41 run tp ~ 319 ~
execute at @e[tag=level43enter] as @a[tag=!level43enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level43 10t
execute at @e[tag=level43enter] as @e[type=#backrooms:canexit,tag=!level43enter,distance=..2] run execute in backrooms:level43 run tp 0 16 0
execute at @e[tag=level44enter] as @e[type=#backrooms:canexit,tag=!level44enter,distance=..2] run execute in backrooms:level43 run tp ~ 16 ~
execute at @e[tag=level45enter] as @e[type=#backrooms:canexit,tag=!level45enter,distance=..2] run execute in backrooms:level43 run tp ~ 16 ~
execute at @e[tag=level46enter] as @a[tag=!level46enter,distance=..2] run execute in backrooms:level46 run schedule function backrooms:locatesublevels/mainspawns/level46 10t
execute at @e[tag=level46enter] as @e[type=#backrooms:canexit,tag=!level46enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level46enter] as @e[type=#backrooms:canexit,tag=!level46enter,distance=..2] run execute in backrooms:level46 run tp 0 319 0
execute at @e[tag=level47enter] as @e[type=#backrooms:canexit,tag=!level47enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level47enter] as @e[type=#backrooms:canexit,tag=!level47enter,distance=..2] run execute in backrooms:level47 run tp ~ 319 ~
execute at @e[tag=level48enter] as @e[type=#backrooms:canexit,tag=!level48enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level48enter] as @e[type=#backrooms:canexit,tag=!level48enter,distance=..2] run execute in backrooms:level47 run tp ~ 319 ~
execute at @e[tag=level49enter] as @e[type=#backrooms:canexit,tag=!level49enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level49enter] as @e[type=#backrooms:canexit,tag=!level49enter,distance=..2] run execute in backrooms:level47 run tp ~ 319 ~
execute at @e[tag=level50enter] as @e[type=#backrooms:canexit,tag=!level50enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level50enter] as @e[type=#backrooms:canexit,tag=!level50enter,distance=..2] run execute in backrooms:level47 run tp ~ 319 ~
execute at @e[tag=level51enter] as @a[tag=!level51enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level51 10t
execute at @e[tag=level51enter] as @e[type=#backrooms:canexit,tag=!level51enter,distance=..2] run execute in backrooms:level51 run tp 0 16 0
execute at @e[tag=level52enter] as @e[type=#backrooms:canexit,tag=!level52enter,distance=..2] run execute in backrooms:level51 run tp ~ 16 ~
execute at @e[tag=level53enter] as @e[type=#backrooms:canexit,tag=!level53enter,distance=..2] run execute in backrooms:level51 run tp ~ 16 ~
execute at @e[tag=level54enter] as @e[type=#backrooms:canexit,tag=!level54enter,distance=..2] run execute in backrooms:level51 run tp ~ 16 ~
execute at @e[tag=level55enter] as @e[type=#backrooms:canexit,tag=!level55enter,distance=..2] run execute in backrooms:level51 run tp ~ 16 ~
execute at @e[tag=level56enter] as @e[type=#backrooms:canexit,tag=!level56enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level56enter] as @e[type=#backrooms:canexit,tag=!level56enter,distance=..2] run execute in backrooms:level56 run tp ~ 200 ~
execute at @e[tag=level57enter] as @e[type=#backrooms:canexit,tag=!level57enter,distance=..2] run schedule function backrooms:add57 5t
execute at @e[tag=level57enter] as @e[type=#backrooms:canexit,tag=!level57enter,distance=..2] run execute in backrooms:level57 run tp 0 16 0
execute at @e[tag=level58enter] as @a[tag=!level58enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level58 10t
execute at @e[tag=level58enter] as @e[type=#backrooms:canexit,tag=!level58enter,distance=..2] run execute in backrooms:level58 run tp 0 21 0
execute at @e[tag=level59enter] as @e[type=#backrooms:canexit,tag=!level59enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level60enter] as @e[type=#backrooms:canexit,tag=!level60enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level61enter] as @e[type=#backrooms:canexit,tag=!level61enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level62enter] as @e[type=#backrooms:canexit,tag=!level62enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level63enter] as @e[type=#backrooms:canexit,tag=!level63enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level64enter] as @e[type=#backrooms:canexit,tag=!level64enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level65enter] as @e[type=#backrooms:canexit,tag=!level65enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level66enter] as @e[type=#backrooms:canexit,tag=!level66enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level67enter] as @e[type=#backrooms:canexit,tag=!level67enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level68enter] as @e[type=#backrooms:canexit,tag=!level68enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level69enter] as @e[type=#backrooms:canexit,tag=!level69enter,distance=..2] run execute in backrooms:level58 run tp ~ 21 ~
execute at @e[tag=level70enter] as @e[type=#backrooms:canexit,tag=!level70enter,distance=..2] run execute in backrooms:level70 run tp ~ 16 ~
execute at @e[tag=level71enter] as @e[type=#backrooms:canexit,tag=!level71enter,distance=..2] run execute in backrooms:level71 run tp ~ 16 ~
execute at @e[tag=level72enter] as @e[type=#backrooms:canexit,tag=!level72enter,distance=..2] run execute in backrooms:level71 run tp ~ 16 ~
execute at @e[tag=level73enter] as @e[type=#backrooms:canexit,tag=!level73enter,distance=..2] run execute in backrooms:level71 run tp ~ 16 ~
execute at @e[tag=level74enter] as @e[type=#backrooms:canexit,tag=!level74enter,distance=..2] run execute in backrooms:level71 run tp ~ 16 ~
execute at @e[tag=level75enter] as @e[type=#backrooms:canexit,tag=!level75enter,distance=..2] run execute in backrooms:level71 run tp ~ 16 ~
execute at @e[tag=level76enter] as @e[type=#backrooms:canexit,tag=!level76enter,distance=..2] run execute in backrooms:level71 run tp ~ 16 ~
execute at @e[tag=level77enter] as @e[type=#backrooms:canexit,tag=!level77enter,distance=..2] run execute in backrooms:level71 run tp ~ 16 ~
execute at @e[tag=level78enter] as @a[tag=!level78enter,distance=..2] run schedule function backrooms:locatesublevels/mainspawns/level78 10t
execute at @e[tag=level78enter] as @e[type=#backrooms:canexit,tag=!level78enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level79enter] as @e[type=#backrooms:canexit,tag=!level79enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level80enter] as @e[type=#backrooms:canexit,tag=!level80enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level81enter] as @e[type=#backrooms:canexit,tag=!level81enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level82enter] as @e[type=#backrooms:canexit,tag=!level82enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @a as @p if predicate backrooms:level7to83 run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level83enter] as @e[type=#backrooms:canexit,tag=!level83enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level84enter] as @e[type=#backrooms:canexit,tag=!level84enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level85enter] as @e[type=#backrooms:canexit,tag=!level85enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level86enter] as @e[type=#backrooms:canexit,tag=!level86enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level87enter] as @e[type=#backrooms:canexit,tag=!level87enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level88enter] as @e[type=#backrooms:canexit,tag=!level88enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level89enter] as @e[type=#backrooms:canexit,tag=!level89enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level90enter] as @e[type=#backrooms:canexit,tag=!level90enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level91enter] as @e[type=#backrooms:canexit,tag=!level91enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level92enter] as @e[type=#backrooms:canexit,tag=!level92enter,distance=..2] run execute in backrooms:level78 run tp ~ 16 ~
execute at @e[tag=level93enter] as @e[type=#backrooms:canexit,tag=!level93enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level93enter] as @e[type=#backrooms:canexit,tag=!level93enter,distance=..2] run execute in backrooms:level93 run tp ~ 319 ~
execute at @e[tag=level94enter] as @e[type=#backrooms:canexit,tag=!level94enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level94enter] as @e[type=#backrooms:canexit,tag=!level94enter,distance=..2] run execute in backrooms:level94 run tp ~ 319 ~
execute at @e[tag=level95enter] as @e[type=#backrooms:canexit,tag=!level95enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level95enter] as @e[type=#backrooms:canexit,tag=!level95enter,distance=..2] run execute in backrooms:level94 run tp ~ 319 ~
execute at @e[tag=level96enter] as @e[type=#backrooms:canexit,tag=!level96enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level96enter] as @e[type=#backrooms:canexit,tag=!level96enter,distance=..2] run execute in backrooms:level94 run tp ~ 319 ~
execute at @e[tag=level97enter] as @e[type=#backrooms:canexit,tag=!level97enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level97enter] as @e[type=#backrooms:canexit,tag=!level97enter,distance=..2] run execute in backrooms:level94 run tp ~ 319 ~
execute at @e[tag=level98enter] as @e[type=#backrooms:canexit,tag=!level98enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level98enter] as @e[type=#backrooms:canexit,tag=!level98enter,distance=..2] run execute in backrooms:level94 run tp ~ 319 ~
execute at @e[tag=level99enter] as @e[type=#backrooms:canexit,tag=!level99enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level99enter] as @e[type=#backrooms:canexit,tag=!level99enter,distance=..2] run execute in backrooms:level94 run tp ~ 319 ~
execute at @e[tag=level100enter] as @e[type=#backrooms:canexit,tag=!level100enter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=level100enter] as @e[type=#backrooms:canexit,tag=!level100enter,distance=..2] run execute in backrooms:level94 run tp ~ 319 ~
schedule function backrooms:enter/enter101_200 2t