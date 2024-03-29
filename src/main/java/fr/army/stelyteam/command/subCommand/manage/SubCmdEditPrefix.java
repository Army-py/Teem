package fr.army.stelyteam.command.subCommand.manage;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.command.SubCommand;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.builder.ColorsBuilder;
import fr.army.stelyteam.utils.manager.MessageManager;

public class SubCmdEditPrefix extends SubCommand {

    private MessageManager messageManager;
    private ColorsBuilder colorsBuilder;

    public SubCmdEditPrefix(StelyTeamPlugin plugin) {
        super(plugin);
        this.messageManager = plugin.getMessageManager();
        this.colorsBuilder = new ColorsBuilder(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        args[0] = "";

        if (args.length < 3){
            player.sendMessage(messageManager.getMessage("commands.stelyteam_editprefix.usage"));
        }else{
            Team team = Team.init(args[1]);
            if (team != null){
                team.updateTeamPrefix(args[2]);
                player.sendMessage(messageManager.getReplaceMessage("commands.stelyteam_editprefix.output", colorsBuilder.replaceColor(args[2])));
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
