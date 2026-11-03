package net.diaithir.upgrades;

/** Flavour of a module. Same maths, different item: circuit, ogham stone or spice. */
public enum Theme {
    TECH("circuit"), MAGIC("ogham"), FOOD("spice");

    public final String itemWord;

    Theme(String itemWord) { this.itemWord = itemWord; }
}
