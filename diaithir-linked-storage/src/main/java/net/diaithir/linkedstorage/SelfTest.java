package net.diaithir.linkedstorage;

import java.util.Set;
import java.util.UUID;

/** Static selftest of the pure logic. Run with main(). */
public final class SelfTest {
    private SelfTest() {}

    private static void check(boolean ok, String what) {
        if (!ok) throw new AssertionError(what);
    }

    public static void main(String[] args) {
        int f = Frequency.pack(3, 9, 15);
        check(Frequency.slot(f, 0) == 3 && Frequency.slot(f, 1) == 9 && Frequency.slot(f, 2) == 15, "pack/slot");
        check(Frequency.with(f, 1, 4) == Frequency.pack(3, 4, 15), "with");
        check(Frequency.cycle(Frequency.pack(0, 0, 15), 2) == Frequency.pack(0, 0, 0), "cycle wraps");
        check(Frequency.CHANNELS == 4096, "4096 channels");

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        ChannelKey k1 = ChannelKey.of(LinkKind.CHEST, Lock.OWNER, a, f);
        ChannelKey k2 = ChannelKey.of(LinkKind.CHEST, Lock.TEAM, a, f);
        ChannelKey k3 = ChannelKey.of(LinkKind.CHEST, Lock.OWNER, b, f);
        check(k1.equals(k2), "owner and team share one scope");
        check(!k1.equals(k3), "different owners do not share");
        check(ChannelKey.of(LinkKind.CHEST, Lock.PUBLIC, a, f).equals(ChannelKey.of(LinkKind.CHEST, Lock.PUBLIC, b, f)), "public shared by all");
        check(!ChannelKey.of(LinkKind.TANK, Lock.OWNER, a, f).id().equals(k1.id()), "kinds are separate");

        check(Access.allowed(Lock.OWNER, a, Set.of(), a), "owner opens own");
        check(!Access.allowed(Lock.OWNER, a, Set.of(b), b), "owner lock blocks team");
        check(Access.allowed(Lock.TEAM, a, Set.of(b), b), "team lock admits member");
        check(!Access.allowed(Lock.TEAM, a, Set.of(), b), "team lock blocks outsider");
        check(Access.allowed(Lock.PUBLIC, a, Set.of(), b), "public admits all");
        check(Lock.OWNER.next() == Lock.TEAM && Lock.PUBLIC.next() == Lock.OWNER, "lock cycles");

        check(IdRemap.migrate("entangled:block").isPresent(), "remap entangled");
        check(IdRemap.migrate("linkedchests:linked_chest").orElse("").equals("diaithirlinkedstorage:linked_chest"), "remap linkedchests");
        check(IdRemap.migrate("nothing:here").isEmpty(), "unknown stays unmapped");
        System.out.println("linkedstorage selftest ok");
    }
}
