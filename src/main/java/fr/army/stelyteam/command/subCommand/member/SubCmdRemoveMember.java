package fr.army.stelyteam.command.subCommand.member;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.command.SubCommand;
import fr.army.stelyteam.team.Member;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.manager.MessageManager;

public class SubCmdRemoveMember extends SubCommand {

    private MessageManager messageManager;

    public SubCmdRemoveMember(StelyTeamPlugin plugin) {
        super(plugin);
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        args[0] = "";

        if (args.length < 3){
            player.sendMessage(messageManager.getMessage("commands.stelyteam_removemember.usage"));
        }else{
            Team team = Team.init(args[1]);
            if (team != null){
                if (team.isTeamMember(args[2])){
                    team.removeMember(args[2]);
                    player.sendMessage(messageManager.getReplaceMessage("commands.stelyteam_removemember.output", args[2]));
                }else{
                    player.sendMessage(messageManager.getMessage("commands.stelyteam_removemember.not_in_team"));
                }
            }else{
                player.sendMessage(messageManager.getMessage("common.team_not_exist"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender.isOp() && args.length == 3){
            if (args[0].equals("removemember")){
                Team team = Team.init(args[1]);
                List<String> result = new ArrayList<>();
                for (Member member : team.getTeamMembers()) {
                    String memberName = member.getMemberName();
                    Integer memberRank = member.getTeamRank();
                    if (memberRank > 0 && memberName.toLowerCase().startsWith(args[2].toLowerCase())){
                        result.add(memberName);
                    }
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public boolean isOpCommand() {
        return true;
    }

}
