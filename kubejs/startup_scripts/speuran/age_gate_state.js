// State for server_scripts/speuran/age_gate.js. `global` can only be assigned in startup scripts (a server script that assigns to it
// fails with "'global' cannot be assigned to in client or server scripts"), and startup scripts run once per launch, so an array made
// here survives every /reload and script reload of the server scripts, which reset their own top-level variables.
global.speuranHeld = []
