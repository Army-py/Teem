package fr.army.stelyteam.command.subCommand.info;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.command.SubCommand;
import fr.army.stelyteam.menu.impl.TeamListMenu;
import fr.army.stelyteam.utils.manager.CacheManager;

public class SubCmdList extends SubCommand {

    private CacheManager cacheManager;

    public SubCmdList(StelyTeamPlugin plugin) {
        super(plugin);
        this.cacheManager = plugin.getCacheManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String playerName = player.getName();

        cacheManager.removePage(cacheManager.getPage(playerName));
        new TeamListMenu(player, null).openMenu(0);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return null;
    }

    @Override
    public boolean isOpCommand() {
        return false;
    }
}
