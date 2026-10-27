package net.diaithir.woodvariants;

/** The seven pieces made for every wood. Boxes face north; the chair back and bench stand on the south side. */
public enum Piece {
    CHAIR("chair", "Sèiter"),
    STOOL("stool", "Kriepun"),
    BENCH("bench", "Beingi"),
    TABLE("table", "Bòird"),
    WALL("wall", "Balda"),
    VERTICAL("vertical_planks", "Klàr dìrech"),
    FRAMED("framed_planks", "Klàr kèisi");

    public final String suffix;
    public final String dia;

    Piece(String suffix, String dia) {
        this.suffix = suffix;
        this.dia = dia;
    }

    public static final int[][] CHAIR_BOXES = {
        Box.b(2, 7, 2, 14, 9, 14), Box.b(2, 0, 2, 4, 7, 4), Box.b(12, 0, 2, 14, 7, 4),
        Box.b(2, 0, 12, 4, 7, 14), Box.b(12, 0, 12, 14, 7, 14), Box.b(2, 9, 12, 14, 16, 14)};
    public static final int[][] STOOL_BOXES = {
        Box.b(3, 7, 3, 13, 9, 13), Box.b(3, 0, 3, 5, 7, 5), Box.b(11, 0, 3, 13, 7, 5),
        Box.b(3, 0, 11, 5, 7, 13), Box.b(11, 0, 11, 13, 7, 13)};
    public static final int[][] BENCH_BOXES = {
        Box.b(0, 7, 3, 16, 9, 13), Box.b(1, 0, 4, 3, 7, 6), Box.b(13, 0, 4, 15, 7, 6),
        Box.b(1, 0, 10, 3, 7, 12), Box.b(13, 0, 10, 15, 7, 12)};
    public static final int[][] TABLE_BOXES = {
        Box.b(0, 13, 0, 16, 16, 16), Box.b(1, 0, 1, 4, 13, 4), Box.b(12, 0, 1, 15, 13, 4),
        Box.b(1, 0, 12, 4, 13, 15), Box.b(12, 0, 12, 15, 13, 15)};
}
