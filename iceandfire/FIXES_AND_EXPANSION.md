# Fixes and Expansion

This list is for comparing with this fork and origin mod.

## Mobs

- Fix dragons lag when breathing. (Forget to sqrt when calculate distance.)
- Expand dragon type API.
- Fix Gorgon will break all armors.
- Fix stone status related crashes.

### Multipart Rewrite

- Fix multipart entities lag. (Remove bounding box entities and respawn them every tick.)
- Fix multipart spams logs.

## WorldGen

- Fix Mausoleums sometimes can be broken with pickaxe.

### Structure System Rewrite

- Switch worldgen system to vanilla one.
- Fix dragon caves like being cut.
- Fix conflict with Distance Horizons.
- Fix conflict with c2me.
- Fix crash when at world border.

## Integration

- Add better integration for jade and fix information display issue for multipart entities.
- Add EMI support.

## Render

- Fix dragon breath render too long if dragon have breathed for a long time.
- Fix armor sometimes wrongly render.
- Make title screen render more capable.

### Tabula Model Loader Rewrite

- Fix tabula models cannot load correctly in specific environment.(Such as Develop Mode.)

## Misc

- Add some food for Farmer's Delight.
- Add netherite armors.
- Merge Dragon Seekers.