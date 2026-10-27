package net.diaithir.woodvariants;

/** Marks a block you can sit on. */
public interface Seat {
    /** Seat top of the shipped furniture (9 sixteenths) plus the sitter offset found by testing. */
    double SEAT_TOP = 0.5625;

    /** Height of the seat top above the block floor, in blocks. */
    double seatHeight();
}
