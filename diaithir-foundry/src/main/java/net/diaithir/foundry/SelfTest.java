package net.diaithir.foundry;

/** Static selftest of the pure logic. Run: java net.diaithir.foundry.SelfTest */
public final class SelfTest {
    private SelfTest() {
    }

    private static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        check("copper".equals(MetalRegistry.metalFromTagPath("ingots/copper")), "ingot tag");
        check(MetalRegistry.metalFromTagPath("ores/copper") == null, "ore tag is not an ingot tag");
        check(MetalRegistry.metalFromTagPath("ingots/") == null, "empty metal");
        check(MetalRegistry.metalFromTagPath("ingots/a/b") == null, "nested metal");
        check(MetalRegistry.meltYield("ores") == 180, "ore yield");
        check(MetalRegistry.meltYield("nuggets") == 10, "nugget yield");
        check(MetalRegistry.meltYield("bogus") == 0, "unknown group");
        check(Shape.INGOT.cost() == 90 && Shape.BLOCK.cost() == 810 && Shape.GEAR.cost() == 360, "shape costs");
        check(FoundryControllerBlockEntity.accept("", 0, "iron", 90, 16000) == 90, "empty tank accepts");
        check(FoundryControllerBlockEntity.accept("iron", 100, "gold", 90, 16000) == 0, "mixing refused");
        check(FoundryControllerBlockEntity.accept("iron", 15950, "iron", 90, 16000) == 50, "capacity clamp");
        check("diaithirfoundry:cast_ingot".equals(IdMigration.migrate("productivemetalworks:ingot_cast")), "migration");
        check("other:thing".equals(IdMigration.migrate("other:thing")), "unknown id passes through");
        System.out.println("diaithirfoundry selftest OK");
    }
}
