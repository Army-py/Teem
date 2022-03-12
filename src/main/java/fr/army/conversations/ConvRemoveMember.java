package fr.army.conversations;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import fr.army.App;
import fr.army.utils.InventoryGenerator;

public class ConvRemoveMember extends StringPrompt {

    @Override
    public Prompt acceptInput(ConversationContext con, String answer) {
        Player author = (Player) con.getForWhom();
        String teamID = App.sqlManager.getTeamIDFromOwner(author.getName());
        if (!App.sqlManager.isMemberInTeam(answer, teamID)){
            con.getForWhom().sendRawMessage("Le joueur n'est pas dans ta team");
            return null;
        }

        Inventory inventory = InventoryGenerator.createConfirmInventory();
        author.openInventory(inventory);
        App.playersKickTeam.add(answer);
        return null;
    }

    @Override
    public String getPromptText(ConversationContext arg0) {
        return "Envoie le pseudo du joueur à retirer";
    }

}