# Diaithir Ultimine

Clean-room vein mining (stands in for FTB Ultimine, all rights reserved). MIT. No code taken.

`/ultimine` toggles it. With it on, breaking a block with the right tool also breaks connected blocks of the same kind (diagonals count, 64 at most). Each block wears the tool as a normal break does and costs a little hunger. Every extra block goes through normal break events, so claims and other protections still apply.

Tested headless with a fake player (`/ultimine selftest`): off breaks one block, on breaks all 12 of a stone vein with 12 wear and leaves the dirt next to it. No shape modes or keybind yet (needs a client).
