package fr.army.stelyteam.external.essentialschat;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.listener.impl.ChatPrefixListener;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.builder.ColorsBuilder;
import net.ess3.api.events.LocalChatSpyEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ExternalEssentialsSpyListener implements Listener {

    private StelyTeamPlugin plugin = StelyTeamPlugin.getPlugin();
    private ColorsBuilder colorBuilder = plugin.getColorsBuilder();
    
    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onSpy(LocalChatSpyEvent event) {
        Player player = event.getPlayer();
        Team team = Team.getFromCache(player);
        String prefix = "";

        if (team != null) {
            prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        }

        event.setFormat(event.getFormat().replace(ChatPrefixListener.PREFIX_PLACEHOLDER, prefix));
    }
}
