execute unless entity @a[predicate=backrooms:loop_needed] run return run schedule function backrooms:enter/enterunnumbered 20t
execute at @e[tag=homeenter] as @e[type=#backrooms:canexit,tag=!homeenter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=homeenter] as @e[type=#backrooms:canexit,tag=!homeenter,distance=..2] run execute in backrooms:home run tp ~ 319 ~
execute at @e[tag=basemententer] as @e[type=#backrooms:canexit,tag=!basemententer,distance=..2] run execute in backrooms:basement run tp ~ 16 ~
execute at @e[tag=graveenter] as @e[type=#backrooms:canexit,tag=!graveenter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=graveenter] as @e[type=#backrooms:canexit,tag=!graveenter,distance=..2] run execute in backrooms:grave run tp ~ 319 ~
execute at @e[tag=shadyenter] as @a[tag=!shadyenter,distance=..2] run schedule function backrooms:addshady 20t
execute at @e[tag=shadyenter] as @e[type=#backrooms:canexit,tag=!shadyenter,distance=..2] run execute in backrooms:shady run tp 68 16 68
execute at @e[tag=shady2enter] as @e[type=#backrooms:canexit,tag=!shady2enter,distance=..2] run execute in backrooms:shady run tp 94 20 168
execute at @e[tag=shady3enter] as @e[type=#backrooms:canexit,tag=!shady3enter,distance=..2] run execute in backrooms:shady run tp 147 32 499
execute at @e[tag=shady4enter] as @e[type=#backrooms:canexit,tag=!shady4enter,distance=..2] run execute in backrooms:shady run tp 381 30 824
execute at @e[tag=shady5enter] as @e[type=#backrooms:canexit,tag=!shady5enter,distance=..2] run execute in backrooms:shady run tp 352 45 1026
execute at @e[tag=voidenter] as @e[type=#backrooms:canexit,tag=!voidenter,distance=..2] run tp @s ~ -64 ~
execute at @e[tag=bluechannelenter] as @e[type=#backrooms:canexit,tag=!bluechannelenter,distance=..2] run execute in backrooms:bluechannel run tp ~ 100 ~
execute at @e[tag=atticenter] as @e[type=#backrooms:canexit,tag=!atticenter,distance=..2] run execute in backrooms:attic run tp ~ 22 ~
execute at @e[tag=levelrunenter] as @a[tag=!levelrunenter,distance=..2] run schedule function backrooms:addlevelrun 20t
execute at @e[tag=levelrunenter] as @e[type=#backrooms:canexit,tag=!levelrunenter,distance=..2] run execute in backrooms:levelrun run tp 3 16 7
execute at @e[tag=frontroomsenter] as @e[type=#backrooms:canexit,tag=!frontroomsenter,distance=..2] run effect give @e[distance=..2] minecraft:slow_falling 100 0 true
execute at @e[tag=frontroomsenter] as @e[type=#backrooms:canexit,tag=!frontroomsenter,distance=..2] run execute in overworld run tp ~ 319 ~
schedule function backrooms:enter/enter0_100 2t