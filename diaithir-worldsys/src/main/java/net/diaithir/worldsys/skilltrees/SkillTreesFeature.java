package net.diaithir.worldsys.skilltrees;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Four skills that gain XP from play and grant small permanent attribute bonuses. */
public final class SkillTreesFeature {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, "diaithirworldsys");
    static final DeferredHolder<AttachmentType<?>, AttachmentType<SkillData>> DATA = ATTACHMENTS.register("skills",
            () -> AttachmentType.builder(() -> SkillData.EMPTY).serialize(SkillData.CODEC.fieldOf("xp")).copyOnDeath().build());

    private SkillTreesFeature() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, SkillTreesFeature::onBreak);
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, SkillTreesFeature::onKill);
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, SkillTreesFeature::onTick);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) SkillBonuses.apply(p, p.getData(DATA));
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerRespawnEvent.class, e -> {
            if (e.getEntity() instanceof ServerPlayer p) SkillBonuses.apply(p, p.getData(DATA));
        });
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, e -> commands(e.getDispatcher()));
    }

    private static void onBreak(BlockDropsEvent e) {
        if (!(e.getBreaker() instanceof ServerPlayer p)) return;
        BlockState s = e.getState();
        if (s.is(BlockTags.COAL_ORES) || s.is(BlockTags.IRON_ORES) || s.is(BlockTags.COPPER_ORES) || s.is(BlockTags.GOLD_ORES)
                || s.is(BlockTags.REDSTONE_ORES) || s.is(BlockTags.LAPIS_ORES) || s.is(BlockTags.DIAMOND_ORES) || s.is(BlockTags.EMERALD_ORES)) {
            gain(p, Skill.MINING, 5);
        } else if (s.is(BlockTags.BASE_STONE_OVERWORLD)) {
            gain(p, Skill.MINING, 1);
        } else if (s.getBlock() instanceof CropBlock crop && crop.isMaxAge(s)) {
            gain(p, Skill.FARMING, 2);
        }
    }

    private static void onKill(LivingDeathEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer p)) return;
        LivingEntity victim = e.getEntity();
        gain(p, Skill.COMBAT, victim instanceof Enemy ? 4 : 1);
    }

    private static void onTick(PlayerTickEvent.Post e) {
        if (!(e.getEntity() instanceof ServerPlayer p) || p.tickCount % 20 != 0) return;
        if (p.isPassenger() || p.isFallFlying() || p.getDeltaMovement().horizontalDistanceSqr() > 0.002) gain(p, Skill.EXPLORING, 1);
    }

    static void gain(ServerPlayer p, Skill skill, int amount) {
        SkillData before = p.getData(DATA);
        int oldLevel = before.level(skill);
        if (oldLevel >= Skill.MAX_LEVEL) return;
        SkillData after = before.with(skill, before.xp(skill) + amount);
        p.setData(DATA, after);
        int newLevel = after.level(skill);
        if (newLevel > oldLevel) {
            SkillBonuses.apply(p, after);
            p.sendOverlayMessage(Component.literal("Skill up: " + skill.key() + " level " + newLevel));
            p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.2F);
        }
    }

    private static void commands(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("skills")
                .executes(c -> {
                    ServerPlayer p = c.getSource().getPlayerOrException();
                    SkillData data = p.getData(DATA);
                    for (Skill s : Skill.values()) {
                        int lvl = data.level(s);
                        c.getSource().sendSuccess(() -> Component.literal(s.key() + ": level " + lvl), false);
                    }
                    return 1;
                })
                .then(Commands.literal("reset").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("player", EntityArgument.player()).executes(c -> {
                            ServerPlayer p = EntityArgument.getPlayer(c, "player");
                            p.setData(DATA, SkillData.EMPTY);
                            SkillBonuses.apply(p, SkillData.EMPTY);
                            c.getSource().sendSuccess(() -> Component.literal("Skills reset."), true);
                            return 1;
                        }))));
    }
}
