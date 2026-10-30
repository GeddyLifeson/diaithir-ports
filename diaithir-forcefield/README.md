# Diaithir Forcefield

A clean-room force-field projector for NeoForge 26.1.2, written from public descriptions of the idea only (no code, textures or names of any other mod). MIT.

The projector (Skardovel) takes FE through the NeoForge energy capability and raises a one-block-thick shell of field blocks (Skarnith) around itself: a cube, a sphere or a wall in front of it. Whoever places it is the owner. Use it bare-handed to switch it on or off, sneak and use to change shape. Radius starts at 6.

The owner, the owner's scoreboard team (switchable) and listed guests pass. Hostile mobs and other players are put back outside every few ticks. Upkeep is about 20 FE per tick for each 1000 shell blocks. If power runs out the shell drops, and it also drops when the projector is broken.

Modules (use on the projector, up to 4 each): range (+4 radius, max 32), strength (+1 layer), mob filter (only hostile mobs are held out), guest list (named players pass), capacitor (+40000 FE buffer), sting (blocked intruders take damage).

Performance: field blocks are plain blocks with no block entity, no tick, no light and no collision. The projector places them in batches over a few passes, and between passes does nothing. One pass every 4 ticks looks at the living entities in the shell's bounding box. The shell is capped by `maxFieldBlocks` (default 12000): a shape that needs more is shrunk to fit.

Commands (look at the projector): `/diaithirforcefield status`, `allow <player>`, `deny <player>`, `team <true|false>`; operators `purge <radius>`; owners `selftest` (places a projector high above spawn, powers it through the capability and checks the shell, owner, team, guest, mob filter, sting, range, wall, power loss and removal with fake players, then removes it). Server config `diaithirforcefield-server.toml` has every number.

Textures, models and recipes are written by `tools/gen_textures.py`. Display names are invented words; ids are English. The Age gate is `data/diaithirforcefield/age_gate.json` (Age VI).

Source: https://github.com/GeddyLifeson/diaithir-ports/tree/diaithircompat/diaithir-forcefield. Licence: MIT.
