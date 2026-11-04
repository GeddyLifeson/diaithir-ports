# Jupiter

Jupiter is a powerful, auto sync config library.

**IMPORTANT NOTICE: Jupiter V2 is not compatible with mods which based on V1!**

## Features

### For players

- Convenient in-game config edit screens.
- Auto sync with server if you are connect to dedicated servers & you have proper permissions.

### For develops

**For how to use, see [document](https://docs.iafenvoy.com/docs/library/jupiter).**

- Simple creation of config instance.
- Config support `int`, `double`, `string`,`list` and so on.
- Customized codec based config entry: You can add config entry for any value type.
- Permission control for dedicate server config.

### Extra Config System

Extra config system aims at loading config files from other config systems. Configs loaded by this system can also use
Jupiter's features such as auto generated config screens and dedicated server sync.

Currently supported:

- (Neo)Forge config system (Also capable with `Forge Config API Port`)
- Cloth Config API

## Special thanks

- `masady`: Author of `Malilib`, config screen designs is partly based on it.
- `Fuzs`: Author of `Forge Config API Port`: `Jupiter` can load (Neo)Forge configs more convenient with its universal
  APIs.

## Discord

https://discord.gg/NDzz2upqAk