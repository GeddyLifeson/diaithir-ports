package net.diaithir.ships.cargo

import com.fasterxml.jackson.annotation.JsonAutoDetect

/**
 * Ship attachment of a cargo-carrying ship: where its deck bay sits in ship space and the saved loads (SNBT of the bay).
 * Plain fields only, so it saves with the ship even when the containers API is absent.
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
class ShipCargo {
    var ready = false
    /** Ship-space block of the bay's (0,0,0) cell: the deck's minimum corner, one above the hull's top. */
    var ox = 0
    var oy = 0
    var oz = 0
    var w = 0
    var h = 0
    var l = 0
    var maxKg = 0.0
    var saved = ""
}
