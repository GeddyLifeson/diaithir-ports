package net.diaithir.crashtools.simplebackups.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.diaithir.crashtools.simplebackups.BackupChain;
import net.diaithir.crashtools.simplebackups.BackupChainManager;
import net.diaithir.crashtools.simplebackups.BackupData;
import net.diaithir.crashtools.simplebackups.SimpleBackups;
import net.diaithir.crashtools.simplebackups.config.BackupType;
import net.diaithir.crashtools.simplebackups.config.CommonConfig;
import net.diaithir.crashtools.simplebackups.merging.MergerBase;
import net.diaithir.crashtools.simplebackups.merging.SbkMerger;
import net.diaithir.crashtools.simplebackups.merging.ZipMerger;
import net.diaithir.crashtools.simplebackups.merging.ZstdMerger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class MergeCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("mergeBackups")
                .then(Commands.argument("chain", StringArgumentType.string()).suggests((stack, builder) -> {
                            String levelId = stack.getSource().getServer().storageSource.getLevelId();
                            BackupChainManager manager = BackupChainManager.get(levelId);

                            return SharedSuggestionProvider.suggest(manager.getChains().stream().map(chain -> chain.getParentFolder().getFileName().toString()), builder);
                        })
                        .executes(new MergeCommand()));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        // Check if only modified files should be backed up
        if (CommonConfig.backupType() == BackupType.FULL_BACKUPS) {
            throw new SimpleCommandExceptionType(Component.translatable("simplebackups.commands.only_modified")).create();
        }

        BackupData data = BackupData.get(commandContext.getSource().getServer());

        // Check if a merge operation is already in progress
        if (data.isMerging()) {
            throw new SimpleCommandExceptionType(Component.translatable("simplebackups.commands.is_merging")).create();
        }

        try {
            String chainName = commandContext.getArgument("chain", String.class);
            BackupChainManager manager = BackupChainManager.get(commandContext.getSource().getServer().storageSource.getLevelId());
            for (BackupChain chain : manager.getChains()) {
                if (chain.getParentFolder().getFileName().toString().equals(chainName)) {
                    MergerBase base = switch(chain.getFormat()) {
                        case ZIP -> new ZipMerger(chain, commandContext);
                        case ZSTD -> new ZstdMerger(chain, commandContext);
                        case SBK -> new SbkMerger(chain, commandContext);
                    };

                    data.startMerging();
                    base.merge();
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            SimpleBackups.LOGGER.error("Invalid chain name: {}", commandContext.getArgument("chain", String.class), e);
            data.stopMerging();
            return 0;
        }

        data.stopMerging();
        return 1;
    }
}
