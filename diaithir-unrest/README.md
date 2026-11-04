# Diaithir Unrest

Clean-room replacement for pollution (plans/FACTORIO_TECH.md 2f). Every chunk keeps an Unrest score. Machines in the `noisy_low/mid/high` block tags add to their chunk each scan (only chunks near players are scanned), explosions add by radius, blocks in the `quiet` tag subtract. Unrest spreads to four neighbours, decays, and is read with the Meter (use it). Nests absorb Unrest from nearby chunks, grow in tiers with what they absorbed (tier cap is a config raised by Age) and post `NestWaveEvent` for the Burrowkin creature module, aimed at the loudest chunk. All numbers are in `diaithirunrest-server.toml`; tags extend by datapack or KubeJS. Self test: `/diaithirunrest selftest`.

Nest placement (far, unclaimed chunks) and the Burrowkin creature are separate modules; this one only provides the score, the Meter, the nest block and the event.
Pollution of the Realms (CC-BY-ND), Pollution Plus (MIT) and others were not used or read.
