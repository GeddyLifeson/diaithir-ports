package net.diaithir.routing;

/** The ten router module kinds. */
public enum ModuleKind {
    EXTRACTOR("extractor"),
    SENDER("sender"),
    PULLER("puller"),
    FILTER("filter"),
    SORTER("sorter"),
    VACUUM("vacuum"),
    VOID("void"),
    FLUID("fluid"),
    ENERGY("energy"),
    ROUND_ROBIN("round_robin");

    private final String id;

    ModuleKind(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
