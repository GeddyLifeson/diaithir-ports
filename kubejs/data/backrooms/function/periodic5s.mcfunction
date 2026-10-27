execute unless entity @a[predicate=backrooms:loop_needed] run return run schedule function backrooms:periodic5s 5s
schedule function backrooms:periodic5s 5s
kill @e[type=falling_block,predicate=backrooms:inlevel0]
kill @e[type=tnt,predicate=backrooms:inlevel0]
execute as @a at @a if biome ~ ~ ~ backrooms:level139_5 unless score @s neardenizen matches 1 run playsound minecraft:entity.player.breath master @p
execute if score #server backroomslocateattempts matches 6.. run scoreboard players set #server backroomslocateattempts 0
execute at @e[tag=shady1entityspawn] unless entity @e[tag=landlord,predicate=backrooms:inshady] run place template backrooms:landlord
execute at @e[tag=shady4entityspawn] unless entity @e[tag=smiler,predicate=backrooms:inshady] run place template backrooms:smiler
execute at @e[tag=shady4entityspawn] unless entity @e[tag=skinstealer,predicate=backrooms:inshady] run place template backrooms:skinstealer
execute at @e[tag=shady4entityspawn] unless entity @e[tag=faceling,predicate=backrooms:inshady] run place template backrooms:faceling
execute at @e[tag=shady4entityspawn] unless entity @e[tag=mangled,predicate=backrooms:inshady] run place template backrooms:mangled
execute at @e[tag=rixagas] run damage @p[distance=..15] 1 minecraft:player_attack by @p from @n
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server backroomsrandomnumber matches ..10 run title @s title [{font:"backrooms:colorfilter",text:"4",color:blue}]
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server backroomsrandomnumber matches ..10 run advancement grant @s only backrooms:pheno/unnumbered/blueflash
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server backroomsrandomnumber matches ..10 run advancement grant @s only backrooms:pheno blueflash
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server backroomsrandomnumber matches ..10 run advancement grant @s only backrooms:true_explorer blueflash
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server backroomsrandomnumber matches 11 run scoreboard players set #server footsteps 1
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server footsteps matches 1.. run advancement grant @s only backrooms:pheno/unnumbered/footsteps
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server footsteps matches 1.. run advancement grant @s only backrooms:pheno footsteps
execute at @a as @p if biome ~ ~ ~ #backrooms:level9_01 if score #server footsteps matches 1.. run advancement grant @s only backrooms:true_explorer footsteps