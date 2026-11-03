# Diaithir Look-At

Client-only box at the top of the screen naming the block or creature under the crosshair, its mod, a creature's health, and stored energy when a block exposes it (stands in for Jade, CC BY-NC-SA). MIT. Clean-room.

Other modules add lines with a message, nothing to compile against:
`InterModComms.sendTo("diaithirlookat", "block/<namespace:path>", () -> (BiFunction<Level, BlockPos, List<String>>) (level, pos) -> List.of("..."))`. diaithir-waypoints does this for the waypoint stone (shows its name). Claim owner is not shown: it lives on the server and needs a sync packet.

Compiles on JDK 25. The drawn box needs a client: check at the first full-pack join that it shows.
