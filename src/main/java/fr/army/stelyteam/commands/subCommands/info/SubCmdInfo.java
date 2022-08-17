package fr.army.stelyteam.commands.subCommands.info;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.commands.SubCommand;
import fr.army.stelyteam.utils.ColorsBuilder;
import fr.army.stelyteam.utils.MessageManager;
import fr.army.stelyteam.utils.SQLManager;

public class SubCmdInfo extends SubCommand {
    private SQLManager sqlManager;
    private YamlConfiguration config;
    private YamlConfiguration messages;
    private MessageManager messageManager;
    private ColorsBuilder colorsBuilder;


    public SubCmdInfo(StelyTeamPlugin plugin) {
        super(plugin);
        this.sqlManager = plugin.getSQLManager();
        this.config = plugin.getConfig();
        this.messages = plugin.getMessages();
        this.messageManager = plugin.getMessageManager();
        // this.colorsBuilder = plugin.getColorsBuilder();
        this.colorsBuilder = new ColorsBuilder();
    }


    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        args[0] = "";
        if (args.length == 1){
            // player.sendMessage("Utilisation : /stelyteam info <nom de team>");
            player.sendMessage(messageManager.getMessage("commands.stelyteam_info.usage"));
        }else{
            String teamID = String.join("", args);
            if (sqlManager.teamIdExist(teamID)){
                String yesMessage = messages.getString("commands.stelyteam_info.true");
                String noMessage = messages.getString("commands.stelyteam_info.false");

                String teamPrefix = sqlManager.getTeamPrefix(teamID);
                String teamOwner = sqlManager.getTeamOwner(teamID);
                String creationDate = sqlManager.getCreationDate(teamID);
                Integer teamMembersLelvel = sqlManager.getTeamMembersLevel(teamID);
                Integer teamMembers = sqlManager.getMembers(teamID).size();
                Integer maxMembers = config.getInt("teamMaxMembers");
                String hasUnlockBank = (sqlManager.hasUnlockedTeamBank(teamID) ? yesMessage : noMessage);
                List<String> members = sqlManager.getMembers(teamID);
                List<String> lore = messages.getStringList("commands.stelyteam_info.output");

                lore = replaceInLore(lore, "%NAME%", teamID);
                lore = replaceInLore(lore, "%PREFIX%", colorsBuilder.replaceColor(teamPrefix));
                lore = replaceInLore(lore, "%OWNER%", teamOwner);
                lore = replaceInLore(lore, "%DATE%", creationDate);
                lore = replaceInLore(lore, "%UNLOCK_BANK%", hasUnlockBank);
                lore = replaceInLore(lore, "%MEMBER_COUNT%", IntegerToString(teamMembers));
                lore = replaceInLore(lore, "%MAX_MEMBERS%", IntegerToString(maxMembers+teamMembersLelvel));
                lore = replaceInLore(lore, "%MEMBERS%", String.join(", ", members));

                player.sendMessage(String.join("\n", lore));
            }else{
                // player.sendMessage("Cette team n'existe pas");
                player.sendMessage(messageManager.getMessage("common.team_not_exist"));
            }
        }
        return true;
    }


    private List<String> replaceInLore(List<String> lore, String value, String replace){
        List<String> newLore = new ArrayList<>();
        for(String str : lore){
            newLore.add(str.replace(value, replace));
        }
        return newLore;
    }


    private String IntegerToString(Integer value){
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }
}