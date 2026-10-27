execute unless entity @a[predicate=backrooms:loop_needed] run return 0
#pitcreator
execute unless score #server devmode matches 1 at @e[tag=1pitcreator] run fill ~ ~-1 ~ ~ ~-1 ~ air
execute unless score #server devmode matches 1 as @e[tag=1pitcreator] at @e[tag=1pitcreator] run tp @s ~ ~-1 ~
execute unless score #server devmode matches 1 as @e[tag=1pitcreator] at @e[tag=1pitcreator] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute unless score #server devmode matches 1 as @e[tag=1pitcreator] run tag @s remove 1pitcreator
execute unless score #server devmode matches 1 at @e[tag=2pitcreator] run fill ~ ~-1 ~ ~ ~-2 ~ air
execute unless score #server devmode matches 1 as @e[tag=2pitcreator] at @e[tag=2pitcreator] run tp @s ~ ~-2 ~
execute unless score #server devmode matches 1 as @e[tag=2pitcreator] at @e[tag=2pitcreator] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute unless score #server devmode matches 1 as @e[tag=2pitcreator] run tag @s remove 2pitcreator
execute unless score #server devmode matches 1 at @e[tag=3pitcreator] run fill ~ ~-1 ~ ~ ~-3 ~ air
execute unless score #server devmode matches 1 as @e[tag=3pitcreator] at @e[tag=3pitcreator] run tp @s ~ ~-3 ~
execute unless score #server devmode matches 1 as @e[tag=3pitcreator] at @e[tag=3pitcreator] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute unless score #server devmode matches 1 as @e[tag=3pitcreator] run tag @s remove 3pitcreator
execute unless score #server devmode matches 1 at @e[tag=4pitcreator] run fill ~ ~-1 ~ ~ ~-4 ~ air
execute unless score #server devmode matches 1 as @e[tag=4pitcreator] at @e[tag=4pitcreator] run tp @s ~ ~-4 ~
execute unless score #server devmode matches 1 as @e[tag=4pitcreator] at @e[tag=4pitcreator] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute unless score #server devmode matches 1 as @e[tag=4pitcreator] run tag @s remove 4pitcreator
execute unless score #server devmode matches 1 at @e[tag=5pitcreator] run fill ~ ~-1 ~ ~ ~-5 ~ air
execute unless score #server devmode matches 1 as @e[tag=5pitcreator] at @e[tag=5pitcreator] run tp @s ~ ~-5 ~
execute unless score #server devmode matches 1 as @e[tag=5pitcreator] at @e[tag=5pitcreator] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute unless score #server devmode matches 1 as @e[tag=5pitcreator] run tag @s remove 5pitcreator
execute unless score #server devmode matches 1 at @e[tag=6pitcreator] run fill ~ ~-1 ~ ~ ~-6 ~ air
execute unless score #server devmode matches 1 as @e[tag=6pitcreator] at @e[tag=6pitcreator] run tp @s ~ ~-6 ~
execute unless score #server devmode matches 1 as @e[tag=6pitcreator] at @e[tag=6pitcreator] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute unless score #server devmode matches 1 as @e[tag=6pitcreator] run tag @s remove 6pitcreator
execute unless score #server devmode matches 1 at @e[tag=7pitcreator] run fill ~ ~-1 ~ ~ ~-7 ~ air
execute unless score #server devmode matches 1 as @e[tag=7pitcreator] at @e[tag=7pitcreator] run tp @s ~ ~-7 ~
execute unless score #server devmode matches 1 as @e[tag=7pitcreator] at @e[tag=7pitcreator] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute unless score #server devmode matches 1 as @e[tag=7pitcreator] run tag @s remove 7pitcreator
execute unless score #server devmode matches 1 at @e[tag=8pitcreator] run fill ~ ~-1 ~ ~ ~-8 ~ air
execute unless score #server devmode matches 1 as @e[tag=8pitcreator] at @e[tag=8pitcreator] run tp @s ~ ~-8 ~
execute unless score #server devmode matches 1 as @e[tag=8pitcreator] at @e[tag=8pitcreator] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute unless score #server devmode matches 1 as @e[tag=8pitcreator] run tag @s remove 8pitcreator
execute unless score #server devmode matches 1 at @e[tag=pitcreator] run fill ~ ~-1 ~ ~ ~-8 ~ air
execute unless score #server devmode matches 1 as @e[tag=pitcreator] at @e[tag=pitcreator] run tp @s ~ ~-8 ~
execute unless score #server devmode matches 1 as @e[tag=pitcreator] at @e[tag=pitcreator] if block ~ ~-1 ~ air run setblock ~ ~-1 ~ dirt
execute unless score #server devmode matches 1 as @e[tag=pitcreator] run tag @s remove pitcreator
#onetimepitcreator
execute unless score #server devmode matches 1 at @e[tag=1onetimepitcreator] run fill ~ ~-1 ~ ~ ~-1 ~ air
execute unless score #server devmode matches 1 as @e[tag=1onetimepitcreator] run kill @s
execute unless score #server devmode matches 1 at @e[tag=2onetimepitcreator] run fill ~ ~-1 ~ ~ ~-2 ~ air
execute unless score #server devmode matches 1 as @e[tag=2onetimepitcreator] run kill @s
execute unless score #server devmode matches 1 at @e[tag=3onetimepitcreator] run fill ~ ~-1 ~ ~ ~-3 ~ air
execute unless score #server devmode matches 1 as @e[tag=3onetimepitcreator] run kill @s
execute unless score #server devmode matches 1 at @e[tag=4onetimepitcreator] run fill ~ ~-1 ~ ~ ~-4 ~ air
execute unless score #server devmode matches 1 as @e[tag=4onetimepitcreator] run kill @s
execute unless score #server devmode matches 1 at @e[tag=5onetimepitcreator] run fill ~ ~-1 ~ ~ ~-5 ~ air
execute unless score #server devmode matches 1 as @e[tag=5onetimepitcreator] run kill @s
execute unless score #server devmode matches 1 at @e[tag=6onetimepitcreator] run fill ~ ~-1 ~ ~ ~-6 ~ air
execute unless score #server devmode matches 1 as @e[tag=6onetimepitcreator] run kill @s
execute unless score #server devmode matches 1 at @e[tag=7onetimepitcreator] run fill ~ ~-1 ~ ~ ~-7 ~ air
execute unless score #server devmode matches 1 as @e[tag=7onetimepitcreator] run kill @s
execute unless score #server devmode matches 1 at @e[tag=8onetimepitcreator] run fill ~ ~-1 ~ ~ ~-8 ~ air
execute unless score #server devmode matches 1 as @e[tag=8onetimepitcreator] run kill @s
execute unless score #server devmode matches 1 at @e[tag=onetimepitcreator] run fill ~ ~-1 ~ ~ ~-8 ~ air
execute unless score #server devmode matches 1 as @e[tag=onetimepitcreator] run kill @s
#voidcreator
execute unless score #server devmode matches 1 at @e[tag=voidcreator] run fill ~ ~ ~ ~ 0 ~ air
execute unless score #server devmode matches 1 at @e[tag=voidcreator] run kill @n[type=glow_item_frame]