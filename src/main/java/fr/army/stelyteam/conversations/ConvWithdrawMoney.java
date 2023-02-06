package fr.army.stelyteam.conversations;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.utils.Team;
import fr.army.stelyteam.utils.manager.EconomyManager;
import fr.army.stelyteam.utils.manager.MessageManager;
import fr.army.stelyteam.utils.manager.MySQLManager;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class ConvWithdrawMoney extends StringPrompt {

    private MySQLManager sqlManager;
    private MessageManager messageManager;
    private EconomyManager economyManager;


    public ConvWithdrawMoney(StelyTeamPlugin plugin) {
        this.sqlManager = plugin.getSQLManager();
        this.messageManager = plugin.getMessageManager();
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public Prompt acceptInput(ConversationContext con, String answer) {
        Player author = (Player) con.getForWhom();
        Team team = sqlManager.getTeamFromPlayerName(author.getName());
        String teamName = team.getTeamName();
        Double teamMoney = team.getTeamMoney();
        Double money;

        if (answer.equals("all")){
            money = teamMoney;
        }else{
            try {
                money = Double.parseDouble(answer);
            } catch (NumberFormatException e) {
                // author.sendRawMessage("Veuillez entrer un nombre");
                author.sendRawMessage(messageManager.getMessage("manage_team.withdraw_money.send_money_amount"));
                return null;
            }
        }

        if (teamReachedMinMoney(teamName, money, teamMoney)) {
            // author.sendRawMessage("Vous ne pouvez pas retirer plus de " + teamMoney + "€");
            author.sendRawMessage(messageManager.getReplaceMessage("manage_team.withdraw_money.team_reached_min_money", teamMoney.toString()));
            return null;
        }else if (money < 0) {
            // author.sendRawMessage("Vous ne pouvez pas ajouter un montant négatif");
            author.sendRawMessage(messageManager.getMessage("manage_team.withdraw_money.cant_send_negative_money"));
            return null;
        }

        economyManager.addMoneyPlayer(author, money);
        // author.sendRawMessage("Le montant a été retiré");
        author.sendRawMessage(messageManager.getMessage("manage_team.withdraw_money.money_withdrawn"));
        team.decrementTeamMoney(money);
        return null;
    }

    @Override
    public String getPromptText(ConversationContext arg0) {
        // return "Envoie le montant à retirer";
        return messageManager.getMessage("manage_team.withdraw_money.send_money_amount");
    }


    private boolean teamReachedMinMoney(String teamID, Double money, Double teamMoney) {
        return teamMoney - money < 0;
    }
}