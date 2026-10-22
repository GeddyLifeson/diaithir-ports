# NOTICE

This is a modified copy of Vampirism by TeamLapen (https://github.com/TeamLapen/Vampirism, tag
`vampirism/26.1.2-2.0.0-alpha.2`, commit 0eb507ce), licensed under the GNU Lesser General Public License v3.0 (LICENSE).
This modified version is under the same licence.

Changes for Diaithir (2026-10-22), built as `Vampirism-26.1.2-2.0.0-alpha.2-diaithir.1.jar`:

- `SplashManagerMixin.vampirism$prepare` copies the splash list before adding Vampirism's lines. Vanilla 26.1.2
  returns an immutable list (`Stream.toList`), so the add threw `UnsupportedOperationException`, the first client
  resource reload failed, Minecraft dropped every selected resource pack and the game came up on a black title
  screen without honouring quick-play.

Build: JDK 25, `./gradlew :vampirism:jar -Pvampirism_version=26.1.2-2.0.0-alpha.2-diaithir.1 -Pfactions_version=26.1.2-1.0.0-alpha.3`
(the factions version keeps the FactionAPI dependency at `[26.1.2-1.0.0-alpha.3,)`, the FactionAPI build in the pack).
