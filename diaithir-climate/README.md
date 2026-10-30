# diaithir-climate

Carries the Diaithir room-temperature model into Homeostatic. A KubeJS script (`content/climate` in DiaithirRPG) registers a
`ClimateBridge.Provider`; a mixin on Homeostatic's `EnvironmentData` adds the room's offset from open air to the player's air
and local temperature. Without the provider, or without Homeostatic, the mod does nothing. MIT.
