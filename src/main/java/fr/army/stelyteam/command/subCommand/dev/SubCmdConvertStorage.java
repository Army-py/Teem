package fr.army.stelyteam.command.subCommand.dev;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.command.SubCommand;
import fr.army.stelyteam.team.Storage;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.manager.CacheManager;
import fr.army.stelyteam.utils.manager.database.DatabaseManager;
import fr.army.stelyteam.utils.manager.serializer.ItemStackSerializer;

public class SubCmdConvertStorage extends SubCommand {
    private final CacheManager cacheManager;
    private final DatabaseManager databaseManager;
    private final ItemStackSerializer serializer;

    public SubCmdConvertStorage(StelyTeamPlugin plugin) {
        super(plugin);
        this.cacheManager = plugin.getCacheManager();
        this.databaseManager = plugin.getDatabaseManager();
        this.serializer = plugin.getSerializeManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        int converted = 0;
        int skippedNbt = 0;
        int skippedEmpty = 0;
        int failed = 0;

        final Set<Team> teams = databaseManager.getTeams();
        for (Team team : teams) {
            final Map<Integer, Storage> storages = databaseManager.getTeamStorages(team.getTeamUuid());
            if (storages == null) continue;

            for (Storage storage : storages.values()) {
                final byte[] content = storage.getStorageContent();
                if (content == null || content.length == 0) {
                    skippedEmpty++;
                    continue;
                }
                if (serializer.isNbtStorageFormat(content)) {
                    skippedNbt++;
                    continue;
                }

                try {
                    final byte[] convertedContent = serializer.convertLegacyStorageToNbt(content);
                    databaseManager.updateStorageContent(team.getTeamUuid(), storage.getStorageId(), convertedContent, null);
                    cacheManager.removeStorage(storage);
                    converted++;
                } catch (final RuntimeException exception) {
                    failed++;
                    plugin.getLogger().warning("Unable to convert storage " + storage.getStorageId()
                            + " for team " + team.getTeamName() + " (" + team.getTeamUuid() + "): "
                            + exception.getMessage());
                }
            }
        }

        sender.sendMessage("§aConversion des stockages terminée.");
        sender.sendMessage("§7Convertis: §a" + converted
                + " §7| Déjà NBT: §e" + skippedNbt
                + " §7| Vides: §e" + skippedEmpty
                + " §7| Erreurs: §c" + failed);
        if (failed > 0) {
            sender.sendMessage("§cCertains stockages n'ont pas pu être convertis. Consulte la console.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean isOpCommand() {
        return true;
    }
}
