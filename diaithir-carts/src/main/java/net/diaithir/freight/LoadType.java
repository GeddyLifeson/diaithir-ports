package net.diaithir.freight;

import java.util.List;

/**
 * One kind of unit load. Sizes are whole cells (1 cell = 1 m cube, a block; a container is a ring of wall cells round a 2 by 2 walkable interior, so it is drawn larger than real); masses are kg, volumes litres. A load that has {@link #inner} cells is a
 * container: it carries its own {@link CargoBay}, so a ship hold, flatcar or trailer bed can take containers and a container can take pallets, barrels and chests.
 *
 * @param w width, h height, l length in cells (before any quarter turn)
 * @param tareKg empty mass
 * @param payloadKg most net mass it can carry
 * @param stackKg most gross mass that may rest on top of it (ISO corner posts for containers)
 * @param itemSlots slots of a loose-item load (pallet, crate, chest, barrel); 0 for containers
 * @param fluidL fluid capacity of a tank, 0 otherwise
 * @param inner interior cells {w,h,l} of a container (outside size minus a wall ring), or null; a tank has none
 * @param cold true for a reefer (refrigeration unit, draws {@link #powerW})
 * @param minAge Age (1..8) a station may first make it
 */
public record LoadType(String id, Kind kind, int w, int h, int l, double tareKg, double payloadKg, double stackKg, int itemSlots, int fluidL,
                       int[] inner, boolean cold, double powerW, int minAge) {
    public enum Kind { PALLET, CRATE, CHEST, BARREL, CONTAINER, REEFER, TANK }

    public boolean container() { return inner != null; }

    /** Rows of the interior taken by refrigeration equipment. */
    public int unitRows() { return cold ? 1 : 0; }

    public boolean looseItems() { return itemSlots > 0; }

    public int cells() { return w * h * l; }

    public int innerCells() { return inner == null ? 0 : inner[0] * inner[1] * inner[2]; }

    private static LoadType small(String id, Kind k, int w, int l, double tare, double payload, double stack, int slots, int age) {
        return new LoadType(id, k, w, 1, l, tare, payload, stack, slots, 0, null, false, 0, age);
    }

    /** A container: walls are a ring of whole cells round the interior, so the outside is {@code inner + 2} cells each way. */
    private static LoadType box(String id, Kind k, int length, int[] inner, double tare, double payload, boolean cold, int fluid, int age) {
        // ISO rating: 192 t may rest on the corner posts of either size
        return new LoadType(id, k, 4, 4, length, tare, payload, 192_000, 0, fluid, inner, cold, cold ? 4_000 : 0, age);
    }

    /** The catalogue. Pallets, barrels and crates are early; containers arrive with the container ships (Age VI). */
    public static final List<LoadType> ALL = List.of(
            small("pallet", Kind.PALLET, 1, 1, 25, 1_500, 2_000, 18, 2),
            small("barrel", Kind.BARREL, 1, 1, 20, 300, 600, 18, 2),
            small("chest", Kind.CHEST, 1, 1, 30, 450, 900, 27, 1),
            small("crate", Kind.CRATE, 1, 1, 55, 1_200, 2_400, 54, 2),
            box("container_20", Kind.CONTAINER, 7, new int[]{2, 2, 5}, 2_300, 28_180, false, 0, 6),
            box("container_40", Kind.CONTAINER, 13, new int[]{2, 2, 11}, 3_800, 26_680, false, 0, 6),
            box("reefer_20", Kind.REEFER, 7, new int[]{2, 2, 4}, 3_000, 27_480, true, 0, 6),
            box("reefer_40", Kind.REEFER, 13, new int[]{2, 2, 10}, 4_800, 25_680, true, 0, 6),
            box("tank_20", Kind.TANK, 7, null, 3_700, 32_300, false, 26_000, 6));

    public static LoadType byId(String id) {
        for (LoadType t : ALL) if (t.id.equals(id)) return t;
        return null;
    }
}
