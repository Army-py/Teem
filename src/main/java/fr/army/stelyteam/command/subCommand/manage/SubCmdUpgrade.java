package fr.army.stelyteam.command.subCommand.manage;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.command.SubCommand;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.manager.MessageManager;

public class SubCmdUpgrade extends SubCommand {

    private YamlConfiguration config;
    private MessageManager messageManager;

    public SubCmdUpgrade(StelyTeamPlugin plugin) {
        super(plugin);
        this.config = plugin.getConfig();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        args[0] = "";

        if (args.length == 1){
            player.sendMessage(messageManager.getMessage("commands.stelyteam_upgrade.usage"));
        }else{
            String teamName = String.join("", args);
            Team team = Team.init(teamName);
            if (team != null){
                Integer maxUpgrades = config.getConfigurationSection("inventories.upgradeTotalMembers").getValues(false).size() - 1;
                Integer teamUpgrades = team.getImprovLvlMembers();
                if (maxUpgrades == teamUpgrades){
                    player.sendMessage(messageManager.getMessage("commands.stelyteam_upgrade.max_level"));
                }else{
                    team.incrementImprovLvlMembers();
                    player.sendMessage(messageManager.getReplaceMessage("commands.stelyteam_upgrade.output", teamName));
                }
            }else{
                player.sendMessage(messageManager.getMessage("common.team_not_exist"));
            }
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
