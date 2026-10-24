package org.valkyrienskies.mod.mixin.feature.wave_spawning;

import net.minecraft.world.entity.raid.Raids;
import org.spongepowered.asm.mixin.Mixin;

// TODO Phase 4: 26.2 で Raids が SavedData 化し、per-instance の `level` フィールドが削除された。
// getNearbyRaid(BlockPos, int) にも ServerLevel 引数がなく、各 Raid の getCenter() を
// ワールド座標変換するために必要な level を mixin 内で取得できない。
// 船上レイドの近接判定座標変換は一旦ドロップ(要再設計: ServerLevel を保持する呼び出し元
// 経路 - Raids.tick(ServerLevel) や getOrCreateRaid(ServerLevel, BlockPos) 側 - に処理を移す)。
@Mixin(Raids.class)
public abstract class MixinRaids {
}
