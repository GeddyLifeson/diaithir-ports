execute unless entity @a[predicate=backrooms:in_backrooms] run return 0
execute store result score #server backroomsrandomnumber run random value 1..100
execute store result score #server backroomsrandomnumber2 run random value 1..1000
execute store result score #server backroomsrandomnumber3 run random value 1..10000
execute store result score #server level399sign run random value 1..399999
execute if score #server level399sign matches 666 run scoreboard players set #server level399sign 665
#execute store result score Aria976 backroomsrandomnumber run scoreboard players get #server backroomsrandomnumber
execute at @e[tag=spawn] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute at @a as @a if biome ~ ~ ~ #backrooms:level3 unless biome ~ ~ ~ backrooms:level3_5 run stopsound @s * minecraft:entity.lightning_bolt.thunder
#level237
execute in backrooms:level237 positioned 0 16 0 unless block 0 16 0 oak_planks run tag @n[type=minecraft:armor_stand,tag=!level237enterance] add level237spawn
execute at @e[tag=level237end] if block ~ ~ ~1 minecraft:oak_fence run kill @n
execute at @e[tag=level237end] run function backrooms:worldgen/level237end
execute at @e[tag=level237enterance] unless block ~ ~-1 ~ minecraft:mangrove_planks run kill @n
execute at @e[tag=level237spawn] if block 0 16 0 air run place template backrooms:level237 0 16 0 none none
execute at @e[tag=level237spawn] run setblock 16 16 3 minecraft:mangrove_planks
execute at @e[tag=level237start] if block ~ ~ ~1 minecraft:oak_fence run kill @n
execute at @e[tag=level237start] run function backrooms:worldgen/level237start
scoreboard players set @a nearfloatingisland 0
scoreboard players set #server teleportedplayer 0
execute at @e[tag=floatingisland] as @e[type=minecraft:player,distance=..25] if score @s backroomsy > @n[tag=floatingisland] backroomsy run scoreboard players set @s nearfloatingisland 1
execute at @e[tag=palmtreeplacer] unless score #server devmode matches 1 run place feature backrooms:palmtree ~ ~ ~
execute at @e[tag=palmtreeplacer] as @e[tag=palmtreeplacer] unless score #server devmode matches 1 unless block ~ ~ ~ air run kill @s
execute as @e[tag=floatingisland] store result score @s backroomsy run data get entity @s Pos[1]
execute as @e[tag=floatingisland] run scoreboard players add @s backroomsy 3
execute as @a at @a if predicate backrooms:floatingisland unless score @s nearfloatingisland matches 1 unless block ~ ~ ~ water unless block ~ ~-1 ~ water run effect give @s minecraft:levitation 2 2 true
execute at @e[tag=rain] run playsound minecraft:weather.rain weather @p
execute at @e[tag=spawn] as @e[tag=spawn] if block ~ ~-1 ~ minecraft:water run kill @s
execute at @e[tag=blacksmoke] run particle minecraft:large_smoke ~ ~1 ~ 0 0 0 0.2 1000
execute at @e[tag=hallucination] as @a[distance=..15] run data modify entity @n[tag=hallucination] Invisible set value 1
execute at @e[tag=hallucination] as @a[distance=15..] run data modify entity @n[tag=hallucination] Invisible set value 0
execute at @e[type=minecraft:glow_item_frame] if block ~ ~ ~ minecraft:light if biome ~ ~ ~ #backrooms:backrooms run setblock ~ ~ ~ air
execute as @e[tag=level43park] as @e[tag=level43park] run data modify entity @s NoGravity set value 1
execute at @e[tag=vendingmachine] if block ~ ~1 ~ air run kill @n
execute as @a if predicate backrooms:dead run scoreboard players set @p backroomssanity 3600
execute as @e[type=minecraft:snowball,nbt={Item:{components:{"minecraft:item_model": "backrooms:firesalt"}}}] at @s run function backrooms:firesalt
#shady
execute at @e[tag=shady0-2] run place template backrooms:shady0-2 ~ ~-1 ~1
execute at @e[tag=shady0-2] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady0-2]
execute at @e[tag=shady0-3] run place template backrooms:shady0-3 ~1 ~-1 ~
execute at @e[tag=shady0-3] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady0-3]
execute at @e[tag=shady0-4] run place template backrooms:shady0-4 ~1 ~-1 ~
execute at @e[tag=shady0-4] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady0-4]
execute at @e[tag=shady1] run place template backrooms:shady1 ~ ~-1 ~ clockwise_90
execute at @e[tag=shady1] if block ~-19 ~ ~2 minecraft:pale_oak_planks run kill @n[tag=shady1]
execute at @e[tag=shady2] run place template backrooms:shady2 ~53 ~-1 ~1 clockwise_90
execute at @e[tag=shady2] if block ~ ~ ~2 minecraft:snow run kill @n[tag=shady2]
execute at @e[tag=shady2-2] run place template backrooms:shady2-2 ~-1 ~-1 ~ clockwise_90
execute at @e[tag=shady2-2] if block ~-1 ~-1 ~ minecraft:pale_moss_block run kill @n[tag=shady2-2]
execute at @e[tag=shady2-3] run place template backrooms:shady2-3 ~ ~-1 ~1 clockwise_90
execute at @e[tag=shady2-3] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady2-3]
execute at @e[tag=shady2-4] run place template backrooms:shady2-4 ~ ~-1 ~1 clockwise_90
execute at @e[tag=shady2-4] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady2-4]
execute at @e[tag=shady3] run place template backrooms:shady3 ~ ~ ~48
execute at @e[tag=shady3] if block ~20 ~3 ~72 minecraft:water run kill @n[tag=shady3]
execute at @e[tag=shady3-2] run place template backrooms:shady3-2 ~ ~-1 ~1
execute at @e[tag=shady3-2] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady3-2]
execute at @e[tag=shady3-3] run place template backrooms:shady3-3 ~ ~-1 ~1
execute at @e[tag=shady3-3] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady3-3]
execute at @e[tag=shady3-4] run place template backrooms:shady3-4 ~1 ~-1 ~
execute at @e[tag=shady3-4] if block ~2 ~-1 ~ minecraft:pale_moss_block run kill @n[tag=shady3-4]
execute at @e[tag=shady3-5] run place template backrooms:shady3-5 ~-95 ~-1 ~1
execute at @e[tag=shady3-5] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady3-5]
execute at @e[tag=shady3-6] run place template backrooms:shady3-6 ~-95 ~-1 ~1
execute at @e[tag=shady3-6] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady3-6]
execute at @e[tag=shady3-7] run place template backrooms:shady3-7 ~1 ~-1 ~
execute at @e[tag=shady3-7] if block ~1 ~-1 ~ minecraft:pale_moss_block run kill @n[tag=shady3-7]
execute at @e[tag=shady3-8] run place template backrooms:shady3-8 ~-95 ~-1 ~1
execute at @e[tag=shady3-8] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady3-8]
execute at @e[tag=shady3-9] run place template backrooms:shady3-9 ~-95 ~-1 ~1
execute at @e[tag=shady3-9] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady3-9]
execute at @e[tag=shady4] run place template backrooms:shady4 ~ ~ ~48
execute at @e[tag=shady4] if block ~20 ~3 ~72 minecraft:pale_moss_block run kill @n[tag=shady4]
execute at @e[tag=shady4-2] run place template backrooms:shady4-2 ~ ~-1 ~1
execute at @e[tag=shady4-2] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady4-2]
execute at @e[tag=shady4-3] run place template backrooms:shady4-3 ~ ~-1 ~1
execute at @e[tag=shady4-3] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady4-3]
execute at @e[tag=shady4-4] run place template backrooms:shady4-4 ~1 ~-1 ~
execute at @e[tag=shady4-4] if block ~2 ~-1 ~ minecraft:pale_moss_block run kill @n[tag=shady4-4]
execute at @e[tag=shady4-5] run place template backrooms:shady4-5 ~-95 ~-1 ~1
execute at @e[tag=shady4-5] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady4-5]
execute at @e[tag=shady4-6] run place template backrooms:shady4-6 ~-95 ~-1 ~1
execute at @e[tag=shady4-6] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady4-6]
execute at @e[tag=shady4-7] run place template backrooms:shady4-7 ~1 ~-1 ~
execute at @e[tag=shady4-7] if block ~1 ~-1 ~ minecraft:pale_moss_block run kill @n[tag=shady4-7]
execute at @e[tag=shady4-8] run place template backrooms:shady4-8 ~-95 ~-1 ~1
execute at @e[tag=shady4-8] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady4-8]
execute at @e[tag=shady4-9] run place template backrooms:shady4-9 ~-95 ~-1 ~1
execute at @e[tag=shady4-9] if block ~ ~-1 ~1 minecraft:pale_moss_block run kill @n[tag=shady4-9]
execute at @e[tag=shady5] run place template backrooms:shady5 ~ ~ ~48
execute at @e[tag=shady5] if block ~20 ~3 ~72 minecraft:pale_moss_block run kill @n[tag=shady5]
execute at @e[tag=shady6] if block ~ ~-1 ~1 minecraft:black_concrete run kill @n[tag=shady6]
execute at @e[tag=shady6] run place template backrooms:shady6 ~ ~-1 ~1
execute at @e[tag=shady6-2] if block ~ ~-1 ~1 minecraft:black_concrete run kill @n[tag=shady6-2]
execute at @e[tag=shady6-2] run place template backrooms:shady6-2 ~-2 ~-1 ~1
execute at @e[tag=shady6-3] if block ~ ~-1 ~1 minecraft:black_concrete run kill @n[tag=shady6-3]
execute at @e[tag=shady6-3] run place template backrooms:shady6-3 ~-2 ~-1 ~1
execute at @e[tag=shady6-4] if block ~ ~-1 ~1 minecraft:black_concrete run kill @n[tag=shady6-4]
execute at @e[tag=shady6-4] run place template backrooms:shady6-4 ~-2 ~-1 ~1
execute at @e[tag=levelrun] if block ~ ~-1 ~1 minecraft:red_concrete run kill @n
execute at @e[tag=levelrun] if score #server backroomsrandomnumber matches 0..14 run place template backrooms:levelrun ~-4 ~-1 ~1
execute at @e[tag=levelrun] if score #server backroomsrandomnumber matches 15..28 run place template backrooms:levelrun2 ~-4 ~-1 ~1
execute at @e[tag=levelrun] if score #server backroomsrandomnumber matches 29..42 run place template backrooms:levelrun3 ~-4 ~-1 ~1
execute at @e[tag=levelrun] if score #server backroomsrandomnumber matches 43..56 run place template backrooms:levelrun4 ~-4 ~-1 ~1
execute at @e[tag=levelrun] if score #server backroomsrandomnumber matches 57..70 run place template backrooms:levelrun5 ~-4 ~-1 ~1
execute at @e[tag=levelrun] if score #server backroomsrandomnumber matches 71..84 run place template backrooms:levelrun6 ~-4 ~-1 ~1
execute at @e[tag=levelrun] if score #server backroomsrandomnumber matches 85..99 run place template backrooms:levelrun7 ~-4 ~-1 ~1
execute at @e[tag=levelrun] if score #server backroomsrandomnumber matches 100 run place template backrooms:levelrunend ~-11 ~-1 ~1
execute as @e[tag=human,tag=!namedhuman] store result score @s humannamenumber run random value 1..23
execute as @e[tag=human,tag=!namedhuman] run function backrooms:randomizename
execute as @a if score @s hasdisease matches 1 run scoreboard players add @s diseasetimer 1
#sanity bar
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 3421.. run title @s actionbar [{text:"111111111144444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 3241..3420 run title @s actionbar [{text:"111111111244444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 3061..3240 run title @s actionbar [{text:"111111111344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 2881..3060 run title @s actionbar [{text:"111111112344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 2701..2880 run title @s actionbar [{text:"111111113344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 2521..2700 run title @s actionbar [{text:"111111123344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 2341..2520 run title @s actionbar [{text:"111111133344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 2161..2340 run title @s actionbar [{text:"111111233344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 1981..2160 run title @s actionbar [{text:"111111333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 1801..1980 run title @s actionbar [{text:"111112333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 1621..1800 run title @s actionbar [{text:"111113333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 1441..1620 run title @s actionbar [{text:"111123333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 1261..1440 run title @s actionbar [{text:"111133333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 1081..1260 run title @s actionbar [{text:"111233333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 901..1080 run title @s actionbar [{text:"111333333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 721..900 run title @s actionbar [{text:"112333333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 540..720 run title @s actionbar [{text:"113333333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 361..540 run title @s actionbar [{text:"123333333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 181..360 run title @s actionbar [{text:"133333333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches 0..180 run title @s actionbar [{text:"233333333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute at @a as @p if biome ~ ~ ~ #backrooms:backrooms if score @s backroomssanity matches ..0 run title @s actionbar [{text:"333333333344444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444",font:"backrooms:sanitybar"}]
execute as @a store result score @s backroomsy run data get entity @s Pos[1]
execute as @a store result score @s pinkmaxhealth run attribute @s minecraft:max_health get
execute as @a store result score @s pinkabsorb run attribute @s minecraft:max_absorption get
execute as @a run scoreboard players operation @a pinkmaxhealth += @s pinkabsorb
execute as @a store result score @s pinkallowedhealth run scoreboard players operation @s pinkmaxhealth -= @s pinklarvae
execute as @a run scoreboard players operation @s pinkallowedhealth -= @s clockroaches
execute as @a run scoreboard players operation @s pinkallowedhealth -= @s mimicglove
execute as @a if score @s health > @s pinkallowedhealth run damage @s 1 backrooms:pinklarvae
execute as @a if predicate backrooms:dead run scoreboard players set @p pinklarvae 0
execute as @e[tag=level7_99] store result score @s backroomsfacing run data get entity @s Rotation.[0]
execute at @e[tag=level7_99] as @n[tag=level7_99] if score @s backroomsfacing matches -90 run function backrooms:worldgen/level7_99_-90
execute at @e[tag=level7_99] as @n[tag=level7_99] if score @s backroomsfacing matches 270 run function backrooms:worldgen/level7_99_-90
execute at @e[tag=level7_99] as @n[tag=level7_99] if score @s backroomsfacing matches -450 run function backrooms:worldgen/level7_99_-90
execute at @e[tag=level7_99] as @n[tag=level7_99] if score @s backroomsfacing matches -180 run function backrooms:worldgen/level7_99_180
execute at @e[tag=level7_99] as @n[tag=level7_99] if score @s backroomsfacing matches 180 run function backrooms:worldgen/level7_99_180
execute at @e[tag=level7_99] as @n[tag=level7_99] if score @s backroomsfacing matches -540 run function backrooms:worldgen/level7_99_90
execute at @e[tag=level7_99] as @n[tag=level7_99] if score @s backroomsfacing matches 90 run function backrooms:worldgen/level7_99_90
execute at @e[tag=level7_99] as @n[tag=level7_99] if score @s backroomsfacing matches -270 run function backrooms:worldgen/level7_99_0
execute at @e[tag=level7_99] as @n[tag=level7_99] if score @s backroomsfacing matches 0 run function backrooms:worldgen/level7_99_0
#level494
execute as @e[tag=level494] store result score @s backroomsfacing run data get entity @s Rotation.[0]
execute at @e[tag=level494] as @n[tag=level494] if score @s backroomsfacing matches -90 run function backrooms:worldgen/level494_-90
execute at @e[tag=level494] as @n[tag=level494] if score @s backroomsfacing matches 270 run function backrooms:worldgen/level494_-90
execute at @e[tag=level494] as @n[tag=level494] if score @s backroomsfacing matches -450 run function backrooms:worldgen/level494_-90
execute at @e[tag=level494] as @n[tag=level494] if score @s backroomsfacing matches -180 run function backrooms:worldgen/level494_180
execute at @e[tag=level494] as @n[tag=level494] if score @s backroomsfacing matches 180 run function backrooms:worldgen/level494_180
execute at @e[tag=level494] as @n[tag=level494] if score @s backroomsfacing matches -540 run function backrooms:worldgen/level494_90
execute at @e[tag=level494] as @n[tag=level494] if score @s backroomsfacing matches 90 run function backrooms:worldgen/level494_90
execute at @e[tag=level494] as @n[tag=level494] if score @s backroomsfacing matches -270 run function backrooms:worldgen/level494_0
execute at @e[tag=level494] as @n[tag=level494] if score @s backroomsfacing matches 0 run function backrooms:worldgen/level494_0
execute at @e[tag=reflection] as @n run data modify entity @s profile.id set from entity @p[distance=..15] UUID
execute as @a store result score @s jackoburgers run clear @s minecraft:music_disc_11[item_model="backrooms:jack_o_burger"] 0
execute as @e[tag=reflection] at @s run tp @s ~ ~ ~ facing entity @p[distance=..15]
effect give @e[tag=human] minecraft:fire_resistance infinite 0 true
execute as @e[tag=human] run attribute @s minecraft:follow_range base set 50
execute at @a as @p if items entity @s weapon.mainhand minecraft:music_disc_11[minecraft:custom_data={fryvo_jade: 1b}] run function backrooms:object/fryvojade_main
execute at @a as @p if items entity @s weapon.offhand minecraft:music_disc_11[minecraft:custom_data={fryvo_jade: 1b}] run function backrooms:object/fryvojade_off
execute at @a as @p if items entity @s weapon.mainhand minecraft:music_disc_11[minecraft:custom_data={evil_eye: 1b}] run function backrooms:object/evileye_main
execute at @a as @p if items entity @s weapon.offhand minecraft:music_disc_11[minecraft:custom_data={evil_eye: 1b}] run function backrooms:object/evileye_off
execute at @a as @p if items entity @s weapon.mainhand minecraft:music_disc_11[minecraft:custom_data={noclip_anchor: 1b}] run function backrooms:object/noclipanchor_main
execute at @a as @p if items entity @s weapon.offhand minecraft:music_disc_11[minecraft:custom_data={noclip_anchor: 1b}] run function backrooms:object/noclipanchor_off
#firesalt arrow
execute at @e[type=#arrows,nbt={inGround:1b,item:{id: "minecraft:tipped_arrow", components:{"minecraft:custom_data":{firesalt_arrow: 1b}}}}] run function backrooms:arrow/inground/firesalt
execute at @e[predicate=backrooms:firesalt] run function backrooms:arrow/firesalt
#liquidsilence arrow
execute as @e[predicate=backrooms:liquidsilence] if predicate backrooms:liquidsilence run function backrooms:arrow/liquidsilence
#smilerrepel arrow
execute as @e[tag=smiler] if predicate backrooms:smilerrepel run damage @s 3 minecraft:generic
#kill
execute as @e[tag=outlet] unless data entity @s Item run kill @s
execute as @e[tag=backshroom] unless data entity @s Item run kill @s
execute as @e[tag=everbulb] unless data entity @s Item run kill @s
execute as @e[tag=scaraback] unless data entity @s Item run kill @s
execute as @e[tag=diceofdestiny] unless data entity @s Item run kill @s
execute as @e[tag=jackoburger] unless data entity @s Item run kill @s
execute as @e[tag=luckycrane] unless data entity @s Item run kill @s
execute as @e[tag=livingpizza] unless data entity @s Item run kill @s
execute at @a as @p if items entity @s weapon.mainhand minecraft:music_disc_11[minecraft:item_model="backrooms:everlight"] if entity @e[type=#backrooms:hostile,distance=..5] run execute as @e[type=#backrooms:hostile,distance=..5] run damage @s 0 minecraft:thorns by @n from @p
execute at @a as @p if items entity @s weapon.offhand minecraft:music_disc_11[minecraft:item_model="backrooms:everlight"] if entity @e[type=#backrooms:hostile,distance=..5] run execute as @e[type=#backrooms:hostile,distance=..5] run damage @s 0 minecraft:thorns by @n from @p
execute at @e[tag=ghostlight] run advancement grant @a[distance=..10] only backrooms:objects/ghostlight
execute at @e[tag=ghostlight] run advancement grant @a[distance=..10] only backrooms:objects ghostlight
execute at @e[tag=ghostlight] run advancement grant @a[distance=..10] only backrooms:true_explorer ghostlight
execute at @e[tag=rixagas] run advancement grant @a[distance=..10] only backrooms:objects/rixagas
execute at @e[tag=rixagas] run advancement grant @a[distance=..10] only backrooms:objects rixagas
execute at @e[tag=rixagas] run advancement grant @a[distance=..10] only backrooms:true_explorer rixagas
execute at @e[tag=burkhartgas] run advancement grant @a[distance=..10] only backrooms:objects/burkhartgas
execute at @e[tag=burkhartgas] run advancement grant @a[distance=..10] only backrooms:objects burkhartgas
execute at @e[tag=burkhartgas] run advancement grant @a[distance=..10] only backrooms:true_explorer burkhartgas
execute at @e[tag=ghostlight] if score #server ghostlightstate matches 0..1 if entity @e[type=#backrooms:hostile,distance=..10] run execute as @e[type=#backrooms:hostile,distance=..10] run damage @s 0 minecraft:thorns by @n from @n
execute at @e[tag=ghostlight] as @n[tag=ghostlight] if block ~ ~2 ~ air run kill @s
execute at @a as @p if score #server ghostlightstate matches 2 if entity @e[tag=ghostlight,distance=..15] run tp @s ~ ~ ~ facing entity @n[tag=ghostlight,distance=..15]
execute at @a[tag=liquidsilence] as @p if predicate backrooms:moving run attribute @s minecraft:gravity modifier add backrooms:liquidsilence -1 add_multiplied_base
execute at @a[tag=liquidsilence] as @p unless predicate backrooms:moving run attribute @s minecraft:gravity modifier remove backrooms:liquidsilence
execute at @a[tag=liquidsilence] as @p if predicate backrooms:jumping run attribute @s minecraft:gravity modifier remove backrooms:liquidsilence
execute at @e[tag=rixagas] run particle minecraft:crimson_spore ~ ~1 ~ 0 1 0 10 500
execute at @e[tag=rixagas] run scoreboard players remove @a[distance=..15] backroomssanity 1
execute at @e[tag=rixagas] run damage @p[distance=..15] 1 minecraft:cactus by @p from @n
execute at @e[tag=burkhartgas] run particle minecraft:soul_fire_flame ~ ~1 ~ 0 1 0 0.4 100
execute at @e[tag=burkhartgas] run effect give @a[distance=..15] minecraft:nausea 10 0 true
execute at @e[tag=burkhartgas] run effect give @a[distance=..15] minecraft:slowness 1 0 true
execute at @e[tag=burkhartgas] run damage @p[distance=..15] 1 minecraft:cactus by @p from @n
execute at @e[tag=level399sign] as @n[type=minecraft:item_frame] run data merge block ~ ~ ~ {front_text:{messages:["",{"score":{"name":"#server","objective":"level399sign"}},"",""]}}
execute at @e[tag=level399sign] if block ~ ~ ~ minecraft:oak_wall_sign run kill @n
kill @e[tag=backroomsdie]