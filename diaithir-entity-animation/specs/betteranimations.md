Better Animations Collection: clean-room design spec (module `diaithir-entity-animation`, package `betteranimations`)

Source of behaviour: the public feature list of Better Animations Collection (code MPL-2.0, assets all rights reserved). Nothing is copied; the list of
animated elements below is taken from the class names of the 26.1 branch, the motion is our own.

1) Elements (each is an independent client toggle)
- Creeper: wobbles and swells before the fuse blows. Slime and magma cube: jiggle on landing; magma cube stacks as a burger.
- Cow: udder sways. Pig: snout wiggles. Chicken: head bucks while walking. Sheep: kneels to graze. Llama: spits with a head snap.
- Cat and ocelot: tail follows turns with lag. Wolf: playful ear and tail motion. Horse and donkey: familiar mane and tail swing.
- Squid and ghast: tentacles trail behind movement. Enderman: arms flail when angry or carrying a block.
- Villager and iron golem: nose wiggles. Snow golem: stick arms swing. Humanoids, zombies, drowned, piglins and spiders: visible knees.

2) Motion model
- Every moving part follows a target through a critically damped spring (`Motion.spring`): no overshoot unless the element asks for it, frame-rate independent.
- Periodic motion (`Motion.wobble`) uses a sine of game time plus per-entity phase from the entity id so herds do not move in step.
- Jiggle (`Motion.jiggle`) is a damped oscillation started by an impulse (landing, hit).

3) Config (client): `enabled`, one boolean per element, `intensity` (0.0 to 2.0 multiplier on amplitudes), `maxDistance` (default 32 blocks).

4) Status
- Stage 1 (this change): the motion maths, with a plain-Java check, and this spec. No renderer hook yet.
- Stage 2: per-element model poses through the same extractor path as the 3D skin layers feature; needs a real client to check, so it waits for one.

Better Animations Collection stage 1 (clean-room): see specs/betteranimations.md. `betteranimations/Motion.java` holds the spring, wobble and jiggle maths; `tools/MotionCheck.java` checks it without a client (ok=7 bad=0). No renderer hook yet.
