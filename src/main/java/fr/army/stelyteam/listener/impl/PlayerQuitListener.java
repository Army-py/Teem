package fr.army.stelyteam.listener.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.army.stelyteam.StelyTeamPlugin;
import fr.army.stelyteam.team.Member;
import fr.army.stelyteam.team.Team;
import fr.army.stelyteam.utils.manager.CacheManager;

public class PlayerQuitListener implements Listener{

    private final CacheManager cacheManager;

    public PlayerQuitListener(StelyTeamPlugin plugin) {
        this.cacheManager = plugin.getCacheManager();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (cacheManager.getTempAction(playerName) != null){
            cacheManager.removePlayerAction(playerName);
        }

        Team team = Team.getFromCache(player);
        if (team == null) return;

        int count = 0;
        for (Member member : team.getTeamMembers()){
            if (member.asPlayer() != null && member.asPlayer().isOnline()){
                count++;
            }
        }
        if (count == 0){
            cacheManager.removeTeam(team);
        }
    }
}
