package fr.army.stelyteam.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.commands.subCommands.help.SubCmdAdmin;
import fr.army.stelyteam.commands.subCommands.help.SubCmdHelp;
import fr.army.stelyteam.commands.subCommands.info.SubCmdInfo;
import fr.army.stelyteam.commands.subCommands.info.SubCmdList;
import fr.army.stelyteam.commands.subCommands.info.SubCmdMoney;
import fr.army.stelyteam.commands.subCommands.manage.SubCmdDelete;
import fr.army.stelyteam.commands.subCommands.manage.SubCmdDowngrade;
import fr.army.stelyteam.commands.subCommands.manage.SubCmdEditName;
import fr.army.stelyteam.commands.subCommands.manage.SubCmdEditPrefix;
import fr.army.stelyteam.commands.subCommands.manage.SubCmdUpgrade;
import fr.army.stelyteam.commands.subCommands.member.SubCmdAddMember;
import fr.army.stelyteam.commands.subCommands.member.SubCmdChangeOwner;
import fr.army.stelyteam.commands.subCommands.member.SubCmdRemoveMember;
import fr.army.stelyteam.commands.subCommands.team.SubCmdAccept;
import fr.army.stelyteam.commands.subCommands.team.SubCmdDeny;
import fr.army.stelyteam.commands.subCommands.utility.SubCmdHome;
import fr.army.stelyteam.commands.subCommands.utility.SubCmdVisual;
import fr.army.stelyteam.utils.TeamMembersUtils;
import fr.army.stelyteam.utils.builder.InventoryBuilder;
import fr.army.stelyteam.utils.manager.CacheManager;
import fr.army.stelyteam.utils.manager.MessageManager;
import fr.army.stelyteam.utils.manager.SQLManager;
import fr.army.stelyteam.utils.manager.SQLiteManager;

public class CmdStelyTeam implements CommandExecutor, TabCompleter {

    private StelyTeamPlugin plugin;
    private CacheManager cacheManager;
    private SQLManager sqlManager;
    private SQLiteManager sqliteManager;
    private MessageManager messageManager;
    private InventoryBuilder inventoryBuilder;
    private TeamMembersUtils teamMembersUtils;
    private Map<String, Object> subCommands;
    // private Map<String, Object> subCommandsOp;


    public CmdStelyTeam(StelyTeamPlugin plugin) {
        this.plugin = plugin;
        this.cacheManager = plugin.getCacheManager();
        this.sqlManager = plugin.getSQLManager();
        this.sqliteManager = plugin.getSQLiteManager();
        this.messageManager = plugin.getMessageManager();
        this.inventoryBuilder = new InventoryBuilder(plugin);
        this.teamMembersUtils = new TeamMembersUtils(plugin);
        this.subCommands = new HashMap<>();
        initSubCommands();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player){
            Player player = (Player) sender;
            String playerName = player.getName();

            if (cacheManager.isInConversation(playerName)){
                player.sendRawMessage(messageManager.getMessage("common.no_command_in_conv"));
                return true;
            }

            if (!sqliteManager.isRegistered(player.getName())) {
                sqliteManager.registerPlayer(player);
            }

            
            if (args.length == 0){
                teamMembersUtils.openMainInventory(player);
            }else{
                if (subCommands.containsKey(args[0])){
                    SubCommand subCmd = (SubCommand) subCommands.get(args[0]);
                    if (subCmd.execute(player, args)){
                        return true;
                    }
                }else if (sender.isOp()){
                    if (subCommands.containsKey(args[0]) && ((SubCommand) subCommands.get(args[0])).isOpCommand()){
                        SubCommand subCmd = (SubCommand) subCommands.get(args[0]);
                        if (subCmd.execute(player, args)){
                            return true;
                        }
                    }
                }

                player.sendMessage(messageManager.getMessage("common.invalid_command"));
            }
        }
    return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1){
            List<String> result = new ArrayList<>();
            for (String subcommand : subCommands.keySet()) {
                if (subcommand.toLowerCase().toLowerCase().startsWith(args[0])){
                    result.add(subcommand);
                }
            }
            if (sender.isOp()){
                for (String subcommand : subCommands.keySet()) {
                    if (subcommand.toLowerCase().toLowerCase().startsWith(args[0]) && ((SubCommand) subCommands.get(subcommand)).isOpCommand()){
                        result.add(subcommand);
                    }
                }
            }
            return result;
        }else if (args.length == 2){
            if (sender.isOp()){
                if (subCommands.containsKey(args[0]) && ((SubCommand) subCommands.get(args[0])).isOpCommand()){
                    List<String> result = new ArrayList<>();
                    for (String teamID : sqlManager.getTeamsName()) {
                        if (teamID.toLowerCase().startsWith(args[1].toLowerCase())){
                            result.add(teamID);
                        }
                    }
                    return result;
                }
            }
        }
        if (args.length > 1 && subCommands.containsKey(args[0].toLowerCase())) {
            List<String> results = ((SubCommand) subCommands.get(args[0])).onTabComplete(sender, args);
            return results;
        } 
        return null;
    }


    private void initSubCommands(){
        subCommands.put("home", new SubCmdHome(plugin));
        subCommands.put("visual", new SubCmdVisual(plugin));
        subCommands.put("info", new SubCmdInfo(plugin));
        subCommands.put("help", new SubCmdHelp(plugin));
        subCommands.put("accept", new SubCmdAccept(plugin));
        subCommands.put("deny", new SubCmdDeny(plugin));
        subCommands.put("list", new SubCmdList(plugin));

        subCommands.put("admin", new SubCmdAdmin(plugin));
        subCommands.put("delete", new SubCmdDelete(plugin));
        subCommands.put("money", new SubCmdMoney(plugin));
        subCommands.put("upgrade", new SubCmdUpgrade(plugin));
        subCommands.put("downgrade", new SubCmdDowngrade(plugin));
        subCommands.put("editname", new SubCmdEditName(plugin));
        subCommands.put("editprefix", new SubCmdEditPrefix(plugin));
        subCommands.put("changeowner", new SubCmdChangeOwner(plugin));
        subCommands.put("addmember", new SubCmdAddMember(plugin));
        subCommands.put("removemember", new SubCmdRemoveMember(plugin));
    }
}
