package fr.army.stelyteam.conversation;

import org.bukkit.Bukkit;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.menu.impl.ConfirmMenu;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.TemporaryAction;
import fr.army.stelyteam.utils.TemporaryActionNames;
import fr.army.stelyteam.utils.manager.CacheManager;
import fr.army.stelyteam.utils.manager.MessageManager;

public class ConvEditOwner extends StringPrompt {

    private CacheManager cacheManager;
    private MessageManager messageManager;

    public ConvEditOwner(StelyTeamPlugin plugin) {
        this.cacheManager = plugin.getCacheManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public Prompt acceptInput(ConversationContext con, String answer) {
        Player player = Bukkit.getPlayer(answer);
        Player author = (Player) con.getForWhom();
        String authorName = author.getName();
        Team team = Team.initFromPlayerName(author.getName());
        
        if (player == null) {
            con.getForWhom().sendRawMessage(messageManager.getMessage("common.player_not_exist"));
            return null;
        }else if (!team.isTeamMember(answer)) {
            con.getForWhom().sendRawMessage(messageManager.getMessage("common.player_not_in_your_team"));
            return null;
        }else if (cacheManager.playerHasActionName(answer, TemporaryActionNames.EDIT_OWNER)) {
            con.getForWhom().sendRawMessage(messageManager.getMessage("common.player_already_action"));
            return null;
        }

        cacheManager.addTempAction(
            new TemporaryAction(
                authorName,
                answer, 
                TemporaryActionNames.EDIT_OWNER, 
                team)
        );
        new ConfirmMenu(author, null).openMenu();
        return null;
    }

    @Override
    public String getPromptText(ConversationContext arg0) {
        return messageManager.getMessage("manage_members.edit_owner.send_player_name");
    }
}