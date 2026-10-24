Valkyrien Skies 2.5.0-diaithir.8 (LGPL-3.0). Full source of the Diaithir builds: https://github.com/GeddyLifeson/diaithir-ports/tree/valkyrienskies (commit c4a3f1a, "Register runtime-created levels with the ship world only at a legal tick stage; version 2.5.0-diaithir.8"). The jar's sha256 is f08ea3ec30a0524b65ce30d970bcbae58ea123606f77d80b2ad76bdacabb4c8d.
This folder holds the changes made after diaithir.5, which was the last version the branch carried when update 1 was built:
- MixinChunkPosBounds.java: ChunkPos bounds mixin (diaithir.6).
- PatchClaims.java, patch-claims.sh, NOTES.md: the post-build ASM step that moves the ship claim area inside Tellus' coordinate limits (diaithir.7).
- PendingDimensions.java, MixinServerLevel.java and diaithir.8-over-diaithir.7.patch: runtime-created levels are registered with the ship world only at a legal tick stage (diaithir.8). NOTES.md describes both changes.
The jar bundles the closed vs-core and Krunch physics binaries unchanged, as the official Valkyrien Skies releases do.
